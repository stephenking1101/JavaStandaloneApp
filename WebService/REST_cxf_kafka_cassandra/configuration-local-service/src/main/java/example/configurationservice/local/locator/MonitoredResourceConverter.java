package example.configurationservice.local.locator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import example.configurationservice.local.exception.ConfigurationRepositoryException;
import example.configurationservice.local.model.MonitoredResource;
import example.configurationservice.local.util.ConfigurationLocalServiceCommonUtils;

/**
 *
 */
public class MonitoredResourceConverter {

    protected static Logger logger = LoggerFactory
            .getLogger(ConfigurationLocalServiceCommonUtils.getLoggerName(MonitoredResourceConverter.class));

    protected static ResourceLoader resourceLoader = new DefaultResourceLoader();

    public List<MonitoredResource> convertToMonitordResource(List<String> urls) {
        List<MonitoredResource> monitoredResources = new ArrayList<MonitoredResource>();
        for (String url : urls) {
            Resource resource = resourceLoader.getResource(url);
            try {
                MonitoredResource monitoredResource = getMonitoredResource(url, resource);
                monitoredResources.add(monitoredResource);
            } catch (ConfigurationRepositoryException e) {
                logger.warn("Could not locate resource:{}, error message:{}", url, e.getMessage());
            }
        }
        return monitoredResources;
    }

    private MonitoredResource getMonitoredResource(String url, Resource resource)
            throws ConfigurationRepositoryException {
        MonitoredResource monitoredResource;
        try {
            monitoredResource = new MonitoredResource();
            File file = resource.getFile();
            File folder;
            if (file == null) {
                throw new ConfigurationRepositoryException("Could not get file object of resource:" + url);
            }

            boolean isDirectory = file.isDirectory();
            if (isDirectory) {
                folder = file;
            } else {
                monitoredResource.setFileObject(file);
                monitoredResource.setFileName(file.getName());

                folder = file.getParentFile();
            }

            monitoredResource.setUrl(url);
            monitoredResource.setIsDirectory(isDirectory);
            monitoredResource.setAbsolutePath(file.getAbsolutePath());
            if (folder != null) {
                monitoredResource.setFolderFileObject(folder);
                monitoredResource.setAbsolutePathOfFolder(folder.getAbsolutePath());
            }

            monitoredResource.constuctIOFileFilter();
        } catch (IOException e) {
            logger.info("Error get file of resource {}", e.getMessage());
            throw new ConfigurationRepositoryException("Could not get file object of resource:" + url, e);
        }
        return monitoredResource;
    }

}
