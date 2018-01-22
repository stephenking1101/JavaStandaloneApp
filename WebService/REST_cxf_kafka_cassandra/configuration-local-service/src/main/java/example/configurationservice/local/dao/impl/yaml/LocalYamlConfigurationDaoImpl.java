package example.configurationservice.local.dao.impl.yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import example.configurationservice.local.dao.impl.AbstractLocalFileConfigurationDaoImpl;
import example.configurationservice.local.exception.ConfigurationRepositoryException;
import example.configurationservice.local.locator.FileLocator;
import example.configurationservice.local.metadata.ConfigurationMetadataRepository;
import example.configurationservice.local.model.ConfigurationEvent;
import example.configurationservice.local.model.ConfigurationEventTypeEnum;
import example.configurationservice.local.util.ConfigurationLocalServiceCommonUtils;

public class LocalYamlConfigurationDaoImpl extends AbstractLocalFileConfigurationDaoImpl {

    private static Logger logger = LoggerFactory
            .getLogger(ConfigurationLocalServiceCommonUtils.getLoggerName(LocalYamlConfigurationDaoImpl.class));

    private ThreadLocal<Yaml> localYaml = new ThreadLocal<Yaml>();

    private Map<String, Object> configurationData = new HashMap<String, Object>();

    private OrderedSubRepos subRepos = new OrderedSubRepos();

    private synchronized Map<String, Object> getConfigurationData(){
        return configurationData;
    }

    private synchronized void setConfigurationData(Map<String, Object> configurationData){
        this.configurationData = configurationData;
    }

    public LocalYamlConfigurationDaoImpl(FileLocator fileLocator) throws ConfigurationRepositoryException {
        super(fileLocator, null);
    }

    public LocalYamlConfigurationDaoImpl(FileLocator fileLocator, ConfigurationMetadataRepository metadataRepository)
            throws ConfigurationRepositoryException {
        super(fileLocator, metadataRepository);
    }

    @Override
    protected List<ConfigurationEvent> loadDataFromFile(File configurationFile) throws ConfigurationRepositoryException {
        return doLoadConfigurationFile(configurationFile);
    }

    @Override
    protected List<ConfigurationEvent> removeDataFromFile(File configurationFile) {
        return doRemoveConfigurationFile(configurationFile);
    }

    @Override
    protected String getValueFromConfigurationData(String name) throws ConfigurationRepositoryException {
        String value = null;
        Map<String, Object> configData = getConfigurationData();
        if (configData == null) {
            throw new ConfigurationRepositoryException("configuration data is not loaded.");
        }

        if (!configData.containsKey(name)) {
            value = null;
            if (logger.isDebugEnabled()) {
                logger.debug("Configuration item not found. key:" + name);
            }
        } else {
            Object rawValue = configData.get(name);
            // TODO [FEATURE] support complex object in the future
            if (rawValue instanceof String) {
                value = (String) rawValue;
                if (logger.isDebugEnabled()) {
                    logger.debug("Configuration item found. key:{}, value :{}", name, value);
                }
            } else {
                value = String.valueOf(rawValue);
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Configuration item [{}] found but the value is not a plain string. value will be transformed to String.",
                            name);
                }
            }
        }
        return value;
    }

    @Override
    protected Set<String> findKeysByFuzzyName(String fuzzyName) {

        Set<String> matchedKeySet = new HashSet<String>();

        if (fuzzyName != null) {
            Set<String> keySet = getConfigurationData().keySet();
            for (String key : keySet) {
                if (key.contains(fuzzyName)) {
                    matchedKeySet.add(key);
                }
            }
        }
        return matchedKeySet;
    }

    @Override
    protected Object getSynchronizedKey() {
        return getConfigurationData();
    }

    @Override
    protected void clear() {
        getConfigurationData().clear();
    }

    private List<ConfigurationEvent> doLoadConfigurationFile(File configurationFile)
            throws ConfigurationRepositoryException {
        Map<String, Object> newData = loadFile(configurationFile);
        List<ConfigurationEvent> configurationEvents = updateConfigurationDataRepository(configurationFile, newData);
        logger.info("Data in file reloaded: {}", configurationFile.getAbsoluteFile());
        return configurationEvents;
    }

    private List<ConfigurationEvent> updateConfigurationDataRepository(File file, Map<String, Object> newData) {
        // update configuration data repository by by combining all existing sub repositories,
        // and construct a brand-new repository.
        String subRepoKey = resolveSubRepoKey(file);
        List<ConfigurationEvent> configurationEventList = replaceSubRepoWithNewData(subRepoKey, newData);
        setConfigurationData(subRepos.constructNewRepo());
        return configurationEventList;
    }


    private List<ConfigurationEvent> doRemoveConfigurationFile(File configurationFile) {
        String subRepoKey = resolveSubRepoKey(configurationFile);
        List<ConfigurationEvent> configurationEventList = ConfigurationLocalServiceCommonUtils.diffDataRepo(
                subRepos.getSubRepo(subRepoKey), new HashMap<String, Object>());

        subRepos.removeSubRepo(subRepoKey);
        setConfigurationData(subRepos.constructNewRepo());

        logger.info("Data in file removed from configuration data repo: {}", configurationFile.getAbsoluteFile());
        return configurationEventList;
    }

    private String resolveSubRepoKey(File file) {
        return file.getAbsolutePath();
    }

    private List<ConfigurationEvent> replaceSubRepoWithNewData(String subRepoKey, Map<String, Object> newData) {
        List<ConfigurationEvent> configurationEventList = ConfigurationLocalServiceCommonUtils.diffDataRepo(
                subRepos.getSubRepo(subRepoKey), newData);
        subRepos.putSubRepo(subRepoKey, newData);
        return configurationEventList;
    }

    private List<ConfigurationEvent> constructConfigurationEvent(Map<String, ?> keyMap,
            ConfigurationEventTypeEnum eventType) {
        List<ConfigurationEvent> eventList = new ArrayList<ConfigurationEvent>();

        if (!CollectionUtils.isEmpty(keyMap)) {
            for (String configurationKey : keyMap.keySet()) {
                ConfigurationEvent event = new ConfigurationEvent();
                event.setConfigurationKey(configurationKey);
                event.setEventType(eventType);
                logger.debug("Add configuration event: {} into list", event);
                eventList.add(event);
            }
        }

        return eventList;
    }

    protected Map<String, Object> loadFile(File configurationFile) throws ConfigurationRepositoryException {
        String location = configurationFile.getAbsolutePath();

        Map<String, Object> object = new HashMap<String, Object>();

        InputStream configInputStream = null;

        try {
            logger.debug("loading YAML filr {}", configurationFile.getAbsolutePath());
            configInputStream = new FileInputStream(configurationFile);
            Yaml yaml = getYaml();
            Map<String, Object> newObject = yaml.loadAs(configInputStream, Map.class);
            if (newObject != null) {
                object.putAll(newObject);
                logger.debug("loaded YAML from file:{}", location);
            } else {
                logger.warn("loaded YAML from file:{}, but data is null.", location);
            }
        } catch (IOException e) {
            logger.warn("Could not load YAML from path:{}, error message:{}", location, e.getMessage());
            throw new ConfigurationRepositoryException("Could not load YAML from path:" + location, e);
        } catch (YAMLException yamle) {
            logger.warn("Error occurred while parsing yaml file from path:{}, error message:{}", location,
                    yamle.getMessage());
            logger.warn("Detailed YAML exception: ", yamle);
            throw new ConfigurationRepositoryException("Error occurred while parsing yaml file from path:" + location,
                    yamle);
        } finally {
            try {
                if (configInputStream != null) {
                    configInputStream.close();
                }
            } catch (IOException ioe) {
                logger.error("Error occurred while closing file:" + location);
            }

        }

        return object;
    }


    private Yaml getYaml() {
        Yaml yaml = this.localYaml.get();
        if (yaml == null) {
            yaml = new Yaml();
            this.localYaml.set(yaml);
        }
        return yaml;
    }

    private static class OrderedSubRepos {
        private List<String> keysInSequence = new LinkedList<String>();
        private Map<String, Map<String, Object>> reposMaps = new ConcurrentHashMap<String, Map<String, Object>>();

        private Map<String, Object> constructNewRepo() {
            Map<String, Object> newConfigurationData = new HashMap<String, Object>();

            for (String key : keysInSequence){
                newConfigurationData.putAll(reposMaps.get(key));
            }
            return newConfigurationData;
        }

        Map<String, Object> getSubRepo(String key){
            return reposMaps.get(key);
        }

        void putSubRepo(String key, Map<String, Object> subRepo){
            if (!keysInSequence.contains(subRepo)){
                // Add key in reversed sequence
                // Hence, the pre added repo's k/v can replace the later one.
                keysInSequence.add(0, key);
            }
            reposMaps.put(key, subRepo);
        }

        void removeSubRepo(String key){
            if (reposMaps.containsKey(key)){
                reposMaps.get(key).clear();
            }
        }
    }
}
