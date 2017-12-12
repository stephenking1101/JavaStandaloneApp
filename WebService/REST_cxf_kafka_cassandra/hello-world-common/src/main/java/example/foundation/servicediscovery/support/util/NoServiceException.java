package example.foundation.servicediscovery.support.util;

public class NoServiceException  extends  RuntimeException{
    private String serviceName;

    public NoServiceException(String message, String serviceName){
        super(message);
        this.serviceName = serviceName;
    }

    public NoServiceException(String message, String serviceName, Throwable cause){
        super(message, cause);
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " for service :" + serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
