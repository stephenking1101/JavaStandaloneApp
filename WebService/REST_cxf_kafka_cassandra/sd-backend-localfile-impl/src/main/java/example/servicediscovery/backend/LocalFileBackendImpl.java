package example.servicediscovery.backend;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import example.foundation.servicediscovery.support.util.Backend;
import example.foundation.servicediscovery.support.util.Service;

public class LocalFileBackendImpl implements Backend {
    private static Logger logger = LoggerFactory.getLogger(LocalFileBackendImpl.class);

    private Map<String, List<Service>> serviceListCacheMap = Maps.newHashMap();
    private Map<String, List<Service>> serviceActiveCacheMap = Maps.newHashMap();
    private Map<String, Service.STATUS> serviceStatusCacheMap = Maps.newHashMap();

    private LocalFileYmlReader ymlReader;
    private final Random random = new Random();

    private static FileAlterationMonitor monitor;
    static {
        try {
            monitor = new FileAlterationMonitor(LocalFileConstants.POLLING_INTERVAL);
            monitor.start();
            logger.debug("file monitor started");
        } catch (Exception e) {
            logger.error("file monitor start error", e);
        }
    }

    public LocalFileBackendImpl() {
        this(LocalFileConstants.SERVICES_FILE_PATH);
    }

    public LocalFileBackendImpl(String filePath) {
        ymlReader = new LocalFileYmlReader(filePath);
        updateCacheMap();
        addFileObserver(filePath);
    }

    @Override
    public Service getOneActiveInstance(String serviceName) {
        logger.debug("begin get one active service for serviceName:{}", serviceName);
        if (StringUtils.isBlank(serviceName)) {
            return null;
        }
        String serviceNameUpper = serviceName.toUpperCase();
        checkAndAddActiveListCache(serviceNameUpper);
        List<Service> serviceList = serviceActiveCacheMap.get(serviceNameUpper);
        Service result = null;
        if (serviceList != null && serviceList.size() > 0) {
            result = serviceList.get(random.nextInt(serviceList.size()));
        }
        logger.debug("finish get one active service for serviceName:{}, result is {}", serviceNameUpper, result);
        return result;
    }

    private void checkAndAddActiveListCache(String serviceName) {
        if (StringUtils.isNotBlank(serviceName) && !serviceActiveCacheMap.containsKey(serviceName)) {
            if (serviceListCacheMap.containsKey(serviceName)) {
                List<Service> tmpServiceActiveList = Lists.newArrayList();
                for (Service service : serviceListCacheMap.get(serviceName)) {
                    if (Service.STATUS.ACTIVE.equals(service.getStatus())) {
                        tmpServiceActiveList.add(service);
                    }
                }
                if (tmpServiceActiveList.size() > 0) {
                    serviceActiveCacheMap.put(serviceName, tmpServiceActiveList);
                    logger.debug("check add service active list cache name:{}, size:{}",
                            serviceName, tmpServiceActiveList.size());
                }
            }
        }
    }

    @Override
    public boolean isActive(String serviceId) {
        logger.debug("check active for serviceId:{}", serviceId);
        checkAndAddStatusCache(serviceId);
        Service.STATUS result = serviceStatusCacheMap.get(serviceId);
        logger.debug("check active for serviceId:{}, status is {} ", serviceId, result);
        return Service.STATUS.ACTIVE.equals(result);
    }

    private void checkAndAddStatusCache(String serviceId) {
        if (StringUtils.isNotBlank(serviceId) && !serviceStatusCacheMap.containsKey(serviceId)) {
            for (String name : serviceListCacheMap.keySet()) {
                for (Service service : serviceListCacheMap.get(name)) {
                    if (serviceId.equals(service.getId())) {
                        serviceStatusCacheMap.put(serviceId, service.getStatus());
                        logger.debug("check add service status cache serviceId:{}, status:{}",
                                service.getId(), service.getStatus());
                    }
                }
            }
        }
    }

    @Override
    public List<Service> getAllByName(String serviceName) {
        logger.debug("get all service by service name:{}", serviceName);
        if (StringUtils.isBlank(serviceName)) {
            return null;
        }
        return serviceListCacheMap.get(serviceName.toUpperCase());
    }

    public void addFileObserver(String filePath) {
        final File file = new File(filePath);
        FileAlterationObserver fao = new FileAlterationObserver(
                file.getParentFile(), new NameFileFilter(file.getName()));
        fao.addListener(new LocalFileAlterationListener());

        try {
            monitor.addObserver(fao);
            logger.debug("file monitor(path:{}) started", filePath);
        } catch (Exception e) {
            logger.error("file monitor(path:{}) start error", filePath, e);
        }
    }

    private class LocalFileAlterationListener extends FileAlterationListenerAdaptor {

        @Override
        public void onFileCreate(final File file) {
            logger.debug("File Created path:{}, length:{}, last modified:{} ",
                    file.getAbsoluteFile(), file.length(), new Date(file.lastModified()));
            updateCacheMap();
        }

        @Override
        public void onFileChange(final File file) {
            logger.debug("File Modified path:{}, length:{}, last modified:{} ",
                    file.getAbsoluteFile(), file.length(), new Date(file.lastModified()));
            updateCacheMap();
        }

    }

    private void updateCacheMap() {
        logger.debug("begin update cache map");
        Map<String, List<Service>> tmpServiceListCacheMap = ymlReader.readAllServices();
        if (tmpServiceListCacheMap == null || tmpServiceListCacheMap.size() == 0) {
            logger.warn("read all services from file result is empty");
            return;
        }

        Map<String, Service.STATUS> tmpServiceStatusCacheMap = Maps.newHashMap();
        Map<String, List<Service>> tmpServiceActiveCacheMap = Maps.newHashMap();
        List<Service> tmpServiceActiveList = null;
        for (String name : tmpServiceListCacheMap.keySet()) {
            tmpServiceActiveList = Lists.newArrayList();
            for (Service service : tmpServiceListCacheMap.get(name)) {
                if (serviceStatusCacheMap.containsKey(service.getId())) {
                    tmpServiceStatusCacheMap.put(service.getId(), service.getStatus());
                    logger.debug("service status cache serviceId:{}, status:{}", service.getId(), service.getStatus());
                }
                if (Service.STATUS.ACTIVE.equals(service.getStatus())) {
                    tmpServiceActiveList.add(service);
                }
            }
            if (serviceActiveCacheMap.containsKey(name) && tmpServiceActiveList.size() > 0) {
                tmpServiceActiveCacheMap.put(name, tmpServiceActiveList);
                logger.debug("service active list cache name:{}, size:{}", name, tmpServiceActiveList.size());
            }
        }

        serviceListCacheMap = tmpServiceListCacheMap;
        serviceStatusCacheMap = tmpServiceStatusCacheMap;
        serviceActiveCacheMap = tmpServiceActiveCacheMap;
        logger.debug("finish update cache map");
    }
}
