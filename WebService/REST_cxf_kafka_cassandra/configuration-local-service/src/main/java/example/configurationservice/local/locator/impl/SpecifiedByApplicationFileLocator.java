package example.configurationservice.local.locator.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import example.configurationservice.local.locator.FileLocator;
import example.configurationservice.local.util.ConfigurationLocalServiceCommonUtils;
import example.configurationservice.local.util.ConfigurationLocalServiceConstants;

/**
 *
 */
public class SpecifiedByApplicationFileLocator implements FileLocator {

    private static Logger logger = LoggerFactory
            .getLogger(ConfigurationLocalServiceCommonUtils.getLoggerName(SpecifiedByApplicationFileLocator.class));

    private static ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Override
    public List<String> locateFileUrl() {
        return resolveFinalLocationsFromDynamicConfig();
    }

    private List<String> resolveFinalLocationsFromDynamicConfig() {
        String finalLocations = null;
        Properties props = loadProperties("classpath:configuration-path.properties");
        if (props != null) {
            finalLocations = props.getProperty("dynamic-configuration-path");
        }
        if (finalLocations == null) {
            finalLocations = ConfigurationLocalServiceConstants.DYNAMIC_CONFIGURATION_PROPERTIES_DEFAULT_PATH;
        }
        return Arrays.asList(finalLocations.split(","));
    }

    private Properties loadProperties(String... resourcesPaths) {
        Properties props = new Properties();

        for (String location : resourcesPaths) {
            if (logger.isDebugEnabled()) {
                logger.debug("Loading properties file from:" + location);
            }
            InputStream is = null;

            try {
                Resource ioe = resourceLoader.getResource(location);
                is = ioe.getInputStream();
                props.load(is);
                if (logger.isDebugEnabled()) {
                    logger.debug("loaded properties from path:" + location);
                }
            } catch (IOException e) {
                logger.info("Could not load properties from path:" + location + ", " + e.getMessage());
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ioe) {
                    logger.warn("Error occurred while closing input stream of resource:" + location);
                }

            }
        }

        return props;
    }
}
