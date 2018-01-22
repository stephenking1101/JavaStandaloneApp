package example.configurationservice.spring;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.Ordered;

import example.configurationservice.ConfigurationService;
import example.foundation.servicediscovery.ServiceDiscoveryFactory;


public class ConfigurationServicePlaceholderConfigurerTest {

    @Mock
    private ConfigurationService configurationService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ServiceDiscoveryFactory.getMocks().put(ConfigurationService.class, configurationService);
    }

    @After
    public void tearDown() throws Exception {
        Mockito.reset(this.configurationService);
        ServiceDiscoveryFactory.getMocks().clear();
    }

    @Test
    public void testGetOrder() {
        ConfigurationServicePlaceholderConfigurer configurer = new ConfigurationServicePlaceholderConfigurer();
        assertEquals("getOrder is incorrect", Ordered.LOWEST_PRECEDENCE, configurer.getOrder());
    }

    @Test
    public void testLoadProperties() {
        Mockito.when(this.configurationService.getString("config1")).thenReturn("value1");
        Mockito.when(this.configurationService.getString("config2")).thenReturn("value2");

        ConfigurationServicePlaceholderConfigurer configurer = new ConfigurationServicePlaceholderConfigurer();
        List<String> placeHolders = new ArrayList<String>();
        placeHolders.add("config1");
        placeHolders.add("config2");
        configurer.setPlaceHolders(placeHolders);
        Properties props = new Properties();
        configurer.loadProperties(props);

        assertEquals("config1 is incorrect", "value1", props.get("config1"));
        assertEquals("config2 is incorrect", "value2", props.get("config2"));

        Mockito.verify(this.configurationService, Mockito.times(1)).getString("config1");
        Mockito.verify(this.configurationService, Mockito.times(1)).getString("config2");
    }

    @Test
    public void testLoadPropertiesWithOtherType() {
        Mockito.when(this.configurationService.getString("config1")).thenThrow(new IllegalArgumentException("TEST"));
        Mockito.when(this.configurationService.getString("config2")).thenThrow(new IllegalArgumentException("TEST"));

        Mockito.when(this.configurationService.getObject("config1")).thenReturn(Long.valueOf(1000));
        Mockito.when(this.configurationService.getObject("config2")).thenReturn(Long.valueOf(2000));

        ConfigurationServicePlaceholderConfigurer configurer = new ConfigurationServicePlaceholderConfigurer();
        List<String> placeHolders = new ArrayList<String>();
        placeHolders.add("config1");
        placeHolders.add("config2");
        configurer.setPlaceHolders(placeHolders);
        Properties props = new Properties();
        configurer.loadProperties(props);

        assertEquals("config1 is incorrect", "1000", props.get("config1"));
        assertEquals("config2 is incorrect", "2000", props.get("config2"));

        Mockito.verify(this.configurationService, Mockito.times(1)).getString("config1");
        Mockito.verify(this.configurationService, Mockito.times(1)).getString("config2");
    }

    @Test
    public void testLoadPropertiesWithDefaultValue() {
        Mockito.when(this.configurationService.getString("config1")).thenReturn("value1");
        Mockito.when(this.configurationService.getString("config2")).thenThrow(new NoSuchElementException());

        ConfigurationServicePlaceholderConfigurer configurer = new ConfigurationServicePlaceholderConfigurer();
        List<String> placeHolders = new ArrayList<String>();
        placeHolders.add("config1");
        placeHolders.add("config2");
        configurer.setPlaceHolders(placeHolders);
        Map<String, String> defaultValues = new HashMap<String, String>();
        defaultValues.put("config2", "value2");
        configurer.setPlaceHolderDefautValues(defaultValues);

        Properties props = new Properties();
        configurer.loadProperties(props);

        assertEquals("config1 is incorrect", "value1", props.get("config1"));
        assertEquals("config2 is incorrect", "value2", props.get("config2"));

        Mockito.verify(this.configurationService, Mockito.times(1)).getString("config1");
        Mockito.verify(this.configurationService, Mockito.times(1)).getString("config2");
    }
}
