package example.foundation.servicediscovery.support;

import java.util.List;

import org.junit.BeforeClass;
import org.mockito.Mockito;

import example.configurationservice.ConfigurationService;
import example.foundation.servicediscovery.ServiceDiscoveryFactory;
import example.foundation.servicediscovery.support.impl.AbstractServiceAddressFinder;
import example.foundation.servicediscovery.support.util.SdFactory;
import example.foundation.servicediscovery.support.util.Service;
import example.foundation.servicediscovery.support.util.ServiceHelper;

import static org.mockito.Mockito.when;

public class AbstractServiceAddressFinderTest {
    private static ConfigurationService configurationServiceMocker = Mockito.mock(ConfigurationService.class);
    private static ClusterDeployment clusterDeploymentMocker = Mockito.mock(ClusterDeployment.class);
    protected static ServiceAddressFinderAdapter finderAdapter = new ServiceAddressFinderAdapter();
    protected static SdFactory sdFactory = Mockito.mock(SdFactory.class);
    protected final String serviceNameNonExistent = "Random-Test-Non-Existent";

    @BeforeClass
    public static void prepare(){
        System.setProperty("SIG_Running_Mode", "Testing");
        ServiceDiscoveryFactory.getMocks().put(ConfigurationService.class, configurationServiceMocker);
        ServiceDiscoveryFactory.getMocks().put(ClusterDeployment.class, clusterDeploymentMocker);
        initClusterDeploymentMocker();
        initMockSdFactory();
        mockFinderType(ServiceAddressFinderAdapter.DEFAULT_FINDER_TYPE);
    }

    protected static void mockFinderType(String type){
        when(configurationServiceMocker.getString(ServiceAddressFinderAdapter.FINDER_TYPE_KEY, ServiceAddressFinderAdapter.DEFAULT_FINDER_TYPE))
                .thenReturn(type);
    }

    private static void initMockSdFactory(){
        AbstractServiceAddressFinder randomFinder =
                (AbstractServiceAddressFinder)ServiceAddressFinderAdapter.getFinder("Random");
        randomFinder.setSdFactory(sdFactory);

        AbstractServiceAddressFinder localOnlyFinder =
                (AbstractServiceAddressFinder)ServiceAddressFinderAdapter.getFinder("LocalOnly");
        localOnlyFinder.setSdFactory(sdFactory);

        AbstractServiceAddressFinder localFirstFinder =
                (AbstractServiceAddressFinder)ServiceAddressFinderAdapter.getFinder("LocalFirst");
        localFirstFinder.setSdFactory(sdFactory);
    }

    protected static void mockSdFactoryRandomInstance(String serviceName, Service service){
        when(sdFactory.getRandomInstance(serviceName)).thenReturn(service);
    }

    protected static void mockSdFactoryRandomInstance(Service service){
        mockSdFactoryRandomInstance(service.getName(), service);
    }

    protected static void mockSdFacotyrAllInstances(String serviceName, List<Service> instaces){
        when(sdFactory.getAllInstances(serviceName)).thenReturn(instaces);
    }

    protected static Service newService(String name, String preFixUri, String serviceUri){
        Service service = new Service();
        service.setName(name);
        service.setPrefix_URI(preFixUri);
        service.setId("app.srv." + name);
        service.setAttribute(ServiceHelper.SERVICE_URI_PROPERTY_NAME, serviceUri);
        service.setStatus(Service.STATUS.ACTIVE);
        return service;
    }

    protected static String INTERNAL_IP = "192.168.0.200";
    protected static String EXTERNAL_IP = "10.175.186.200";
    protected static String HOST_NAME = "host-1";
    protected static String NODE_NAME = "node-1";

    protected static void initClusterDeploymentMocker(){
        Node node = new Node();
        node.setExternalIp(EXTERNAL_IP);
        node.setInternalIp(INTERNAL_IP);
        node.setHostname(HOST_NAME);
        node.setNodename(NODE_NAME);
        when(clusterDeploymentMocker.currentNode()).thenReturn(node);
    }

    protected static final int INJECT_ACTIVE_SERVICE_COUNT = 3;
    protected static final int INJECT_INACTIVE_SERVICE_COUNT = 4;
    protected static void injectServiceInstace(String serviceName, List<Service> services){
        for (int i=0; i<INJECT_ACTIVE_SERVICE_COUNT; i++){
            Service item = newService(serviceName, "192.168.10.1" + i + ":2700",
                    "http://" + ServiceHelper.SERVICE_NAME_KEY + "/active-instance-" + i);
            item.setId(item.getId() + ".active." + i);
            services.add(item);
        }
        for (int i=0; i<INJECT_INACTIVE_SERVICE_COUNT; i++) {
            Service item = newService(serviceName, "192.168.20.1" + i + ":2700",
                    "http://" + ServiceHelper.SERVICE_NAME_KEY + "/inactive-instance-" + i);
            item.setStatus(Service.STATUS.DEACTIVE);
            item.setId(item.getId() + ".inactive." + i);
            services.add(item);
        }
    }
}
