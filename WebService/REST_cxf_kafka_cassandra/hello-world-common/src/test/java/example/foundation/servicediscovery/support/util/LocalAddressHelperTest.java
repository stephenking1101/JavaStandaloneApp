package example.foundation.servicediscovery.support.util;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import example.configurationservice.ConfigurationService;
import example.foundation.servicediscovery.ServiceDiscoveryFactory;
import example.foundation.servicediscovery.support.ClusterDeployment;
import example.foundation.servicediscovery.support.Node;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

public class LocalAddressHelperTest {
    private final static ClusterDeployment clusterDeploymentMocker = Mockito.mock(ClusterDeployment.class);
    private static ConfigurationService configurationServiceMocker = Mockito.mock(ConfigurationService.class);

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
    public void testIsLocalIPSuccess() {
        String strURI = "localhost:2700/Random-Test";
        Assert.assertTrue(LocalAddressHelper.isLocalIP(strURI));
    }

    @Test
    public void testIsLocalIPDoubleColonBefore() {
        String strURI = "http://localhost:2700/Random-Test";
        Assert.assertFalse(LocalAddressHelper.isLocalIP(strURI));
    }

    @Test
    public void testIsLocalIPDoubleColonAfter() {
        String strURI = "localhost:2700/Random-Test:8080";
        Assert.assertTrue(LocalAddressHelper.isLocalIP(strURI));
    }

    @Test
    public void testIsLocalIPWhenIsNotLocalIp() {
        String strURI = "192.168.0.1:2700/Random-Test:8080";
        Assert.assertFalse(LocalAddressHelper.isLocalIP(strURI));
    }

    @Test
    public void testIsLocalIPNull() {
        String strURI = null;
        Assert.assertFalse(LocalAddressHelper.isLocalIP(strURI));
    }

    @Test
    public void testReplaceServiceUri() {
        String strURI = "192.168.0.1:2700/Random-Test";
        Assert.assertEquals("localhost:2700/Random-Test", LocalAddressHelper.covertToLocalIpUri(strURI));
    }

    @Test
    public void testReplaceServiceUri_null() {
        String strURI = null;
        Assert.assertNull(LocalAddressHelper.covertToLocalIpUri(strURI));
    }

    @Test
    public void testReplaceServiceUri_localUri() {
        String strURI = "localhost";
        Assert.assertEquals(strURI, LocalAddressHelper.covertToLocalIpUri(strURI));
    }

}