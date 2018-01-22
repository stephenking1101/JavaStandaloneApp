package example.configurationservice.local.ct;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import example.configuration.type.Configuration;
import example.configurationservice.ConfigurationService;
import example.configurationservice.local.ut.util.TestPrepareFailedException;
import example.configurationservice.local.util.ConfigurationLocalServiceCommonUtils;


public abstract class AbstractComponentTest extends Assert {

    protected static Logger logger = LoggerFactory.getLogger(AbstractComponentTest.class);

    public static final int EXTRA_WAITTIME_FOR_FILE_MONITOR = 500;

    public static final String TEST_AREA_ROOT = "src/test/resources/test-area/etc/modules/";

    public static final String TEST_SOURCE_ROOT = "src/test/resources/test-files/";

    protected Configuration testConfiguration = null;

    protected Configuration testIntTypeConfiguration = null;

    protected Configuration noneExistsConfiguration = null;

    protected static long waitTimeForMonitorToDetectChanges;

    protected File srcTestFile;
    protected File temporaryTestFile;
    protected File updatedTestFile;

    @Autowired
    protected ConfigurationService configurationService;

    protected static String testDp;

    @BeforeClass
    public static void beforeClass() throws Exception {
        testDp = "DP-ProfileService-Traffic";
        System.setProperty("dp.name", testDp);

        // test mode
        System.setProperty("SIG_Running_Mode", "Testing");

        waitTimeForMonitorToDetectChanges = ConfigurationLocalServiceCommonUtils.getDefaultInterval() + EXTRA_WAITTIME_FOR_FILE_MONITOR;
    }

    @AfterClass
    public static void afterClass() throws Exception {
    }

    protected void prepareTestData() {
        testConfiguration = new Configuration();
        testConfiguration.setName("iam.authentication.config1");
        testConfiguration.setValue("value1");
        testConfiguration.setRw(1);

        testIntTypeConfiguration = new Configuration();
        testIntTypeConfiguration.setName("iam.authentication.config2");
        testIntTypeConfiguration.setType("java.lang.Long");
        testIntTypeConfiguration.setValue("1001");
        testIntTypeConfiguration.setRw(1);

        noneExistsConfiguration = new Configuration();
        noneExistsConfiguration.setName("iam.authentication.noneExists");
    }

    protected void waitForMonitorInterval() throws InterruptedException {
        logger.info("Sleep a while (" + waitTimeForMonitorToDetectChanges
                + " ms) to test configuration monitoring file create event ...");
        Thread.sleep(waitTimeForMonitorToDetectChanges);
    }

    protected void doCopyTestFileToTestArea(String dp, String testFileSrcDir, String initialTestFileName,
            String updatedTestFileName) throws IOException {
        srcTestFile = new File(testFileSrcDir + "/" + initialTestFileName);
        temporaryTestFile = new File(TEST_AREA_ROOT + dp + "/" + initialTestFileName);

        if (updatedTestFileName != null) {
            updatedTestFile = new File(testFileSrcDir + "/" + updatedTestFileName);
        }

        if (temporaryTestFile.exists()) {
            FileUtils.forceDelete(temporaryTestFile);
        }

        if (temporaryTestFile.exists()) {
            throw new TestPrepareFailedException(
                    "Temporary test file should not exist before copying test file to test folder.");
        }
        FileUtils.copyFile(srcTestFile, temporaryTestFile);
        if (!temporaryTestFile.exists()) {
            throw new TestPrepareFailedException(
                    "Temporary test file should exist after copying test files to test folder.");
        }
    }



    protected void doCopyUpdatedTestFileToTestArea(String dp, String testFileSrcDir, String initialTestFileName,
            String updatedTestFileName) throws IOException {
        srcTestFile = new File(testFileSrcDir + "/" + initialTestFileName);
        temporaryTestFile = new File(TEST_AREA_ROOT + dp + "/" + initialTestFileName);

        if (updatedTestFileName != null) {
            updatedTestFile = new File(testFileSrcDir + "/" + updatedTestFileName);
        }

        FileUtils.copyFile(updatedTestFile, temporaryTestFile);
        if (!temporaryTestFile.exists()) {
            throw new TestPrepareFailedException(
                    "Temporary test file should exist after copying test files to test folder.");
        }
    }

    protected void cleanTemporaryTestFiles() {
        boolean deleted = FileUtils.deleteQuietly(new File(TEST_AREA_ROOT));
        if (!deleted) {
            logger.error("Temporary test dir [{}] is not removed as expected. delete them manually.", TEST_AREA_ROOT);
        } else {
            logger.debug("Temporary test dir [{}] is deleted.", TEST_AREA_ROOT);
        }
    }

    protected void copyFile(String source, String target) throws IOException {
        File sourceFile = new File(source);
        File targetFile = new File(target);
        if (targetFile.exists()) {
            FileUtils.forceDelete(targetFile);
        }

        if (targetFile.exists()) {
            throw new TestPrepareFailedException(
                    "Temporary test file should not exist before copying test file to test folder." + target);
        }
        FileUtils.copyFile(sourceFile, targetFile);
        if (!targetFile.exists()) {
            throw new TestPrepareFailedException(
                    "Temporary test file should exist after copying test files to test folder." + target);
        }

    }

    protected void removeFile(String target) {
        File targetFile = new File(target);
        boolean deleted = FileUtils.deleteQuietly(targetFile);
        if (targetFile.exists()) {
            throw new TestPrepareFailedException(
                    "Temporary test file should be deleted." + target  + " but delete action result: " + deleted);
        }
        logger.info(" Test Action: deleted file " + targetFile.getAbsolutePath());
    }
}
