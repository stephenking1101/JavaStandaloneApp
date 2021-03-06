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

import example.configuration.type.Configuration;
import example.configuration.type.ConfigurationList;
import example.configurationservice.local.dao.impl.yaml.LocalYamlConfigurationDaoImpl;
import example.configurationservice.local.exception.ConfigurationRepositoryException;
import example.configurationservice.local.locator.FileLocator;
import example.configurationservice.local.metadata.impl.ConfigurationMetadataYamlRepository;
import example.configurationservice.local.ut.util.ConfigurationDaoImplTestHelper;


@RunWith(MockitoJUnitRunner.class)
public class LocalYamlConfigurationDaoImplTest extends AbstractLocalFileConfigurationDaoImplTest {

    public static final String[] DEFAULT_TEST_FILE_URL = new String[]{
            "file:src/test/resources/test-files/ut/yml/DP-ProfileService-Traffic.yml"};
    public static final String[] DEFAULT_TEST_FOLDER_URL = new String[]{"file:src/test/resources/test-files/ut/yml"};

    public static final String DEFAULT_TEST_METADATA_FILE_URL = "file:src/test/resources/test-files/ut/metadata/DP-ProfileService-Traffic.metadata.yml";

    private void prepareMock(String[] fileUrl) throws ConfigurationRepositoryException {
        FileLocator fileLocator = ConfigurationDaoImplTestHelper.getMockFileLocator(fileUrl);

        // TODO Change to mock
        //ConfigurationMetadataRepository configurationMetadataRepository = mock(ConfigurationMetadataRepository.class);
        //when(configurationMetadataRepository.selectConfigurationMetadataByName("iam.authentication."))

        FileLocator metadataFileLocator = ConfigurationDaoImplTestHelper
                .getMockFileLocator(DEFAULT_TEST_METADATA_FILE_URL);
        ConfigurationMetadataYamlRepository metadataRepository = new ConfigurationMetadataYamlRepository(
                metadataFileLocator);
        metadataRepository.startMonitor();

        configurationDao = new LocalYamlConfigurationDaoImpl(fileLocator, metadataRepository);
        configurationDao.startMonitor();
    }

    @Before
    public void setUp() throws IOException, ConfigurationRepositoryException {
    }

    @Test
    public void testSelectConfigurationByName_IntTypeValue_success() throws ConfigurationRepositoryException {
        //mock
        prepareMock(DEFAULT_TEST_FILE_URL);

        Configuration expectedConfiguration = ConfigurationDaoImplTestHelper.getTestConfigurationIntValue();

        Configuration result = configurationDao.selectConfigurationByName(expectedConfiguration.getName());
        assertEquals(expectedConfiguration.getValue(), result.getValue());
        assertEquals(expectedConfiguration.getType(), result.getType());
    }

    /**
     * Case:
     * <p>
     * in yaml: config1 : '1001'
     * <p>
     * Test: ensure that original value: '1001' is read into memory as
     * "1001" instead of "'1001'"
     * <p>
     * Caution: this behavior it not identical with properties type
     * file.
     *
     * @throws ConfigurationRepositoryException
     * @throws IOException
     */
    @Test
    public void testSelectConfigurationByName_IntTypeValue_StringQuoted_success()
            throws ConfigurationRepositoryException {
        //mock
        prepareMock(DEFAULT_TEST_FILE_URL);

        Configuration expectedConfiguration = ConfigurationDaoImplTestHelper.getTestConfigurationIntValueStringQuoted();

        Configuration result = configurationDao.selectConfigurationByName(expectedConfiguration.getName());
        assertEquals(expectedConfiguration.getValue(), result.getValue());
        assertEquals(expectedConfiguration.getType(), result.getType());
    }

    @Test
    public void testSelectConfigurationByName_WithOneFile_success() throws ConfigurationRepositoryException {
        //mock
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
        //mock
        prepareMock(DEFAULT_TEST_FOLDER_URL);

        doTestSelectConfigurationByName_WithMultipleFiles_success();
    }

    @Test
    public void testSelectConfigurationByName_WithOneFileInClasspath_success() throws ConfigurationRepositoryException {
        prepareMock(new String[]{"classpath:/test-files/ut/yml/DP-ProfileService-Traffic.yml"});

        doTestSelectConfigurationByName_WithOneFileInClasspath_success();
    }

    @Test
    public void testSelectConfigurationByName_NoneExistsConfiguration() throws ConfigurationRepositoryException {
        //mock
        prepareMock(DEFAULT_TEST_FILE_URL);

        doTestSelectConfigurationByName_NoneExistsConfiguration();
    }

    @Test
    public void testSelectConfigurationByFuzzyName() throws ConfigurationRepositoryException {
        //mock
        prepareMock(DEFAULT_TEST_FILE_URL);

        ConfigurationList expectedList = ConfigurationDaoImplTestHelper.getFuzzyTestConfigurationList();

        String fuzzyTestName = "FuzzyTest";
        ConfigurationList result = configurationDao.selectConfigurationByFuzzyName(fuzzyTestName);

        assertEquals(expectedList.getConfigurations().size(), result.getConfigurations().size());

    }

    @Test
    public void testSelectConfigurationByFuzzyName_LowerCase() throws ConfigurationRepositoryException {
        //mock
        prepareMock(DEFAULT_TEST_FILE_URL);

        String fuzzyTestName = "fuzzytest";
        ConfigurationList result = configurationDao.selectConfigurationByFuzzyName(fuzzyTestName);

        assertEquals(1, result.getConfigurations().size());

    }

    @Test
    public void testSelectConfigurationByFuzzyName_EmptyParameter() throws ConfigurationRepositoryException {
        //mock
        prepareMock(DEFAULT_TEST_FILE_URL);

        // total count of configuration items in multiple files
        int expectedSize = ConfigurationDaoImplTestHelper.TEST_ITMES_COUNT;

        String fuzzyTestName = "";
        ConfigurationList result = configurationDao.selectConfigurationByFuzzyName(fuzzyTestName);

        assertNotNull(result);
        assertNotNull(result.getConfigurations());
        assertEquals(expectedSize, result.getConfigurations().size());

    }

    @Test
    public void testSelectConfigurationByFuzzyName_NullParameter() throws ConfigurationRepositoryException {
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
