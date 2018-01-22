package example.configurationservice.spring;

import java.util.Properties;

/**
 * The class is to replace the placeholder with the value get from
 * ConfigurationService.
 */
public class CommonFunctionPlaceholderConfigurer extends ConfigurationServicePlaceholderConfigurer {

	@Override
	protected void loadProperties(Properties props) {
		for (String placeHolder : placeHolders) {
			String placeHolderValue = configurationService.getString(placeHolder, null);
			props.put(placeHolder, placeHolderValue == null ? "" : placeHolderValue);
		}
	}

}
