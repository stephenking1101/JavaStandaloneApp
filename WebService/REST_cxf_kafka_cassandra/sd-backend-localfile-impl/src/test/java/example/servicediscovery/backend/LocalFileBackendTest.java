package example.servicediscovery.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import example.foundation.servicediscovery.support.util.Service;

public class LocalFileBackendTest {

    private static LocalFileBackendImpl backend;

    @BeforeClass
    public static void setup() throws InterruptedException {
        String ymlFilePath = LocalFileBackendTest.class.getClassLoader().getResource("services.yml").getPath();
        backend = new LocalFileBackendImpl(ymlFilePath);
    }

    @Test
    public void testGetServiceUriSuccess(){
        List<Service> services = backend.getAllByName("example_service");
        for (Service service:services){
            String service_uri = service.getAttribute("service_uri");
            assertEquals("http://{host}:{port}/test/", service_uri);
        }
    }

    @Test
    public void testGetServiceUriNull(){
        List<Service> services = backend.getAllByName("EXAMPLE-SINGLE");
        for (Service service:services){
            String service_uri = service.getAttribute("service_uri");
            assertEquals(null, service_uri);
        }
    }

    @Test
    public void testGetAllByName(){
        assertNotNull(backend.getAllByName("EXAMPLE-SINGLE"));
        assertEquals(3, backend.getAllByName("EXAMPLE-SINGLE").size());
        assertNotNull(backend.getAllByName("example_service"));
        assertEquals(1, backend.getAllByName("example_service").size());
        Service oauth = backend.getAllByName("example_service").get(0);
        assertEquals("example_service_node-1", oauth.getId());
        assertEquals("example_service", oauth.getName());
        assertEquals("192.168.137.186:27020", oauth.getPrefix_URI());
        assertEquals(Service.STATUS.ACTIVE, oauth.getStatus());
        assertEquals(LocalFileConstants.TYPE, oauth.getType());
    }

    @Test
    public void testGetOneActiveInstanceSuccess(){
        Service result = backend.getOneActiveInstance("EXAMPLE-SINGLE");
        assertNotNull(result);
        assertEquals(Service.STATUS.ACTIVE, result.getStatus());
    }

    @Test
    public void testGetOneActiveInstanceWhenNoActive(){
        Service result = backend.getOneActiveInstance("iam_idm");
        assertNull(result);
    }

    @Test
    public void testGetOneActiveInstanceWhenNotExists(){
        Service result = backend.getOneActiveInstance("iam_abc");
        assertNull(result);
    }

    @Test
    public void testIsActiveWhenTrue(){
        Boolean result = backend.isActive("example_service_node-1");
        assertTrue(result);
    }

    @Test
    public void testIsActiveWhenFalse(){
        Boolean result = backend.isActive("iam-idm_node-1");
        assertFalse(result);
    }

    @Test
    public void testIsActiveWhenNotExists(){
        Boolean result = backend.isActive("iam-abc_node-1");
        assertFalse(result);
    }


}
