package example.configurationservice.local.metadata.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import example.configurationservice.local.dao.impl.MonitorableRepository;
import example.configurationservice.local.exception.ConfigurationRepositoryException;
import example.configurationservice.local.locator.FileLocator;
import example.configurationservice.local.metadata.ConfigurationMetadataRepository;
import example.configurationservice.local.model.ConfigurationEvent;
import example.configurationservice.local.model.ConfigurationMetadata;
import example.configurationservice.local.model.ConfigurationMetadataFileObject;
import example.configurationservice.local.util.ConfigurationLocalServiceCommonUtils;


public class ConfigurationMetadataYamlRepository extends MonitorableRepository implements
        ConfigurationMetadataRepository, FileAlterationListener {

    private static Logger logger = LoggerFactory
            .getLogger(ConfigurationLocalServiceCommonUtils.getLoggerName(ConfigurationMetadataYamlRepository.class));

    private ThreadLocal<Yaml> localYaml = new ThreadLocal<Yaml>();

    private Map<String, ConfigurationMetadata> metadataRepo = new HashMap<String, ConfigurationMetadata>();

    private Map<String, Map<String, ConfigurationMetadata>> subMetadataRepos = new LinkedHashMap<String, Map<String, ConfigurationMetadata>>();

    public ConfigurationMetadataYamlRepository(FileLocator metadataFileLocator)
            throws ConfigurationRepositoryException {
        setFileLocator(metadataFileLocator);
        init();
    }

    @Override
    public ConfigurationMetadata selectConfigurationMetadataByName(String name)
            throws ConfigurationRepositoryException {
        return getValueFromConfigurationData(name);
    }

    @Override
    protected List<ConfigurationEvent> loadDataFromFile(File metadataFile) throws ConfigurationRepositoryException {
        Map<String, ConfigurationMetadata> newData = loadFile(metadataFile);
        updateConfigurationMetadataRepository(metadataFile, newData);
        return null;
    }

    @Override
    protected List<ConfigurationEvent> removeDataFromFile(File configurationFile)
            throws ConfigurationRepositoryException {
        String subRepoKey = resolveSubMetadataRepoKey(configurationFile);
        removeSubMetadataRepo(subRepoKey);
        rebuildMetaDataRepo();
        logger.info("Data in file removed from metadata repo: {}", configurationFile.getAbsoluteFile());
        return null;
    }

    @Override
    public void clearData() {
        synchronized (getSynchronizedKey()) {
            clear();
        }
    }

    private void updateConfigurationMetadataRepository(File metadataFile, Map<String, ConfigurationMetadata> newData) {
        String subRepoKey = resolveSubMetadataRepoKey(metadataFile);
        replaceSubRepoWithNewData(subRepoKey, newData);
        rebuildMetaDataRepo();
    }

    private void removeSubMetadataRepo(String subRepoKey) {
        //not removing the key of subrepo to retain order of subrepos
        if (subMetadataRepos.containsKey(subRepoKey)) {
            subMetadataRepos.put(subRepoKey, new HashMap<String, ConfigurationMetadata>());
        }
    }

    private String resolveSubMetadataRepoKey(File file) {
        return file.getAbsolutePath();
    }

    private void replaceSubRepoWithNewData(String subRepoKey, Map<String, ConfigurationMetadata> newData) {
        subMetadataRepos.put(subRepoKey, newData);
    }

    private Map<String, ConfigurationMetadata> constructNewRepo() {
        Map<String, ConfigurationMetadata> newConfigurationData = new HashMap<String, ConfigurationMetadata>();

        for (Map<String, ConfigurationMetadata> subRepo : subMetadataRepos.values()) {
            newConfigurationData.putAll(subRepo);
        }

        return newConfigurationData;
    }

    private void rebuildMetaDataRepo() {
        Map<String, ConfigurationMetadata> newConfiguraionDataRepo = constructNewRepo();
        this.metadataRepo = newConfiguraionDataRepo;
    }

    protected Map<String, ConfigurationMetadata> loadFile(File configurationFile)
            throws ConfigurationRepositoryException {
        String location = configurationFile.getAbsolutePath();

        Map<String, ConfigurationMetadata> metadata = new HashMap<String, ConfigurationMetadata>();

        InputStream configInputStream = null;

        try {
            logger.debug("loading {}", configurationFile.getAbsolutePath());
            configInputStream = new FileInputStream(configurationFile);
            Yaml yaml = getYaml();

            ConfigurationMetadataFileObject objectLoaded = yaml.loadAs(configInputStream,
                    ConfigurationMetadataFileObject.class);

            if (objectLoaded != null) {
                Map<String, ConfigurationMetadata> metadataMap = objectLoaded.getMetadata();
                if (metadataMap != null) {
                    metadata.putAll(metadataMap);
                    logger.debug("loaded YAML from path:" + location);
                } else {
                    logger.warn(
                            "The content in metadata section is emtpy (or in wrong format?). Please check metadata file and ensure it's correct.");
                }
            } else {
                logger.warn(
                        "The content of metadata yaml is empty (or in wrong format?). Please check metadata file and ensure it's correct.");
            }
        } catch (IOException e) {
            logger.warn("Could not load YAML from path:{}, error message:{}", location, e.getMessage());
            throw new ConfigurationRepositoryException("Could not load YAML from path:" + location, e);
        } finally {
            try {
                if (configInputStream != null) {
                    configInputStream.close();
                }
            } catch (IOException ioe) {
                logger.error("Error occurred while closing file:" + location);
            }

        }

        return metadata;
    }

    protected ConfigurationMetadata getValueFromConfigurationData(String name) throws ConfigurationRepositoryException {
        ConfigurationMetadata value = null;
        if (this.metadataRepo != null) {
            if (!this.metadataRepo.containsKey(name)) {
                value = null;
                if (logger.isDebugEnabled()) {
                    logger.debug("Configuration metadata not found. key:" + name);
                }
            } else {
                value = this.metadataRepo.get(name);
            }
        } else {
            throw new ConfigurationRepositoryException("configuration metadata is not loaded.");
        }
        return value;
    }

    protected Set<String> findKeysByFuzzyName(String fuzzyName) {

        Set<String> matchedKeySet = new HashSet<String>();

        if (fuzzyName != null) {
            Set<String> keySet = this.metadataRepo.keySet();
            for (String key : keySet) {
                if (key.contains(fuzzyName)) {
                    matchedKeySet.add(key);
                }
            }
        }
        return matchedKeySet;
    }

    private Yaml getYaml() {
        Yaml yaml = this.localYaml.get();
        if (yaml == null) {
            yaml = new Yaml();
            this.localYaml.set(yaml);
        }
        return yaml;
    }

    protected Object getSynchronizedKey() {
        return this.metadataRepo;
    }

    protected void clear() {
        this.metadataRepo.clear();
    }

}
