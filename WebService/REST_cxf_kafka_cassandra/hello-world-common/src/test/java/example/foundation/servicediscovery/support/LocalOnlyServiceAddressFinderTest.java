package example.foundation.servicediscovery.support;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import example.foundation.servicediscovery.support.util.Service;
import example.foundation.servicediscovery.support.util.ServiceHelper;

public class LocalOnlyServiceAddressFinderTest extends AbstractServiceAddressFinderTest{
    private String serviceName = "Random-Test";
    private String servicePrefix = INTERNAL_IP + ":2700";
    private String serviceUri = "http://" + ServiceHelper.SERVICE_NAME_KEY + "/" + serviceName;
    private final String localServiceUrl = serviceUri.replace(ServiceHelper.SERVICE_NAME_KEY, "localhost:2700");
    private final String randomServicePrefix = "192.168.11.98:2700";
    private final String localInactiveServiceUrl = "http://localhost:2700/inactive-instance-local";

    List<Service> services = new LinkedList<Service>();
    @Before
    public void mockRandomType(){
        mockFinderType(ServiceAddressFinderAdapter.FINDER_TYPE_LOCAL_ONLY);
        mockSdFactory();
    }

    @Test
    public void testGetUri(){
        String url = finderAdapter.getURI(serviceName);
        Assert.assertEquals(localServiceUrl, url);
    }

    @Test
    public void testGetUriWhenServiceNameNonExistent() {
        String url = finderAdapter.getURI(serviceNameNonExistent);
        Assert.assertNull(url);
    }

    @Test
    public void testGetUriWhenHasInactiveLocalService() {
        addInactiveLocalService();
        String url = finderAdapter.getURI(serviceName);
        Assert.assertEquals(localServiceUrl, url);
    }

    @Test
    public void testGetUriWhenActiveLocalServiceNotExistAndHasDeactiveLocalService() {
        removeActiveLocalService();
        addInactiveLocalService();
        String url = finderAdapter.getURI(serviceName);
        Assert.assertEquals(localInactiveServiceUrl, url);
    }

    @Test
    public void testGetAllActiveUris(){
        List<String> urls = finderAdapter.getAllActivateURIs(serviceName);
        Assert.assertEquals(getMockActiveUrls(), urls);
    }

    @Test
    public void testGetAllActiveUrisWhenServiceNonExistent(){
        List<String> urls = finderAdapter.getAllActivateURIs(serviceNameNonExistent);
        Assert.assertEquals(0, urls.size());
    }

    @Test
    public void testGetAllActiveUrisWhenHasInactiveLocalService(){
        addInactiveLocalService();
        List<String> urls = finderAdapter.getAllActivateURIs(serviceName);
        Assert.assertEquals(getMockActiveUrls(), urls);
    }

    @Test
    public void testGetAllActiveUrisWhenActiveLocalServiceNotExistAndHasDeactiveLocalService() {
        removeActiveLocalService();
        addInactiveLocalService();
        List<String> urls = finderAdapter.getAllActivateURIs(serviceName);
        Assert.assertEquals(1, urls.size());
    }

    @Test
    public void testGetUriWhenBackEndReturnNull() {
        mockSdFacotyrAllInstances("serviceNameForNullList", null);
        Assert.assertEquals(0, finderAdapter.getAllActivateURIs("serviceNameForNullList").size());
        Assert.assertNull(finderAdapter.getURI("serviceNameForNullList"));
    }

    private List<String> getMockActiveUrls() {
        List<String> activeUrls = new ArrayList<String>();
        for (Service service : services) {
            if (service.getStatus().equals(Service.STATUS.ACTIVE) && (service.getPrefix_URI().equals("localhost") || service.getPrefix_URI().equals(servicePrefix))) {
                activeUrls.add(localServiceUrl);
            }
        }
        return activeUrls;
    }

    private void addInactiveLocalService() {
        Service service = newService(serviceName, "localhost:2700", "http://" + ServiceHelper.SERVICE_NAME_KEY + "/inactive-instance-local");
        service.setId(service.getId() + ".inactive-local");
        service.setStatus(Service.STATUS.DEACTIVE);
        services.add(service);
        mockSdFacotyrAllInstances(serviceName, services);
    }

    private void removeActiveLocalService() {
        Iterator<Service> it = services.iterator();
        while (it.hasNext()) {
            Service service = it.next();
            if (service.getPrefix_URI().equals(servicePrefix) && service.getStatus().equals(Service.STATUS.ACTIVE))
                it.remove();

            if (service.getPrefix_URI().equals("localhost") && service.getStatus().equals(Service.STATUS.ACTIVE))
                it.remove();
        }
        mockSdFacotyrAllInstances(serviceName, services);
    }

    private void mockSdFactory(){
        // add random Service
        Service randomService = newService(serviceName, randomServicePrefix, serviceUri);
        mockSdFactoryRandomInstance(randomService);
        services.add(randomService);

        // add local service
        Service localService = newService(serviceName, servicePrefix, serviceUri);
        services.add(localService);

        injectServiceInstace(serviceName, services);
        mockSdFacotyrAllInstances(serviceName, services);
    }
}
