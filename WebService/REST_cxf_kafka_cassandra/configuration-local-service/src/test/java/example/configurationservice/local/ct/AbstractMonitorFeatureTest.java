package example.configurationservice.local.ct;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.commons.io.FileUtils;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import example.configurationservice.ConfigurationService;
import example.configurationservice.local.exception.ConfigurationRepositoryException;
import example.configurationservice.local.impl.ConfigurationLocalServiceImpl;


/**
 * Monitor feature test. <br/>
 * Use spring context files directly instead of ServiceDiscovery to
 * allow spring profile switching
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext_ConfigurationLocalService.xml"})
public abstract class AbstractMonitorFeatureTest extends AbstractComponentTest {

    protected static Logger logger = LoggerFactory.getLogger(AbstractMonitorFeatureTest.class);


    @Autowired
    protected ConfigurationService configurationService;

    protected ConfigurationLocalServiceImpl configurationServiceImpl;

    /**
     * @param profile    spring profile to test
     * @param fileFormat properties/yml
     * @throws Exception
     */
    protected void doTestConfigurationFileMonitorCreateEvent(String profile, final String fileFormat) throws Exception {

        try {
            configureLocalConfigurationServiceForComponentTest();

            // test file has not been created in test area yet, so NoSuchElementException should be thrown.
            try {
                String value = configurationService.getString(testConfiguration.getName());
                fail("Should throw NoSuchElementException, but got value:" + value);
            } catch (NoSuchElementException e) {
                // ok
            } catch (Exception e1) {
                logger.info("test error", e1);
                fail();
            }

            copyTestFilesToTestFolder(profile, fileFormat);

            waitForMonitorInterval();

            assertEquals(testConfiguration.getValue(), configurationService.getString(testConfiguration.getName()));
        } finally {
            clearTestEnvironment();
        }

    }


    /**
     * @param profile    spring profile to test
     * @param fileFormat properties/yml
     * @throws Exception
     */
    protected void doTestConfigurationFileMonitorUpdateEvent(String profile, final String fileFormat) throws Exception {

        try {
            configureLocalConfigurationServiceForComponentTest();

            copyTestFilesToTestFolder(profile, fileFormat);

            logger.info("Sleep a while ({} ms) to test configuration monitoring file create event ...",
                    waitTimeForMonitorToDetectChanges);
            Thread.sleep(waitTimeForMonitorToDetectChanges);

            assertEquals(testConfiguration.getValue(), configurationService.getString(testConfiguration.getName()));

            try {
                configurationService.getString("none.exist.config");
                fail("should throw NoSuchElementException");
            } catch (NoSuchElementException nsee) {

            }

            String testNoneExistWithDefault = configurationService.getString("none.exist.config", "defaultValue");
            assertEquals(testNoneExistWithDefault, "defaultValue");

            FileUtils.copyFile(updatedTestFile, temporaryTestFile);

            logger.info("Sleep a while ({} ms) to test configuration monitoring update event ...",
                    waitTimeForMonitorToDetectChanges);
            Thread.sleep(waitTimeForMonitorToDetectChanges);

            assertEquals("newValue1", configurationService.getString(testConfiguration.getName()));
        } finally {
            clearTestEnvironment();
        }

    }

    protected void configureLocalConfigurationServiceForComponentTest() throws ConfigurationRepositoryException {
        configurationServiceImpl = (ConfigurationLocalServiceImpl) configurationService;

        // FIXME : avoid restart monitor to reset component status: reset dao internal cache
        configurationServiceImpl.startMonitor();


    }

    protected void copyTestFilesToTestFolder(String profile, String fileFormat) throws IOException {
        String initialTestFileName = testDp + "." + fileFormat;
        String updatedTestFileName = testDp + "2." + fileFormat;

        String testFileSrcDir = "src/test/resources/test-files/" + profile;

        doCopyTestFileToTestArea(testDp, testFileSrcDir, initialTestFileName, updatedTestFileName);

    }

    protected void clearTestEnvironment() throws InterruptedException {
        cleanTemporaryTestFiles();

        logger.info("Sleep a while ({} ms)", waitTimeForMonitorToDetectChanges);
        Thread.sleep(waitTimeForMonitorToDetectChanges);

    }

}
