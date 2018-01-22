package example.configurationservice.spring;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.Ordered;

import com.google.common.collect.Maps;

import example.configurationservice.ConfigurationService;
import example.foundation.servicediscovery.ServiceDiscoveryFactory;

public class ConfigurationServicePlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    protected ConfigurationService configurationService = ServiceDiscoveryFactory.getServiceDiscovery().discover(
            ConfigurationService.class, null, null);

    private Map<String, String> placeHolderDefautValues = Maps.newHashMap();

    protected List<String> placeHolders;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    protected void loadProperties(Properties props) {
        for (String placeHolder : placeHolders) {
            props.put(placeHolder, loadProperty(placeHolder));
        }
    }

    private String loadProperty(String placeHolder) {
        String placeHolderValue;
        try {
            placeHolderValue = configurationService.getString(placeHolder);
        } catch (NoSuchElementException e) {
            final String defaultValue = placeHolderDefautValues.get(placeHolder);
            if (defaultValue != null) {
                placeHolderValue = defaultValue;
            } else {
                throw new NoSuchElementException(
                        String.format(
                                "no configration item [%s] found from db, check WSFCONFIG table, or add a placeHolderDefalutValues to fix it",
                                placeHolder));
            }
        } catch (Exception otherException) {
            //use primitive type change to string.
            placeHolderValue = configurationService.getObject(placeHolder).toString();
        }

        return placeHolderValue;
    }

    public void setPlaceHolders(List<String> placeHolders) {
        this.placeHolders = placeHolders;
    }

    public void setPlaceHolderDefautValues(Map<String, String> placeHolderDefautValues) {
        this.placeHolderDefautValues = placeHolderDefautValues;
    }
}
