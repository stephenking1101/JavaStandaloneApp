package example.configurationservice.local.dao.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import example.configuration.type.Configuration;
import example.configuration.type.ConfigurationList;
import example.configurationservice.local.dao.MonitorableReadonlyConfigurationDAO;
import example.configurationservice.local.exception.ConfigurationRepositoryException;
import example.configurationservice.local.locator.FileLocator;
import example.configurationservice.local.metadata.ConfigurationMetadataRepository;
import example.configurationservice.local.model.ConfigurationMetadata;
import example.configurationservice.local.util.ConfigurationLocalServiceCommonUtils;

public abstract class AbstractLocalFileConfigurationDaoImpl extends MonitorableRepository
        implements MonitorableReadonlyConfigurationDAO, FileAlterationListener {

    protected static Logger logger = LoggerFactory
            .getLogger(ConfigurationLocalServiceCommonUtils.getLoggerName(AbstractLocalFileConfigurationDaoImpl.class));



    protected ConfigurationMetadataRepository metadataRepository;

    public AbstractLocalFileConfigurationDaoImpl(FileLocator fileLocator)
            throws ConfigurationRepositoryException, IOException {
        this(fileLocator, null);
    }

    public AbstractLocalFileConfigurationDaoImpl(FileLocator fileLocator,
            ConfigurationMetadataRepository metadataRepository) throws ConfigurationRepositoryException {
        if (fileLocator == null) {
            throw new IllegalArgumentException("Parameter fileLocator cannot be null.");
        }
        setFileLocator(fileLocator);
        setMetadataRepository(metadataRepository);
        init();
    }

    protected abstract Object getSynchronizedKey();

    protected abstract void clear();

    public FileLocator getFileLocator() {
        return fileLocator;
    }

    public void setFileLocator(FileLocator fileLocator) {
        this.fileLocator = fileLocator;
    }

    public void setMetadataRepository(ConfigurationMetadataRepository metadataRepository)
            throws ConfigurationRepositoryException {
        this.metadataRepository = metadataRepository;
        if (metadataRepository != null) {
            this.metadataRepository.startMonitor();
        }
    }

    public Configuration selectConfigurationByName(String name) throws ConfigurationRepositoryException {
        String value = getValueFromConfigurationData(name);

        if (value != null) {
            Configuration configuration = encapsulateConfigurationMetadataAndValue(name, value);
            return configuration;
        }

        return null;
    }

    protected Configuration encapsulateConfigurationMetadataAndValue(String name, String value)
            throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName(name);
        configuration.setValue(value);

        if (metadataRepository != null) {
            ConfigurationMetadata metadata = metadataRepository.selectConfigurationMetadataByName(name);
            if (metadata != null) {
                configuration.setType(metadata.getType());
                configuration.setDescription(metadata.getDescription());
            } else {
                logger.warn("Could not find metadata of configuration item:{}", name);
            }
        }
        return configuration;
    }

    protected abstract String getValueFromConfigurationData(String name) throws ConfigurationRepositoryException;

    @Override
    public ConfigurationList selectConfigurationByFuzzyName(String fuzzyName) throws ConfigurationRepositoryException {
        List<Configuration> result = new ArrayList<Configuration>();

        Set<String> keySet = findKeysByFuzzyName(fuzzyName);

        for (String key : keySet) {
            String value = getValueFromConfigurationData(key);
            Configuration configuration = encapsulateConfigurationMetadataAndValue(key, value);
            result.add(configuration);
        }

        ConfigurationList configList = new ConfigurationList();

        if (result.size() > 0) {
            configList.getConfigurations().addAll(result);
        }
        return configList;
    }

    /**
     * @param fuzzyName if fuzzyName is null or empty string, return
     *            an empty set.
     * @return keys found
     */
    protected abstract Set<String> findKeysByFuzzyName(String fuzzyName);

    //@Override
    public ConfigurationList selectConfigurationsByQueryString(String queryString)
            throws ConfigurationRepositoryException {
        return null;
    }

    @Override
    protected void clearData() {
        synchronized (getSynchronizedKey()) {
            clear();
        }
    }

}
