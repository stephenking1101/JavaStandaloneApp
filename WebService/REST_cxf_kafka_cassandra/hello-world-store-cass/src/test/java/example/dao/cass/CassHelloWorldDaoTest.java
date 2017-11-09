package example.dao.cass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import example.dao.common.AbstractDaoTestCase;
import example.service.payload.HelloWorld;

import java.util.List;

public class CassHelloWorldDaoTest extends AbstractDaoTestCase {
    public static final Long STORE_TIMESTAMP = 1645728846252L;

    @Autowired
    private CassHelloWorldDao helloWorldDao;

    @Test
    public void testGetNotExist() {
        String userName="NotExist_u_1001";
        HelloWorld helloWorld = helloWorldDao.get(userName, STORE_TIMESTAMP);
        assertNull(helloWorld);
    }

    @Test
    public void testGet() {
        String userId="get_u_1001";
        HelloWorld helloWorld = helloWorldDao.get(userId, STORE_TIMESTAMP);
        assertNotNull(helloWorld);
        assertNotNull(helloWorld.getExtension());
        assertEquals(1001, helloWorld.getExtension().get("v_int"));
        assertEquals(true, helloWorld.getExtension().get("v_bool"));
        assertEquals("test_get", helloWorld.getExtension().get("v_str"));
    }

    @Test
    public void testCreate() {
        String uName = "get_u_1002";
        HelloWorld helloWorld = new HelloWorld();
        helloWorld.setUserName(uName);
        helloWorld.setTimestamp(STORE_TIMESTAMP);

        helloWorld.setExtension("v_int", 123);
        helloWorld.setExtension("v_bool", true);
        helloWorld.setExtension("v_str", "");
        helloWorldDao.create(helloWorld);

        HelloWorld result = helloWorldDao.get(uName, STORE_TIMESTAMP);
        assertNotNull(result);
        assertEquals(helloWorld.getUserName(), result.getUserName());
        assertEquals(helloWorld.getTimestamp(), result.getTimestamp());

        assertNotNull(result.getExtension());
        assertEquals(helloWorld.getExtension().get("v_int"), result.getExtension().get("v_int"));
        assertEquals(helloWorld.getExtension().get("v_bool"), result.getExtension().get("v_bool"));
        assertEquals(helloWorld.getExtension().get("v_str"), result.getExtension().get("v_str"));
    }

    @Test
    public void testDelete() {
        String uName = "get_u_1003";
        helloWorldDao.delete(uName, STORE_TIMESTAMP);

        assertNull(helloWorldDao.get(uName, STORE_TIMESTAMP));
    }

    @Test
    public void testQueryByUserName() {
        String userName="get_u_1001";

        List<HelloWorld> helloWorldList = helloWorldDao.queryByUserName(userName);

        assertNotNull(helloWorldList);
        assertEquals(2, helloWorldList.size());
        for(HelloWorld helloWorld : helloWorldList){
            assertEquals(userName, helloWorld.getUserName());
            assertNotNull(helloWorld.getExtension());
            assertEquals("test_get", helloWorld.getExtension().get("v_str"));
        }
    }

    @Test
    public void testDeleteByUserName() {
        String userName="get_u_1004";

        helloWorldDao.deleteByUserName(userName);

        List<HelloWorld> helloWorldList = helloWorldDao.queryByUserName(userName);
        assertNotNull(helloWorldList);
        assertEquals(0, helloWorldList.size());
    }
}
