package example.configurationservice.local.ct;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

import example.configuration.type.Configuration;

@ActiveProfiles("ct-yml-multiple")
public class ConfigurationLocalServiceImplYamlMultiplyFilesCT extends AbstractMonitorFeatureTest {

    protected static Logger logger = LoggerFactory.getLogger(ConfigurationLocalServiceImplYamlMultiplyFilesCT.class);

    private String[] compatibleComponents = new String[] { testDp };
    private String[] testComponents = new String[] { "foundation" };

    @Before
    public void before() {
        prepareTestData();
        // TODO ensure test area is clean
    }

    @After
    public void after() throws InterruptedException {
        clearTestEnvironment();
    }

    @Test
    public void testYamlConfigurationFileMonitorCreateEvent() throws Exception {
        String profile = "ct-yml-multiple";
        String fileFormat = "yml";

        configureLocalConfigurationServiceForComponentTest();

        // test file has not been created in test area yet, so NoSuchElementException should be thrown.
        try {
            String value = configurationService.getString(testConfiguration.getName());
            fail("Should throw NoSuchElementException, but got value:" + value);
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        } catch (Exception e1) {
            logger.info("test error", e1);
            fail();
        }

        copyTestFilesToTestFolder(profile, fileFormat, false);
        copyTestMetadataFilesToTestFoder(profile, fileFormat);

        logger.info("Sleep a while (" + waitTimeForMonitorToDetectChanges
                + " ms) to test configuration monitoring file create event ...");
        Thread.sleep(waitTimeForMonitorToDetectChanges);

        assertEquals(testConfiguration.getValue(), configurationService.getString(testConfiguration.getName()));

        assertEquals("3", configurationService.getString("facility.integration.restful.retryTimes"));
        assertEquals(3, configurationService.getInt("facility.integration.restful.retryTimes"));
        assertEquals(Integer.class,
                configurationService.getObject("facility.integration.restful.retryTimes").getClass());

    }

    @Test
    public void testYamlConfigurationFileMonitorUpdateEvent() throws Exception {
        String profile = "ct-yml-multiple";
        String fileFormat = "yml";

        // prepare
        configureLocalConfigurationServiceForComponentTest();

        // test file has not been created in test area yet, so NoSuchElementException should be thrown.
        try {
            String value = configurationService.getString(testConfiguration.getName());
            fail("Should throw NoSuchElementException, but got value:" + value);
        } catch (NoSuchElementException e) {
            assertTrue(true);
        } catch (Exception e1) {
            logger.info("test error", e1);
            fail();
        }

        // prepare create scenario
        copyTestFilesToTestFolder(profile, fileFormat, false);
        copyTestMetadataFilesToTestFoder(profile, fileFormat);

        logger.info("Sleep a while (" + waitTimeForMonitorToDetectChanges
                + " ms) to test configuration monitoring file create event ...");
        Thread.sleep(waitTimeForMonitorToDetectChanges);

        // verify
        assertEquals(testConfiguration.getValue(), configurationService.getString(testConfiguration.getName()));
        assertEquals(3, configurationService.getInt("facility.integration.restful.retryTimes"));
        assertEquals("value", configurationService.getObject("facility.integration.willBeDeleted"));

        // prepare update scenario
        copyTestFilesToTestFolder(profile, fileFormat, true);

        logger.info("Sleep a while (" + waitTimeForMonitorToDetectChanges
                + " ms) to test configuration monitoring file update event ...");
        Thread.sleep(waitTimeForMonitorToDetectChanges);

        // verify
        assertEquals(5, configurationService.getInt("facility.integration.restful.retryTimes"));
        try {
            configurationService.getObject("facility.integration.willBeDeleted");
            fail("facility.integration.willBeDeleted should not exist after file updated");
        } catch (NoSuchElementException nsee) {
            assertTrue(true);
        }

    }

    @Test
    @Ignore
    public void testYamlConfigurationFileMonitorDeleteEvent2() throws Exception {

        configureLocalConfigurationServiceForComponentTest();

        // prepare initial files
        copyFile(TEST_SOURCE_ROOT + "ct-yml-multiple/foundation.config.yml",
                TEST_AREA_ROOT + "DP-ProfileService-Traffic/foundation.config.yml");

        logger.info("Sleep a while (" + waitTimeForMonitorToDetectChanges
                + " ms) to test configuration monitoring file create event ...");
        Thread.sleep(waitTimeForMonitorToDetectChanges);


        System.out.println("Sleep 10s for test");
        Thread.sleep(1000*10);
        // delete files

        removeFile(TEST_AREA_ROOT + "DP-ProfileService-Traffic/foundation.config.yml");

        logger.info("Sleep a while (" + waitTimeForMonitorToDetectChanges
                + " ms) to test configuration monitoring file delete event ...");
        Thread.sleep(waitTimeForMonitorToDetectChanges);

        // end
    }

    @Test
    public void testYamlConfigurationFileMonitorDeleteEvent() throws Exception {

        configureLocalConfigurationServiceForComponentTest();

        // prepare initial files
        copyFile(TEST_SOURCE_ROOT + "ct-yml-multiple/foundation.metadata.yml",
                TEST_AREA_ROOT + "DP-ProfileService-Traffic/foundation.metadata.yml");
        copyFile(TEST_SOURCE_ROOT + "ct-yml-multiple/foundation.config.yml",
                TEST_AREA_ROOT + "DP-ProfileService-Traffic/foundation.config.yml");
        copyFile(TEST_SOURCE_ROOT + "ct-yml-multiple/DP-ProfileService-Traffic.metadata.yml",
                TEST_AREA_ROOT + "DP-ProfileService-Traffic/DP-ProfileService-Traffic.metadata.yml");
        copyFile(TEST_SOURCE_ROOT + "ct-yml-multiple/DP-ProfileService-Traffic.yml",
                TEST_AREA_ROOT + "DP-ProfileService-Traffic/DP-ProfileService-Traffic.yml");

        logger.info("Sleep a while (" + waitTimeForMonitorToDetectChanges
                + " ms) to test configuration monitoring file create event ...");
        Thread.sleep(waitTimeForMonitorToDetectChanges);

        // verify
        assertEquals(testConfiguration.getValue(), configurationService.getString(testConfiguration.getName()));

        assertEquals(3, configurationService.getInt("facility.integration.restful.retryTimes"));

        // delete files

        removeFile(TEST_AREA_ROOT + "DP-ProfileService-Traffic/foundation.metadata.yml");
        removeFile(TEST_AREA_ROOT + "DP-ProfileService-Traffic/foundation.config.yml");

        logger.info("Sleep a while (" + waitTimeForMonitorToDetectChanges
                + " ms) to test configuration monitoring file delete event ...");
        Thread.sleep(waitTimeForMonitorToDetectChanges);

        // verify
        // items in foundation.yml deleted
        try {
            configurationService.getObject("facility.integration.restful.retryTimes");
            fail("facility.integration.restful.retryTimes should not exist after file deleted");
        } catch (NoSuchElementException nsee) {
            assertTrue(true);
        }

        // items in another file still exist
        assertEquals(testConfiguration.getValue(), configurationService.getString(testConfiguration.getName()));

    }

    @Test
    public void testYamlConfigurationFileContainsSameKey() throws Exception {
        Configuration configurationDuplicated = new Configuration();
        configurationDuplicated.setName("iam.authentication.configInBothFiles");
        //prefer dp config over shared config
        configurationDuplicated.setValue("ProfileServiceYMLValue");
        configurationDuplicated.setType("java.lang.String");
        configurationDuplicated.setRw(1);
        String profile = "ct-yml-multiple";
        String fileFormat = "yml";

        copyTestFilesToTestFolder(profile, fileFormat, false);
        copyTestMetadataFilesToTestFoder(profile, fileFormat);
        configureLocalConfigurationServiceForComponentTest();

        logger.info("Sleep a while (" + waitTimeForMonitorToDetectChanges
                + " ms) to test configuration monitoring file create event ...");
        Thread.sleep(waitTimeForMonitorToDetectChanges);

        assertEquals(configurationDuplicated.getValue(), configurationService.getString(configurationDuplicated.getName()));
    }

    protected void copyTestMetadataFilesToTestFoder(String profile, String fileFormat) throws IOException {

        String testFileSrcDir = "src/test/resources/test-files/" + profile;

        for (String component : compatibleComponents) {
            String initialTestMetadataFileName = component + ".metadata." + fileFormat;
            String updatedTestMetadataFileName = null;

            doCopyTestFileToTestArea(testDp, testFileSrcDir, initialTestMetadataFileName, updatedTestMetadataFileName);
        }

        for (String component : testComponents) {
            String initialTestMetadataFileName = component + ".metadata." + fileFormat;
            String updatedTestMetadataFileName = null;

            doCopyTestFileToTestArea(testDp, testFileSrcDir, initialTestMetadataFileName, updatedTestMetadataFileName);
        }

    }

    protected void copyTestFilesToTestFolder(String profile, String fileFormat, boolean copyUpdatedFile)
            throws IOException {

        String testFileSrcDir = "src/test/resources/test-files/" + profile;

        for (String component : testComponents) {
            String initialTestFile1Name = component + ".config." + fileFormat;
            String updatedTestFile1Name = component + ".config." + fileFormat + ".updated";

            if (!copyUpdatedFile) {
                doCopyTestFileToTestArea(testDp, testFileSrcDir, initialTestFile1Name, updatedTestFile1Name);
            } else {
                doCopyUpdatedTestFileToTestArea(testDp, testFileSrcDir, initialTestFile1Name, updatedTestFile1Name);
            }
        }

        for (String component : compatibleComponents) {
            String initialTestFileName = component + "." + fileFormat;
            String updatedTestFileName = component + "." + fileFormat + ".updated";

            if (!copyUpdatedFile) {
                doCopyTestFileToTestArea(testDp, testFileSrcDir, initialTestFileName, updatedTestFileName);
            } else {
                doCopyUpdatedTestFileToTestArea(testDp, testFileSrcDir, initialTestFileName, updatedTestFileName);
            }
        }
    }
}
