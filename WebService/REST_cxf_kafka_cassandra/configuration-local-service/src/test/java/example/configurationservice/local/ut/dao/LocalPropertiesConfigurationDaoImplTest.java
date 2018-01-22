package example.configurationservice.local.ut.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import example.configuration.type.ConfigurationList;
import example.configurationservice.local.dao.impl.properties.LocalPropertiesConfigurationDaoImpl;
import example.configurationservice.local.exception.ConfigurationRepositoryException;
import example.configurationservice.local.locator.FileLocator;
import example.configurationservice.local.metadata.impl.ConfigurationMetadataYamlRepository;
import example.configurationservice.local.ut.util.ConfigurationDaoImplTestHelper;

@RunWith(MockitoJUnitRunner.class)
public class LocalPropertiesConfigurationDaoImplTest extends AbstractLocalFileConfigurationDaoImplTest {

    public static final String[] DEFAULT_TEST_FILE_URL = new String[] {
            "file:src/test/resources/test-files/ut/properties/DP-ProfileService-Traffic.properties" };
    public static final String[] DEFAULT_TEST_FOLDER_URL = new String[] {
            "file:src/test/resources/test-files/ut/properties" };

    public static final String DEFAULT_TEST_METADATA_FILE_URL = "file:src/test/resources/test-files/ut/metadata/DP-ProfileService-Traffic.metadata.yml";

    private void prepareMock(String... testFileUrl) throws ConfigurationRepositoryException {
        FileLocator fileLocator = ConfigurationDaoImplTestHelper.getMockFileLocator(testFileUrl);

        // TODO Change to mock
        //ConfigurationMetadataRepository configurationMetadataRepository = mock(ConfigurationMetadataRepository.class);
        //when(configurationMetadataRepository.selectConfigurationMetadataByName("iam.authentication."))

        FileLocator metadataFileLocator = ConfigurationDaoImplTestHelper
                .getMockFileLocator(DEFAULT_TEST_METADATA_FILE_URL);
        ConfigurationMetadataYamlRepository metadataRepository = new ConfigurationMetadataYamlRepository(
                metadataFileLocator);
        metadataRepository.startMonitor();

        configurationDao = new LocalPropertiesConfigurationDaoImpl(fileLocator, metadataRepository);
        configurationDao.startMonitor();
    }

    @Before
    public void setUp() throws IOException, ConfigurationRepositoryException {
    }

    @Test
    public void testSelectConfigurationByName_WithOneFile_success() throws ConfigurationRepositoryException {
        prepareMock(DEFAULT_TEST_FILE_URL);

        doTestSelectConfigurationByName_WithOneFile_success();
    }

    /**
     * Test : merge multiple configuration files.
     *
     * @throws ConfigurationRepositoryException
     * @throws IOException
     */
    @Test
    public void testSelectConfigurationByName_WithMultipleFiles_success() throws ConfigurationRepositoryException {
        prepareMock(DEFAULT_TEST_FOLDER_URL);

        doTestSelectConfigurationByName_WithMultipleFiles_success();
    }

    @Test
    public void testSelectConfigurationByName_WithOneFileInClasspath_success() throws ConfigurationRepositoryException {
        prepareMock("classpath:/test-files/ut/properties/DP-ProfileService-Traffic.properties");

        doTestSelectConfigurationByName_WithOneFileInClasspath_success();
    }

    //@Test(expected = NoSuchElementException.class)
    @Test
    public void testSelectConfigurationByName_NoneExistsConfiguration()
            throws ConfigurationRepositoryException, IOException {
        prepareMock(DEFAULT_TEST_FILE_URL);

        doTestSelectConfigurationByName_NoneExistsConfiguration();
    }

    @Test
    public void testSelectConfigurationByFuzzyName() throws ConfigurationRepositoryException, IOException {
        //mock
        prepareMock(DEFAULT_TEST_FILE_URL);

        ConfigurationList expectedList = ConfigurationDaoImplTestHelper.getFuzzyTestConfigurationList();

        String fuzzyTestName = "FuzzyTest";
        ConfigurationList result = configurationDao.selectConfigurationByFuzzyName(fuzzyTestName);

        assertEquals(expectedList.getConfigurations().size(), result.getConfigurations().size());

    }

    @Test
    public void testSelectConfigurationByFuzzyName_LowerCase() throws ConfigurationRepositoryException, IOException {
        //mock
        prepareMock(DEFAULT_TEST_FILE_URL);

        String fuzzyTestName = "fuzzytest";
        ConfigurationList result = configurationDao.selectConfigurationByFuzzyName(fuzzyTestName);

        assertEquals(1, result.getConfigurations().size());

    }

    @Test
    public void testSelectConfigurationByFuzzyName_EmptyParameter()
            throws ConfigurationRepositoryException, IOException {
        //mock
        prepareMock(DEFAULT_TEST_FOLDER_URL);

        // total count of configuration items in multiple files
        int expectedSize = ConfigurationDaoImplTestHelper.TEST_ITMES_COUNT;

        String fuzzyTestName = "";
        ConfigurationList result = configurationDao.selectConfigurationByFuzzyName(fuzzyTestName);

        assertNotNull(result);
        assertNotNull(result.getConfigurations());
        assertEquals(expectedSize, result.getConfigurations().size());

    }

    @Test
    public void testSelectConfigurationByFuzzyName_NullParameter()
            throws ConfigurationRepositoryException, IOException {
        //mock
        prepareMock(DEFAULT_TEST_FILE_URL);

        String fuzzyTestName = null;
        ConfigurationList result = configurationDao.selectConfigurationByFuzzyName(fuzzyTestName);

        assertNotNull(result);
        assertTrue((result.getConfigurations() == null || result.getConfigurations().size() == 0));

    }

    @After
    public void tearDown() {

        if (configurationDao != null) {
            configurationDao.stopMonitor();
        }

    }

}
