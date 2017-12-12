package example.foundation.servicediscovery.support.cxf;

import org.apache.cxf.message.Message;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

import java.io.IOException;

public class ServiceDiscoveryFailedConduit implements Conduit {
    private Throwable cause;
    private String serviceName;

    public ServiceDiscoveryFailedConduit(String errorMessage, Throwable cause){
        this.serviceName = errorMessage;
        this.cause = cause;
    }

    @Override
    public void prepare(Message message) throws IOException {
    }

    @Override
    public void close(Message message) throws IOException {
        throw new ServiceDiscoveryException(serviceName, cause);
    }

    @Override
    public EndpointReferenceType getTarget() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public void setMessageObserver(MessageObserver observer) {

    }

    @Override
    public MessageObserver getMessageObserver() {
        return null;
    }
}
