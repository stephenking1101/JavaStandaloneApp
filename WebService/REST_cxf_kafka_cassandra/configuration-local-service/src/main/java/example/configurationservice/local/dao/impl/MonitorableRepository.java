package example.configurationservice.local.dao.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import example.configurationservice.local.exception.ConfigurationRepositoryException;
import example.configurationservice.local.impl.ConfigurationUpdateCallback;
import example.configurationservice.local.locator.FileLocator;
import example.configurationservice.local.locator.MonitoredResourceConverter;
import example.configurationservice.local.model.ConfigurationEvent;
import example.configurationservice.local.model.MonitoredResource;
import example.configurationservice.local.util.ConfigurationLocalServiceCommonUtils;

public abstract class MonitorableRepository implements FileAlterationListener {

    private static Logger logger = LoggerFactory
            .getLogger(ConfigurationLocalServiceCommonUtils.getLoggerName(MonitorableRepository.class));

    protected FileLocator fileLocator;

    protected List<MonitoredResource> monitoredResources = new ArrayList<MonitoredResource>();

    protected Set<MonitoredResource> alreadyObserved = new HashSet<MonitoredResource>();

    protected List<String> finalLocations;

    protected FileAlterationMonitor fileMonitor;

    protected Map<String, ConfigurationUpdateCallback> updateCallbackMap;

    protected abstract void clearData();

    protected abstract List<ConfigurationEvent> loadDataFromFile(File metadataFile)
            throws ConfigurationRepositoryException;

    protected abstract List<ConfigurationEvent> removeDataFromFile(File metadataFile)
            throws ConfigurationRepositoryException;

    protected long getInterval() {
        return ConfigurationLocalServiceCommonUtils.getDefaultInterval();
    }

    public List<MonitoredResource> getMonitoredResources() {
        return monitoredResources;
    }

    public FileLocator getFileLocator() {
        return fileLocator;
    }

    public void setFileLocator(FileLocator fileLocator) {
        this.fileLocator = fileLocator;
    }

    protected void init() throws ConfigurationRepositoryException {
        if (this.finalLocations != null) {
            return;
        }
        this.finalLocations = getFileLocator().locateFileUrl();
        if ((this.finalLocations == null) || this.finalLocations.isEmpty()) {
            throw new ConfigurationRepositoryException();
        }
        setMonitoredResources(new MonitoredResourceConverter().convertToMonitordResource(this.finalLocations));
    }

    public void setMonitoredResources(List<MonitoredResource> monitoredResources) {
        this.monitoredResources = monitoredResources;
    }

    public void startMonitor() throws ConfigurationRepositoryException {
        startMonitor(true);
    }

    public void startMonitor(boolean isStartMonitor) throws ConfigurationRepositoryException {
        stopMonitor();
        clearData();

        List<MonitoredResource> resources = getMonitoredResources();//getConfigurationFolders();

        loadDataFromAllResources(resources);
        if (isStartMonitor) {
            monitorAllResources(resources);
        }
    }

    public void stopMonitor() {
        if (fileMonitor != null) {
            try {
                logger.debug("Stopping monitor.");
                fileMonitor.stop();
                logger.info("Monitor for local configuration metadata files has been stopped.");
            } catch (IllegalStateException e) {
                logger.warn("No need to stop monitor because it isn't running.");
            } catch (Exception e) {
                logger.error("Exception occurs when stop monitor", e);
            }
        }
    }

    protected void monitorAllResources(List<MonitoredResource> resources) {
        if (fileMonitor == null) {
            fileMonitor = new FileAlterationMonitor(getInterval());
        }

        for (MonitoredResource resource : resources) {
            monitorResource(resource);
        }

        try {
            fileMonitor.start();
            logger.info("Setup monitor for configuration resource '" + resources.toString());
        } catch (Exception e) {
            logger.error("Exception occurs when setup monitor", e);
        }

    }

    protected void monitorResource(MonitoredResource monitoredResource) {
        if (!alreadyObserved.contains(monitoredResource)) {
            String folder = monitoredResource.getAbsolutePathOfFolder();
            FileAlterationObserver fileObserver = new FileAlterationObserver(folder,
                    monitoredResource.getIoFileFilter());
            fileObserver.addListener(this);
            fileMonitor.addObserver(fileObserver);
            alreadyObserved.add((monitoredResource));
        } else {
            // just ignored
        }
    }

    protected void loadDataFromAllResources(List<MonitoredResource> resources) throws ConfigurationRepositoryException {
        for (MonitoredResource resource : resources) {
            logger.info("ConfigurationMetadata resource is '" + resource
                    + "'. Start to read configuration metadata into system.");
            boolean isLoaded = loadDataFromResource(resource);
        }
    }

    protected boolean loadDataFromResource(MonitoredResource monitoredResource)
            throws ConfigurationRepositoryException {
        logger.info("Loading all configurations in resource: " + monitoredResource);

        List<File> allFiles = monitoredResource.getConfigFiles();

        if (allFiles.isEmpty()) {
            logger.warn("No configuration files found in resource: {}", monitoredResource.toStringWithFilter());
        } else {
            logger.debug("The count of configuration files to load: {}", allFiles.size());
            for (File configFile : allFiles) {
                logger.debug("Reading configuration from file {}", configFile.getAbsolutePath());
                loadDataFromFile(configFile);
            }
        }

        return true;
    }

    @Override
    public void onStart(FileAlterationObserver fileAlterationObserver) {
        //No need to implemented in current solution
    }

    @Override
    public void onDirectoryCreate(File file) {
        //No need to implemented in current solution

        if (file == null) {
            logger.debug("onDirectoryDelete: File is null");
            return;
        }

        logger.debug("onDirectoryChange: Directory has been created: {}", file.getAbsolutePath());

    }

    @Override
    public void onDirectoryChange(File file) {
        //No need to implemented in current solution

        if (file == null) {
            logger.debug("onDirectoryDelete: File is null");
            return;
        }

        logger.debug("onDirectoryChange: Directory has been changed: {}", file.getAbsolutePath());
    }

    @Override
    public void onDirectoryDelete(File file) {
        //No need to implemented in current solution

        if (file == null) {
            logger.debug("onDirectoryDelete: File is null");
            return;
        }

        logger.debug("onDirectoryDelete: Directory has been deleted: {}", file.getAbsolutePath());
    }

    @Override
    public void onFileCreate(File file) {
        if (file == null) {
            logger.debug("File is null");
            return;
        }
        logger.debug("File created. Loading data from it: {}", file.getAbsolutePath());
        try {
            List<ConfigurationEvent> configurationEvents = loadDataFromFile(file);
            notifyChanges(configurationEvents);
        } catch (ConfigurationRepositoryException e) {
            logger.error("Error occurred while loading data file: {}", e.getMessage());
        }
    }

    @Override
    public void onFileChange(File file) {
        if (file == null) {
            logger.debug("File is null");
            return;
        }
        logger.debug("File content changed. Reloading data from it: {}", file.getAbsolutePath());
        try {
            List<ConfigurationEvent> configurationEvents = loadDataFromFile(file);
            notifyChanges(configurationEvents);
        } catch (ConfigurationRepositoryException e) {
            logger.error("Error occurred while loading data file: {}", e.getMessage());
        }
    }

    @Override
    public void onFileDelete(File file) {
        if (file == null) {
            logger.debug("File is null");
            return;
        }
        logger.debug("File has been deleted. Removing data data in it: {}", file.getAbsolutePath());
        try {
            List<ConfigurationEvent> configurationEvents = removeDataFromFile(file);
            notifyChanges(configurationEvents);
        } catch (ConfigurationRepositoryException e) {
            logger.error("Error occurred while removing data from file: {}", e.getMessage());
        }
    }

    @Override
    public void onStop(FileAlterationObserver fileAlterationObserver) {
        //No need to implemented in current solution
    }

    public Map<String, ConfigurationUpdateCallback> getUpdateCallbackMap() {
        return updateCallbackMap;
    }

    public void registerUpdateCallback(String configurationKey, ConfigurationUpdateCallback updateCallback) {
        if (updateCallbackMap == null) {
            updateCallbackMap = new HashMap<String, ConfigurationUpdateCallback>();
        }
        updateCallbackMap.put(configurationKey, updateCallback);
    }

    protected void notifyChanges(List<ConfigurationEvent> configurationEvents) {
        if (CollectionUtils.isEmpty(updateCallbackMap) || CollectionUtils.isEmpty(configurationEvents)) {
            logger.debug("updateCallbackMap isEmpty: {}", CollectionUtils.isEmpty(updateCallbackMap));
            logger.debug("configurationEvents isEmpty: {}", CollectionUtils.isEmpty(configurationEvents));
            logger.debug("No callback map or no configuration events, so no need to notify");
            return;
        }

        for (ConfigurationEvent configurationEvent : configurationEvents) {
            ConfigurationUpdateCallback callBack = updateCallbackMap.get(configurationEvent.getConfigurationKey());
            if (callBack != null) {
                logger.debug("{} is registered for configuration key: {}", callBack,
                        configurationEvent.getConfigurationKey());
                try {
                    callBack.nofityChange(configurationEvent);
                } catch (Exception e) {
                    logger.warn("Error occurred while calling update callback:{} for configuration event:{}", callBack
                            .getClass().getSimpleName(), configurationEvent, e);
                }
            } else {
                logger.debug("No callback register for configuration key: {}", configurationEvent.getConfigurationKey());
            }
        }
    }
}
