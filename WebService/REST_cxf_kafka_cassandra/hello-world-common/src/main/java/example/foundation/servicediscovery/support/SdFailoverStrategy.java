package example.foundation.servicediscovery.support;

import java.util.List;

import org.apache.cxf.clustering.FailoverStrategy;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;

public class SdFailoverStrategy implements FailoverStrategy{
    private AddressSelectStrategy failoverAddressSelectStrategy = new RandomStrategy();

    @Override
    public List<Endpoint> getAlternateEndpoints(Exchange exchange) {
        return null;
    }

    @Override
    public Endpoint selectAlternateEndpoint(List<Endpoint> alternates) {
        return null;
    }

    @Override
    public List<String> getAlternateAddresses(Exchange exchange) {
        return null;
    }

    @Override
    public String selectAlternateAddress(List<String> addresses) {
        return failoverAddressSelectStrategy.getNextAlternate(addresses);
    }

}