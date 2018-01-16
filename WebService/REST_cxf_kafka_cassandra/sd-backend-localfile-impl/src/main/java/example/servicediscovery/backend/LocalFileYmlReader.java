package example.servicediscovery.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import example.foundation.servicediscovery.support.util.Service;

public class LocalFileYmlReader {
    private static Logger logger = LoggerFactory.getLogger(LocalFileYmlReader.class);

    private String ymlFilePath;

    public LocalFileYmlReader(String ymlFilePath) {
        this.ymlFilePath = ymlFilePath;
    }

    public Map<String, List<Service>> readAllServices() {
        logger.debug("begin read all services from path:{}", ymlFilePath);
        Map<String, List<Service>> result = Maps.newHashMap();
        InputStream input = null;
        try {
            Yaml yaml = new Yaml();
            File ymlFile = new File(ymlFilePath);
            if (ymlFile.length() <= 0) {
                logger.debug("finish read all services, file length is {}", ymlFile.length());
                return result;
            }
            input = new FileInputStream(ymlFile);
            Map<String, Object> nameMap = (Map<String, Object>) yaml.load(input);
            List<Object> nodeList = null;
            List<Service> serviceList = null;
            for (String name : nameMap.keySet()) {
                nodeList = (List<Object>) nameMap.get(name);
                serviceList = fetchNodeList(name, nodeList);
                if(serviceList!=null && serviceList.size()>0 && StringUtils.isNotBlank(name)) {
                    result.put(name.toUpperCase(), serviceList);
                }
            }
            logger.debug("finish read all services, total is {}", nameMap.size());
        } catch (FileNotFoundException e) {
            logger.error("Local service discovery file not found on path:{}", ymlFilePath);
        } catch (YAMLException e) {
            logger.error("Local service discovery file yaml format error:{}", e.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.error("Local service discovery file close exception path:{}", ymlFilePath, e);
                }
            }
        }
        return result;
    }

    private List<Service> fetchNodeList(String name, List<Object> nodeList) {
        if (nodeList == null) {
            return null;
        }
        Map<String, Object> nodeMap = null;
        List<Object> serviceIdList = null;
        List<Service> serviceList = Lists.newArrayList();
        for (Object node : nodeList) {
            nodeMap = (Map<String, Object>) node;
            for (String nodeValue : nodeMap.keySet()) {
                serviceIdList = (List<Object>) nodeMap.get(nodeValue);
                fetchServiceIdList(name, nodeValue, serviceIdList, serviceList);
            }
        }
        return serviceList;
    }

    private void fetchServiceIdList(String name, String node, List<Object> serviceIdList, List<Service> serviceList) {
        if (serviceIdList == null) {
            return ;
        }
        Map<String, Object> idMap = null;
        Map<String, Object> propertiesMap = null;
        Service service = null;
        for (Object id : serviceIdList) {
            idMap = (Map<String, Object>) id;
            for (String idValue : idMap.keySet()) {
                propertiesMap = (Map<String, Object>) idMap.get(idValue);
                service = constructService(name, node, idValue, propertiesMap);
                if (service != null) {
                    serviceList.add(service);
                }
            }
        }
    }

    private Service constructService(String name, String node, String serviceId, Map<String, Object> propertiesMap) {
        if (propertiesMap == null) {
            return null;
        }

        String address = (String) propertiesMap.get(LocalFileConstants.ADDRESS);
        Integer port = (Integer) propertiesMap.get(LocalFileConstants.PORT);
        String status = (String) propertiesMap.get(LocalFileConstants.STATUS);
        if (address == null || port == null || status == null) {
            return null;
        }

        Service service = new Service();
        service.setId(serviceId.concat(LocalFileConstants.LINE).concat(node));
        service.setName(name);
        service.setType(LocalFileConstants.TYPE);
        service.setPrefix_URI(address.concat(":").concat(Integer.toString(port)));

        List<String> tags = (List<String>) propertiesMap.get(LocalFileConstants.TAGS);
        if (tags != null) {
            for (String str : tags) {
                int idx = str.indexOf("=");
                if (idx > 0)
                    service.setAttribute(str.substring(0, idx), str.substring(idx + 1));
                else
                    service.setAttribute(str, "");
            }
        }

        if (status.equalsIgnoreCase(LocalFileConstants.STATUS_PASSING)) {
            service.setStatus(Service.STATUS.ACTIVE);
        } else if (status.equalsIgnoreCase(LocalFileConstants.STATUS_WARNING)) {
            service.setStatus(Service.STATUS.BUSY);
        } else {
            service.setStatus(Service.STATUS.DEACTIVE);
        }
        return service;
    }

}


