package example.foundation.servicediscovery.support.util;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import example.configurationservice.ConfigurationService;
import example.foundation.servicediscovery.ServiceDiscoveryFactory;
import example.foundation.servicediscovery.support.ClusterDeployment;
import example.foundation.servicediscovery.support.Node;

import static org.mockito.Mockito.when;

public class ServiceHelperTest {
    private final static ClusterDeployment clusterDeploymentMocker = Mockito.mock(ClusterDeployment.class);
    private static ConfigurationService configurationServiceMocker = Mockito.mock(ConfigurationService.class);
    private final String serviceName = "Random-Test";
    private final String preFixUri = "localhost:2700";
    private String serviceUri = "http://" + ServiceHelper.SERVICE_NAME_KEY + "/" + serviceName;

    @BeforeClass
    public static void prepare(){
        ServiceDiscoveryFactory.getMocks().put(ConfigurationService.class, configurationServiceMocker);
        ServiceDiscoveryFactory.getMocks().put(ClusterDeployment.class, clusterDeploymentMocker);
        initClusterDeploymentMocker();
    }

    private final static String INTERNAL_IP = "192.168.0.200";
    private final static String EXTERNAL_IP = "10.175.186.200";
    private final static String HOST_NAME = "host-1";
    private final static String NODE_NAME = "node-1";
    public static void initClusterDeploymentMocker(){
        Node node = new Node();
        node.setExternalIp(EXTERNAL_IP);
        node.setInternalIp(INTERNAL_IP);
        node.setHostname(HOST_NAME);
        node.setNodename(NODE_NAME);
        when(clusterDeploymentMocker.currentNode()).thenReturn(node);
    }

    @Test
    public void testIsLocalService() {
        Service service = newService(serviceName, preFixUri, serviceUri);
        Assert.assertTrue(ServiceHelper.isLocalService(service));
    }

    @Test
    public void testIsLocalServiceNull() {
        Assert.assertFalse(ServiceHelper.isLocalService(null));
    }

    @Test
    public void testIsLocalServiceHasNoLocalService() {
        Service service = newService(serviceName, "192.168.0.1:8080", serviceUri);
        Assert.assertFalse(ServiceHelper.isLocalService(service));
    }

    @Test
    public void testGetServiceUri() {
        Service service = newService(serviceName, preFixUri, serviceUri);
        String strUri = ServiceHelper.getServiceUri(service);
        Assert.assertEquals(serviceUri.replace(ServiceHelper.SERVICE_NAME_KEY, preFixUri), strUri);
    }

    @Test
    public void testGetServiceUriNull() {
        String strUri = ServiceHelper.getServiceUri(null);
        Assert.assertNull(serviceUri.replace(ServiceHelper.SERVICE_NAME_KEY, preFixUri), strUri);
    }

    @Test
    public void testReplaceServiceUri() {
        String strUri = ServiceHelper.replaceServiceUri(serviceUri, preFixUri);
        Assert.assertEquals("http://" + preFixUri + "/" + serviceName, strUri);
    }

    @Test
    public void testReplaceServiceUriNull() {
        String strUri = ServiceHelper.replaceServiceUri(null, preFixUri);
        Assert.assertEquals(preFixUri, strUri);
    }

    private static Service newService(String name, String preFixUri, String serviceUri){
        Service service = new Service();
        service.setName(name);
        service.setPrefix_URI(preFixUri);
        service.setId("app.srv." + name);
        service.setAttribute(ServiceHelper.SERVICE_URI_PROPERTY_NAME, serviceUri);
        service.setStatus(Service.STATUS.ACTIVE);
        return service;
    }
}
