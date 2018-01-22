package example.configurationservice.local.dao.impl.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import example.configurationservice.local.dao.impl.AbstractLocalFileConfigurationDaoImpl;
import example.configurationservice.local.exception.ConfigurationRepositoryException;
import example.configurationservice.local.locator.FileLocator;
import example.configurationservice.local.metadata.ConfigurationMetadataRepository;
import example.configurationservice.local.model.ConfigurationEvent;
import example.configurationservice.local.util.ConfigurationLocalServiceCommonUtils;

public class LocalPropertiesConfigurationDaoImpl extends AbstractLocalFileConfigurationDaoImpl {

    protected static Logger logger = LoggerFactory
            .getLogger(ConfigurationLocalServiceCommonUtils.getLoggerName(LocalPropertiesConfigurationDaoImpl.class));

    private Properties configurationData = new Properties();

    public LocalPropertiesConfigurationDaoImpl(FileLocator fileLocator)
            throws ConfigurationRepositoryException, IOException {
        super(fileLocator, null);
    }

    public LocalPropertiesConfigurationDaoImpl(FileLocator fileLocator,
            ConfigurationMetadataRepository metadataRepository) throws ConfigurationRepositoryException {
        super(fileLocator, metadataRepository);
    }

    @Override
    protected Object getSynchronizedKey() {
        return this.configurationData;
    }

    @Override
    protected void clear() {
        this.configurationData.clear();
    }

    @Override
    protected List<ConfigurationEvent> loadDataFromFile(File configurationFile) throws ConfigurationRepositoryException {
        return doLoadConfigurationFile(configurationFile);
    }

    @Override
    protected List<ConfigurationEvent> removeDataFromFile(File configFile) throws ConfigurationRepositoryException {
        throw new UnsupportedOperationException();
    }

    protected List<ConfigurationEvent> doLoadConfigurationFile(File configurationFile) throws ConfigurationRepositoryException {
        Properties newProperties = loadFile(configurationFile);
        updateConfigurationDataRepository(newProperties);
        return new ArrayList<ConfigurationEvent>();
    }

    private void updateConfigurationDataRepository(Properties newProperties) {
        // FIXME handle deleting items by storing each file in separate Properties map.
        // currently just combines all data into one properties simply.
        this.configurationData.putAll(newProperties);
    }

    protected Properties loadFile(File configurationFile) throws ConfigurationRepositoryException {
        Properties props = new Properties();
        String location = configurationFile.getAbsolutePath();

        InputStream configInputStream = null;

        try {
            logger.debug("loading {}", configurationFile.getAbsolutePath());
            configInputStream = new FileInputStream(configurationFile);
            props.load(configInputStream);
            logger.debug("loaded properties from path: {}", location);
        } catch (IOException e) {
            logger.error("Could not load properties from path:" + location + ", error message:" + e.getMessage());
            throw new ConfigurationRepositoryException("Could not load properties from path:" + location, e);
        } finally {
            try {
                if (configInputStream != null) {
                    configInputStream.close();
                }
            } catch (IOException ioe) {
                logger.error("Error occurred while closing configuration file:" + location);
            }
        }

        return props;
    }

    @Override
    protected String getValueFromConfigurationData(String name) throws ConfigurationRepositoryException {
        String value = null;
        if (this.configurationData != null) {
            value = this.configurationData.getProperty(name);
        } else {
            throw new ConfigurationRepositoryException("configuration data is not loaded.");
        }
        return value;
    }

    @Override
    protected Set<String> findKeysByFuzzyName(String fuzzyName) {

        Set<String> matchedKeySet = new HashSet<String>();

        if (fuzzyName != null) {
            Set<Object> keySet = this.configurationData.keySet();
            for (Object key : keySet) {
                if (key instanceof String) {
                    String keyStr = (String) key;
                    if (keyStr.contains(fuzzyName)) {
                        matchedKeySet.add(keyStr);
                    }
                } else {
                    logger.warn("");
                }
            }
        }
        return matchedKeySet;
    }

}
