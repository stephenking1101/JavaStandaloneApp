package example.foundation.servicediscovery.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import example.foundation.servicediscovery.support.util.ServiceHelper;
import example.util.JacksonMapConvertor;

public class ServiceDefinition {
    public interface NAME{
        String Test_Service = "test_service";
    }

    private static String filePath = "classpath:service_list.json";
    private static Logger logger = LoggerFactory.getLogger(ServiceDefinition.class);
    private static ResourceLoader resourceLoader = new DefaultResourceLoader();
    private static Map<String, ServiceDefinition> services = new HashMap<String, ServiceDefinition>();

    static{
        String content = readFile(filePath);
        Map<String, Object> serviceMap = JacksonMapConvertor.jsonStringToObject(content, Map.class);
        for (String name : serviceMap.keySet()){
            Map<String, String> params = (Map<String, String>)serviceMap.get(name);
            ServiceDefinition service = new ServiceDefinition(name);
            service.setDpName(params.get("dp_name"));
            service.setServiceUri(params.get("service_uri"));
            service.setHealthCheckPostFix(params.get("healthcheck_postfix"));
            services.put(name, service);
        }
    }

    private String name;
    private String dpName;
    private String serviceUri;
    protected String healthCheckPostFix;

    private ServiceDefinition(String name){
        this.name = name;
    }

    protected ServiceDefinition(ServiceDefinition serviceDef){
        if (serviceDef != null) {
            this.name = serviceDef.name;
            this.dpName = serviceDef.dpName;
            this.serviceUri = serviceDef.serviceUri;
            this.healthCheckPostFix = serviceDef.healthCheckPostFix;
        }
    }

    public void setServiceUri(String serviceUri){
        this.serviceUri = serviceUri.trim();
        if (this.serviceUri.endsWith("/")){
            this.serviceUri.substring(0, this.serviceUri.length()-1);
        }
    }

    public void setServiceUriWithContext(String context){
        String tmp = context.trim();
        while (tmp.startsWith("/")){
            tmp = tmp.substring(1);
        }
        while (tmp.endsWith("/")){
            tmp = tmp.substring(0, tmp.length()-1);
        }
        this.serviceUri = "http://".concat(ServiceHelper.SERVICE_NAME_KEY).concat("/").concat(tmp);
    }

    public String getPreFix(){
        return serviceUri.substring(0, serviceUri.indexOf(ServiceHelper.SERVICE_NAME_KEY));
    }
    public String getContext(){
        return serviceUri.substring(serviceUri.indexOf(ServiceHelper.SERVICE_NAME_KEY) + ServiceHelper.SERVICE_NAME_KEY.length());
    }

    public String getServiceUriFormat(){
        return serviceUri;
    }

    public String getServiceName(){
        return name;
    }

    public void setServiceName(String name){
        this.name = name;
    }

    public void setDpName(String dpName){
        this.dpName = dpName;
    }

    public String getDpName(){
        return dpName;
    }

    public void setHealthCheckPostFix(String healthCheckPostFix){
        this.healthCheckPostFix = healthCheckPostFix.trim();
        if (!this.healthCheckPostFix.startsWith("/")) {
            ;
        }{
            this.healthCheckPostFix = "/" + this.healthCheckPostFix;
        }
    }

    public String getHealthCheckUri(){
        return serviceUri + healthCheckPostFix;
    }

    private static String readFile(String path) {
        Resource resource = resourceLoader.getResource(path);
        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                buffer.append(tempString);
            }
        } catch (IOException e) {
            logger.error("{} read fail:", path, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("{} close fail:", path, e);
                }
            }
        }
        return buffer.toString();
    }

    @Override
    protected Object clone(){
        ServiceDefinition service = new ServiceDefinition(this.name);
        service.dpName = this.dpName;
        service.serviceUri = this.serviceUri;
        service.healthCheckPostFix = this.healthCheckPostFix;
        return service;
    }

    public static ServiceDefinition getInstance(String serviceName){
        ServiceDefinition result = services.get(serviceName);
        if (result != null){
            result = new ServiceDefinition(result);
        }
        return result;
    }
}

