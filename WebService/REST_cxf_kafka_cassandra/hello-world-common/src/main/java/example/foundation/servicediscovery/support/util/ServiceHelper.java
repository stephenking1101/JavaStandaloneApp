package example.foundation.servicediscovery.support.util;

import org.apache.cxf.common.util.StringUtils;

public class ServiceHelper {
    public static final String SERVICE_URI_PROPERTY_NAME = "service_uri";
    public static final String SERVICE_NAME_KEY = "{host}:{port}";
    public static boolean isActive(Service service){
        return (service != null) &&
                (service.getStatus() == Service.STATUS.ACTIVE);
    }

    public static boolean isLocalService(Service service){
        if (service == null){
            return false;
        }
        return LocalAddressHelper.isLocalIP(service.getPrefix_URI());
    }

    public static String getServiceUri(Service service){
        if (service == null){
            return null;
        }
        return replaceServiceUri(service.getAttribute(SERVICE_URI_PROPERTY_NAME), service.getPrefix_URI());
    }

    public static String replaceServiceUri(String serviceUri, String preFixUri){
        if (StringUtils.isEmpty(serviceUri)){
            return preFixUri;
        }

        if (serviceUri.contains(SERVICE_NAME_KEY)){
            return serviceUri.replace(SERVICE_NAME_KEY, preFixUri);
        }else{
            return serviceUri;
        }
    }
}
