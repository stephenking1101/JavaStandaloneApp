package org.apache.cxf.clustering;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.clustering.FailoverStrategy;
import org.apache.cxf.clustering.FailoverTargetSelector;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.Conduit;

import example.foundation.servicediscovery.support.AddressProvider;
import example.foundation.servicediscovery.support.cxf.ServiceDiscoveryFailedConduit;
import example.foundation.servicediscovery.support.util.ConfigurationException;
import example.foundation.servicediscovery.support.util.NoServiceException;

public class SdLbsTargetSelector extends FailoverTargetSelector {
    private static final Logger LOG =
            LogUtils.getL7dLogger(FailoverTargetSelector.class);
    private AddressProvider addressProvider = null;
    
    private static final String IS_DISTRIBUTED = 
            "org.apache.cxf.clustering.VnfsdLbsTargetSelector.IS_DISTRIBUTED";
    private static final String DISTRIBUTED_ADDRESS = 
            "org.apache.cxf.clustering.VnfsdLbsTargetSelector.DISTRIBUTED_ADDRESS";
    private boolean failoverFlag = true;
    
    public synchronized void setAddressProvider(AddressProvider addressProvider){
        this.addressProvider = addressProvider;
    }
    
    public synchronized AddressProvider getAddressProvider(){
        return addressProvider;
    }
    
    /**
     * @return strategy the FailoverStrategy to use
     */    
    @Override
    public synchronized FailoverStrategy getStrategy()  {
        if (failoverStrategy == null) {
            failoverStrategy = new SdFailoverStrategy();
            getLogger().log(Level.INFO,
                            "USING_STRATEGY",
                            new Object[] {failoverStrategy});
        }
        return failoverStrategy;
    }

    
    /**
     * Use VnfsdFailoverStrategy Strategy instead default one. 
     */
    protected Endpoint getFailoverTarget(Exchange exchange,
                                       InvocationContext invocation) {
        List<String> alternateAddresses = null;
        String distributedAddress = (String)invocation.getContext().remove(DISTRIBUTED_ADDRESS);
        if (distributedAddress == null || distributedAddress.isEmpty()){
            alternateAddresses = invocation.getAlternateAddresses();
        }else{
            // no previous failover attempt on this invocation
            invocation.setAlternateAddresses(null);
            invocation.setAlternateEndpoints(null);
            
            try {
                alternateAddresses = getAddressProvider().getFailoverAddresses(distributedAddress);
            }catch (RuntimeException ex){
                LOG.warning(ex.getMessage());
            }

            LOG.fine("failover address list get for service " + addressProvider.getServiceName()
                    + ": " + alternateAddresses);
            if (alternateAddresses != null) {
                invocation.setAlternateAddresses(alternateAddresses);
            }
        }

        Endpoint failoverTarget = null;
        if (alternateAddresses != null) {
            String alternateAddress = 
                    getStrategy().selectAlternateAddress(alternateAddresses);
            LOG.fine("failover address " + alternateAddress + " selected for service " + addressProvider.getServiceName());
            if (alternateAddress != null) {
                // re-use current endpoint
                //
                failoverTarget = getEndpoint();
                failoverTarget.getEndpointInfo().setAddress(alternateAddress);
            }
        }
        return failoverTarget;
    }
    
    /**
     * Called when a Conduit is actually required.
     *
     * @param message
     * @return the Conduit to use for mediation of the message
     */
    public synchronized Conduit selectConduit(Message message) {
        Conduit c = message.get(Conduit.class);
        if (c != null) {
            return c;
        }

        Exchange exchange = message.getExchange();
        InvocationKey key = new InvocationKey(exchange);
        InvocationContext invocation = inProgress.get(key);
        if ((invocation != null) && !invocation.getContext().containsKey(IS_DISTRIBUTED)) {
            Endpoint target = null;

            try {
                target = getDistributionTarget(exchange, invocation);
            }catch(ConfigurationException e){
                LOG.warning(e.getClass().getSimpleName() + ":" + e.getMessage());
                return new ServiceDiscoveryFailedConduit(e.getServiceName(), e);
            }catch(NoServiceException e){
                LOG.warning(e.getClass().getSimpleName() + ":" + e.getMessage());
                return new ServiceDiscoveryFailedConduit(e.getServiceName(), e);
            }

            if (target != null) {
                setEndpoint(target);
                message.put(Message.ENDPOINT_ADDRESS, target.getEndpointInfo().getAddress());
                overrideAddressProperty(invocation.getContext());
                invocation.getContext().put(IS_DISTRIBUTED, null);
            }
        }
        message.put(CONDUIT_COMPARE_FULL_URL, Boolean.TRUE);
        return getSelectedConduit(message);
    }

    
    /**
     * Get the distribution target endpoint, if a suitable one is available.
     *
     * @param exchange the current Exchange
     * @param invocation the current InvocationContext
     * @return a distribution endpoint if one is available
     */
    private Endpoint getDistributionTarget(Exchange exchange,
                                           InvocationContext invocation) {
        String alternateAddress = addressProvider.getAddress();

        LOG.fine("get distribute address " + alternateAddress + " for service " + addressProvider.getServiceName());

        Endpoint distributionTarget = null;
        if (alternateAddress != null && !alternateAddress.isEmpty()){
            distributionTarget = getEndpoint();
            distributionTarget.getEndpointInfo().setAddress(alternateAddress);
            invocation.getContext().put(DISTRIBUTED_ADDRESS, alternateAddress);
        }
        return distributionTarget;
    }
    
    // Involve the CXF Bug Fix.
    // Need to be removed after CXF upgrade.
    //  - CXF Bug:       https://issues.apache.org/jira/browse/CXF-5184
    //  -Submit:         https://github.com/apache/cxf/commit/351659b7be63b7e16f18b9332aa473bb98132c36
    //  - Fixed version: 2.6.10, 2.7.7, 3.0.0-milestone1
    @Override
    protected boolean replaceEndpointAddressPropertyIfNeeded(Message message,
                                                             String endpointAddress,
                                                             Conduit cond) {
        String requestURI = (String)message.get(Message.REQUEST_URI);
        if (requestURI != null && endpointAddress != null && !requestURI.equals(endpointAddress)) {
            String basePath = (String)message.get(Message.BASE_PATH);
            if (basePath != null && requestURI.startsWith(basePath)) {
                String pathInfo = requestURI.substring(basePath.length());
                message.put(Message.BASE_PATH, endpointAddress);
                final String slash = "/";
                boolean startsWithSlash = pathInfo.startsWith(slash);
                if (endpointAddress.endsWith(slash)) {
                    endpointAddress = endpointAddress + (startsWithSlash ? pathInfo.substring(1) : pathInfo);
                } else {
                    endpointAddress = endpointAddress + (startsWithSlash ? pathInfo : (slash + pathInfo));
                }
                message.put(Message.ENDPOINT_ADDRESS, endpointAddress);

                Exchange exchange = message.getExchange();
                InvocationKey key = new InvocationKey(exchange);
                InvocationContext invocation = inProgress.get(key);
                if (invocation != null) {
                    overrideAddressProperty(invocation.getContext(),
                            cond.getTarget().getAddress().getValue());
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean requiresFailover(Exchange exchange, Exception ex) {
        return failoverFlag && super.requiresFailover(exchange, ex);
    }
}
