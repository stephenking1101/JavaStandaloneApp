package example.foundation.servicediscovery.support.cxf;

import java.net.ConnectException;

public class ServiceDiscoveryException extends ConnectException{
    public ServiceDiscoveryException(String serviceName, Throwable cause){
        super("Cannot find instance for service " + serviceName +
                ((cause!=null && cause.getMessage() != null) ? ":" + cause.getMessage() : ""));
        initCause(cause);
    }

}
