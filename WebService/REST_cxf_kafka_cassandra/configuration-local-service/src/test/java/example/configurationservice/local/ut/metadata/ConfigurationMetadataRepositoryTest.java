package example.configurationservice.local.ut.metadata;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import example.configurationservice.local.exception.ConfigurationRepositoryException;
import example.configurationservice.local.locator.FileLocator;
import example.configurationservice.local.metadata.impl.ConfigurationMetadataYamlRepository;
import example.configurationservice.local.model.ConfigurationMetadata;
import example.configurationservice.local.ut.util.ConfigurationDaoImplTestHelper;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationMetadataRepositoryTest {

    public static final String DEFAULT_TEST_METADATA_DIR = "file:src/test/resources/test-files/ut/metadata/";
    public static final String DEFAULT_TEST_METADATA_FILE_URL = DEFAULT_TEST_METADATA_DIR
            + "DP-ProfileService-Traffic.metadata.yml";
    public static final String DEFAULT_TEST_METADATA_FILE_WILDCARD_URL = DEFAULT_TEST_METADATA_DIR + "*.metadata.yml";
    public static final String DEFAULT_TEST_METADATA_FILE2_URL = DEFAULT_TEST_METADATA_DIR + "module2.metadata.yml";

    private ConfigurationMetadataYamlRepository metadataRepository;

    private void prepareMock(List<String> metadataFileUrl) throws ConfigurationRepositoryException, IOException {
        FileLocator metadataFileLocator = ConfigurationDaoImplTestHelper.getMockFileLocator(metadataFileUrl);
        metadataRepository = new ConfigurationMetadataYamlRepository(metadataFileLocator);
        metadataRepository.startMonitor();
    }

    @Before
    public void setUp() throws IOException, ConfigurationRepositoryException {
    }

    @Test
    public void testSelectConfigurationByNameWithOneFileSuccess() throws ConfigurationRepositoryException, IOException {
        prepareMock(Arrays.asList(DEFAULT_TEST_METADATA_FILE_URL));
        ConfigurationMetadata metadataResult;

        metadataResult = metadataRepository.selectConfigurationMetadataByName("iam.authentication.config1");
        assertEquals("java.lang.String", metadataResult.getType());

        metadataResult = metadataRepository.selectConfigurationMetadataByName("iam.authentication.config2");
        assertEquals("java.lang.Integer", metadataResult.getType());

    }

    @Test
    public void testSelectConfigurationByNameWithWildcardFileSuccess()
            throws ConfigurationRepositoryException, IOException {
        prepareMock(Arrays.asList(DEFAULT_TEST_METADATA_FILE_WILDCARD_URL));
        ConfigurationMetadata metadataResult;

        metadataResult = metadataRepository.selectConfigurationMetadataByName("iam.authentication.config1");
        assertEquals("java.lang.String", metadataResult.getType());

        metadataResult = metadataRepository.selectConfigurationMetadataByName("iam.authentication.config2");
        assertEquals("java.lang.Integer", metadataResult.getType());

    }

    @Test
    public void testSelectConfigurationByNameWithMutlpleFilesSuccess()
            throws ConfigurationRepositoryException, IOException {
        prepareMock(Arrays.asList(DEFAULT_TEST_METADATA_FILE_URL, DEFAULT_TEST_METADATA_FILE2_URL));
        ConfigurationMetadata metadataResult;

        metadataResult = metadataRepository.selectConfigurationMetadataByName("iam.authentication.config1");
        assertEquals("java.lang.String", metadataResult.getType());

        metadataResult = metadataRepository.selectConfigurationMetadataByName("module2.config2");
        assertEquals("java.lang.Integer", metadataResult.getType());

    }

    @Test
    public void testSelectConfigurationByNameWithDuplicateResources()
            throws ConfigurationRepositoryException, IOException {
        prepareMock(Arrays.asList(DEFAULT_TEST_METADATA_FILE_WILDCARD_URL, DEFAULT_TEST_METADATA_FILE2_URL));
        ConfigurationMetadata metadataResult;

        metadataResult = metadataRepository.selectConfigurationMetadataByName("iam.authentication.config1");
        assertEquals("java.lang.String", metadataResult.getType());

        metadataResult = metadataRepository.selectConfigurationMetadataByName("module2.config2");
        assertEquals("java.lang.Integer", metadataResult.getType());

    }

    @After
    public void tearDown() {

        if (metadataRepository != null) {
            metadataRepository.stopMonitor();
        }

    }

}
