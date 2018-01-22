package example.configurationservice.local.ct;

import java.util.NoSuchElementException;

import javax.ws.rs.NotFoundException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

import example.configurationservice.ConfigurationService;
import example.foundation.servicediscovery.ServiceDiscovery;
import example.foundation.servicediscovery.ServiceDiscoveryFactory;


@ActiveProfiles("ct-yml")
public class ConfigurationLocalServiceImplYamlWithFilePreExistsCT extends AbstractComponentTest {

    protected static Logger logger = LoggerFactory
            .getLogger(ConfigurationLocalServiceImplYamlWithFilePreExistsCT.class);

    @BeforeClass
    public static void beforeClass() throws Exception {
        AbstractComponentTest.beforeClass();
        System.setProperty("spring.profiles.active", "ct-yml");
    }

    @Before
    public void before() {
        prepareTestData();
    }

    /**
     * Test for looking up configuration item success from files
     * specified by DP Name from system environment <br/>
     * prepare test files first, priori to start configuration
     * service.
     *
     * @throws NotFoundException
     */
    @Test
    public void testGetString_success() throws Exception {

        String fileFormat = "yml";

        String initialTestFileName = testDp + "." + fileFormat;

        String testFileSrcDir = "src/test/resources/test-files/ct-yml";

        doCopyTestFileToTestArea(testDp, testFileSrcDir, initialTestFileName, null);

        ServiceDiscovery serviceDiscovery = ServiceDiscoveryFactory.getServiceDiscovery();
        ConfigurationService configurationService = serviceDiscovery.discover(ConfigurationService.class, null, null);

        waitForMonitorInterval();

        try {
            String value = configurationService.getString(testConfiguration.getName());
            assertEquals(testConfiguration.getValue(), value);

            value = configurationService.getString(testConfiguration.getName());
            assertEquals(testConfiguration.getValue(), value);
        } finally {
            cleanTemporaryTestFiles();
        }

    }



    /**
     * Test for looking up configuration item success from files
     * specified by DP Name from system environment <br/>
     * prepare test files first, priori to start configuration
     * service.
     *
     * @throws NotFoundException
     */
    @Test
    public void testGetInt_success() throws Exception {

        String fileFormat = "yml";

        String initialTestFileName = testDp + "." + fileFormat;

        String testFileSrcDir = "src/test/resources/test-files/ct-yml";

        doCopyTestFileToTestArea(testDp, testFileSrcDir, initialTestFileName, null);

        ServiceDiscovery serviceDiscovery = ServiceDiscoveryFactory.getServiceDiscovery();
        ConfigurationService configurationService = serviceDiscovery.discover(ConfigurationService.class, null, null);

        waitForMonitorInterval();

        try {
            Integer value = configurationService.getInt(testIntTypeConfiguration.getName());
            assertEquals(new Integer(1001), value);
        } finally {
            cleanTemporaryTestFiles();
        }

    }

    @Test
    public void testGetObject_success() throws Exception {

        String fileFormat = "yml";

        String initialTestFileName = testDp + "." + fileFormat;

        String testFileSrcDir = "src/test/resources/test-files/ct-yml";

        doCopyTestFileToTestArea(testDp, testFileSrcDir, initialTestFileName, null);

        ServiceDiscovery serviceDiscovery = ServiceDiscoveryFactory.getServiceDiscovery();
        ConfigurationService configurationService = serviceDiscovery.discover(ConfigurationService.class, null, null);

        waitForMonitorInterval();

        try {
            Object value = configurationService.getObject(testIntTypeConfiguration.getName());
            assertTrue(value instanceof Integer);
            assertEquals(new Integer(1001), value);
        } finally {
            cleanTemporaryTestFiles();
        }

    }

    @Test
    public void testGetObject_failure() throws Exception {
        String fileFormat = "yml";
        String initialTestFileName = testDp + "." + fileFormat;
        String testFileSrcDir = "src/test/resources/test-files/ct-yml";
        doCopyTestFileToTestArea(testDp, testFileSrcDir, initialTestFileName, null);

        ServiceDiscovery serviceDiscovery = ServiceDiscoveryFactory.getServiceDiscovery();
        ConfigurationService configurationService = serviceDiscovery.discover(ConfigurationService.class, null, null);

        waitForMonitorInterval();

        try {
            configurationService.getObject(noneExistsConfiguration.getName());
            fail();
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        } catch (Exception e1) {
            logger.info("error", e1);
            fail();
        }
    }
}
