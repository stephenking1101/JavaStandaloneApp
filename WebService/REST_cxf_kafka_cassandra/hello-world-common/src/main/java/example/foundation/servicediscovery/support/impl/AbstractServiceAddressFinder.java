package example.foundation.servicediscovery.support.impl;

import example.foundation.servicediscovery.support.ServiceAddressFinder;
import example.foundation.servicediscovery.support.util.SdFactory;
import example.foundation.servicediscovery.support.util.Service;

public abstract class AbstractServiceAddressFinder implements ServiceAddressFinder {
    private SdFactory sdFactory;

    public void setSdFactory(SdFactory sdFactory){
        this.sdFactory = sdFactory;
    }

    public SdFactory getSdFactory(){
        if (sdFactory == null){
            sdFactory = new SdFactory();
        }
        return sdFactory;
    }

    protected Service getRandomService(String serviceName){
        return getSdFactory().getRandomInstance(serviceName);
    }
}
