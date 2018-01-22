package example.configurationservice;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class ConfigurationServiceFileImpl implements ConfigurationService {

    private static final String ILLEGAL_ARGUMENT = "Illegal argument: ";

    private static Logger logger = LoggerFactory.getLogger(ConfigurationServiceFileImpl.class);

    private static ResourceLoader resourceLoader = new DefaultResourceLoader();

    private Properties properties;

    public ConfigurationServiceFileImpl() {
        properties = new Properties();
    }

    public void setLocations(String locations) {
        properties = loadProperties(locations.split(","));
    }

    /**
     * Load properties from configuration files.
     */
    private Properties loadProperties(String... resourcesPaths) {
        Properties props = new Properties();

        for (String location : resourcesPaths) {

            logger.debug("Loading properties file from:" + location);

            InputStream is = null;
            try {
                Resource resource = resourceLoader.getResource(location);
                is = resource.getInputStream();
                props.load(is);
            } catch (IOException ex) {
                logger.info("Could not load properties from path:" + location + ", " + ex.getMessage());
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ioe) {
                    // ignore
                }
            }
        }
        return props;
    }

    @Override
    public String getString(String key) {
        String result = getValue(key);

        if (result != null) {
            return result;
        }

        throw new IllegalArgumentException(ILLEGAL_ARGUMENT + key);
    }

    @Override
    public String getString(String key, String defaultValue) {
        String result = getValue(key);
        if (result != null) {
            return result;
        }

        return defaultValue;
    }

    @Override
    public int getInt(String key) {
        String value = getValue(key);
        if (value != null) {
            return Integer.valueOf(value);
        }

        throw new IllegalArgumentException(ILLEGAL_ARGUMENT + key);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        String value = getValue(key);
        if (value != null) {
            return Integer.valueOf(value);
        }

        return defaultValue;
    }

    @Override
    public boolean getBoolean(String key) {
        String value = getValue(key);
        if (value != null) {
            return Boolean.valueOf(value);
        }

        throw new IllegalArgumentException(ILLEGAL_ARGUMENT + key);
    }


    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getValue(key);
        if (value != null) {
            return Boolean.valueOf(value);
        }

        return defaultValue;
    }


    @Override
    public double getDouble(String key) {
        String value = getValue(key);
        if (value != null) {
            return Double.valueOf(value);
        }

        throw new IllegalArgumentException(ILLEGAL_ARGUMENT + key);
    }


    @Override
    public double getDouble(String key, double defaultValue) {
        String value = getValue(key);
        if (value != null) {
            return Double.valueOf(value);
        }

        return defaultValue;
    }

    private String getValue(String key) {
        String result = System.getProperty(key);
        if (result != null) {
            return result;
        }

        return properties.getProperty(key);
    }


    @Override
    public Object getObject(String key) {
        return getValue(key);
    }

    @Override
    public Map<String, Object> searchObject(String key) {
        return null;
    }


}
