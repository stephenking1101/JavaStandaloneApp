package example.servicediscovery.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import example.foundation.servicediscovery.support.util.Service;

public class LocalFileBackendMonitorTest {

    private static String ymlFileName = "monitor.yml";
    private static String ymlFileName2 = "monitor2.yml";
    private static String ymlFilePath;
    private static String ymlFilePath2;
    private static String servicesYmlFilePath;
    private static String servicesChangeYmlFilePath;
    private static String servicesChange2YmlFilePath;

    @BeforeClass
    public static void init() throws InterruptedException {
        servicesYmlFilePath = LocalFileBackendMonitorTest.class.getClassLoader().getResource("services.yml").getPath();
        servicesChangeYmlFilePath = LocalFileBackendMonitorTest.class.getClassLoader().getResource("servicesChange.yml").getPath();
        servicesChange2YmlFilePath = LocalFileBackendMonitorTest.class.getClassLoader().getResource("servicesChange2.yml").getPath();
        File srcYmlFile = new File(servicesYmlFilePath);
        ymlFilePath = srcYmlFile.getParent() + "/" + ymlFileName;
        ymlFilePath2 = srcYmlFile.getParent() + "/" + ymlFileName2;
    }

    @Test
    public void testNoFileCreateChangeDeleteFile() throws InterruptedException, IOException {
        //init no file
        FileUtils.deleteQuietly(new File(ymlFilePath));
        LocalFileBackendImpl backend1 = new LocalFileBackendImpl(ymlFilePath);
        assertNull(backend1.getAllByName("EXAMPLE-SINGLE"));
        assertNull(backend1.getAllByName("example_service"));

        //Create
        FileUtils.copyFile(new File(servicesYmlFilePath), new File(ymlFilePath));
        Thread.sleep(2000L);
        checkServiceInfo(backend1, "192.168.137.186:27020");

        //Change
        FileUtils.copyFile(new File(servicesChangeYmlFilePath), new File(ymlFilePath));
        Thread.sleep(2000L);
        checkServiceInfo(backend1, "1.1.1.1:27020");

        //Delete
        Boolean result = FileUtils.deleteQuietly(new File(ymlFilePath));
        assertTrue(result);
        Thread.sleep(2000L);
        checkServiceInfo(backend1, "1.1.1.1:27020");
    }

    @Test
    public void testTwoBackendMonitorOneFile() throws InterruptedException, IOException {
        //init no file
        FileUtils.deleteQuietly(new File(ymlFilePath));
        LocalFileBackendImpl backend1 = new LocalFileBackendImpl(ymlFilePath);
        LocalFileBackendImpl backend2 = new LocalFileBackendImpl(ymlFilePath);

        assertNull(backend1.getAllByName("EXAMPLE-SINGLE"));
        assertNull(backend1.getAllByName("example_service"));
        assertNull(backend2.getAllByName("EXAMPLE-SINGLE"));
        assertNull(backend2.getAllByName("example_service"));

        //Create
        FileUtils.copyFile(new File(servicesYmlFilePath), new File(ymlFilePath));
        Thread.sleep(2000L);
        checkServiceInfo(backend1, "192.168.137.186:27020");
        checkServiceInfo(backend2, "192.168.137.186:27020");

        //Change
        FileUtils.copyFile(new File(servicesChangeYmlFilePath), new File(ymlFilePath));
        Thread.sleep(2000L);
        checkServiceInfo(backend1, "1.1.1.1:27020");
        checkServiceInfo(backend2, "1.1.1.1:27020");

        //Delete
        Boolean result = FileUtils.deleteQuietly(new File(ymlFilePath));
        assertTrue(result);
        Thread.sleep(2000L);
        checkServiceInfo(backend1, "1.1.1.1:27020");
        checkServiceInfo(backend2, "1.1.1.1:27020");
    }

    @Test
    public void testTwoBackendMonitorDifferentFile() throws InterruptedException, IOException {
        //init no file
        FileUtils.deleteQuietly(new File(ymlFilePath));
        FileUtils.deleteQuietly(new File(ymlFilePath2));
        LocalFileBackendImpl backend1 = new LocalFileBackendImpl(ymlFilePath);
        LocalFileBackendImpl backend2 = new LocalFileBackendImpl(ymlFilePath2);

        assertNull(backend1.getAllByName("EXAMPLE-SINGLE"));
        assertNull(backend1.getAllByName("example_service"));
        assertNull(backend2.getAllByName("EXAMPLE-SINGLE"));
        assertNull(backend2.getAllByName("example_service"));

        //Create
        FileUtils.copyFile(new File(servicesYmlFilePath), new File(ymlFilePath));
        FileUtils.copyFile(new File(servicesYmlFilePath), new File(ymlFilePath2));
        Thread.sleep(2000L);
        checkServiceInfo(backend1, "192.168.137.186:27020");
        checkServiceInfo(backend2, "192.168.137.186:27020");

        //Change
        FileUtils.copyFile(new File(servicesChangeYmlFilePath), new File(ymlFilePath));
        FileUtils.copyFile(new File(servicesChange2YmlFilePath), new File(ymlFilePath2));
        Thread.sleep(2000L);
        checkServiceInfo(backend1, "1.1.1.1:27020");
        checkServiceInfo(backend2, "2.2.2.2:27020");

        //Delete
        Boolean result = FileUtils.deleteQuietly(new File(ymlFilePath));
        assertTrue(result);
        Boolean result2 = FileUtils.deleteQuietly(new File(ymlFilePath2));
        assertTrue(result2);
        Thread.sleep(2000L);
        checkServiceInfo(backend1, "1.1.1.1:27020");
        checkServiceInfo(backend2, "2.2.2.2:27020");
    }

    private void checkServiceInfo(LocalFileBackendImpl backend, String oauthAddress) {
        assertNotNull(backend.getAllByName("EXAMPLE-SINGLE"));
        assertEquals(3, backend.getAllByName("EXAMPLE-SINGLE").size());
        assertNotNull(backend.getAllByName("example_service"));
        assertEquals(1, backend.getAllByName("example_service").size());
        Service oauth = backend.getAllByName("example_service").get(0);
        assertEquals("example_service_node-1", oauth.getId());
        assertEquals("example_service", oauth.getName());
        assertEquals(oauthAddress, oauth.getPrefix_URI());
        assertEquals(Service.STATUS.ACTIVE, oauth.getStatus());
        assertEquals(LocalFileConstants.TYPE, oauth.getType());
    }
}
