package example.servicediscovery.backend;

import static org.junit.Assert.assertEquals;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import example.foundation.servicediscovery.support.util.Service;

public class LocalFileYmlReaderTest {

    @Test
    public void testReadAllServicesSuccess(){
        String ymlFilePath = getClass().getClassLoader().getResource("services.yml").getPath();
        LocalFileYmlReader ymlReader = new LocalFileYmlReader(ymlFilePath);
        Map<String, List<Service>> result = ymlReader.readAllServices();
        assertEquals(3, result.size());
        assertEquals(3, result.get("EXAMPLE-SINGLE").size());
        assertEquals(3, result.get("iam_idm".toUpperCase()).size());
        assertEquals(1, result.get("example_service".toUpperCase()).size());
        Service oauth = result.get("example_service".toUpperCase()).get(0);
        assertEquals("example_service_node-1", oauth.getId());
        assertEquals("example_service", oauth.getName());
        assertEquals("192.168.137.186:27020", oauth.getPrefix_URI());
        assertEquals(Service.STATUS.ACTIVE, oauth.getStatus());
        assertEquals(LocalFileConstants.TYPE, oauth.getType());
    }

    @Test
    public void testReadNotExistsFile(){
        String ymlFilePath = "not/exists/path";
        LocalFileYmlReader ymlReader = new LocalFileYmlReader(ymlFilePath);
        Map<String, List<Service>> result = ymlReader.readAllServices();
        assertEquals(result.size(), 0);
    }

    @Test
    public void testReadNotContentsFile(){
        String ymlFilePath = getClass().getClassLoader().getResource("services0.yml").getPath();
        LocalFileYmlReader ymlReader = new LocalFileYmlReader(ymlFilePath);
        Map<String, List<Service>> result = ymlReader.readAllServices();
        assertEquals(result.size(), 0);
    }


    @Test
    public void testLostProperties(){
        String ymlFilePath = getClass().getClassLoader().getResource("lostproperties.yml").getPath();
        LocalFileYmlReader ymlReader = new LocalFileYmlReader(ymlFilePath);
        Map<String, List<Service>> result = ymlReader.readAllServices();
        assertEquals(1, result.size());
        assertEquals(1, result.get("example_service".toUpperCase()).size());
        Service oauth = result.get("example_service".toUpperCase()).get(0);
        assertEquals("example_service_node-1", oauth.getId());
        assertEquals("example_service", oauth.getName());
        assertEquals("192.168.137.186:27020", oauth.getPrefix_URI());
        assertEquals(Service.STATUS.ACTIVE, oauth.getStatus());
        assertEquals(LocalFileConstants.TYPE, oauth.getType());
    }

    @Test
    public void testYmlFormatNotThrowException(){
        String ymlFilePath = getClass().getClassLoader().getResource("ymlformaterror.yml").getPath();
        LocalFileYmlReader ymlReader = new LocalFileYmlReader(ymlFilePath);
        ymlReader.readAllServices();
    }


}


