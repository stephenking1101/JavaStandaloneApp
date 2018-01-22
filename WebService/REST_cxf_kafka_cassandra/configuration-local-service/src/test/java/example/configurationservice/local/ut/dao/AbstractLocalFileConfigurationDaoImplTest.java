package example.configurationservice.local.ut.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import example.configuration.type.Configuration;
import example.configurationservice.local.dao.MonitorableReadonlyConfigurationDAO;
import example.configurationservice.local.exception.ConfigurationRepositoryException;
import example.configurationservice.local.ut.util.ConfigurationDaoImplTestHelper;

public class AbstractLocalFileConfigurationDaoImplTest {

    protected MonitorableReadonlyConfigurationDAO configurationDao;

    public void doTestSelectConfigurationByName_WithOneFile_success() throws ConfigurationRepositoryException {
        Configuration expectedConfiguration = ConfigurationDaoImplTestHelper.getTestConfiguration();

        Configuration result = configurationDao.selectConfigurationByName(expectedConfiguration.getName());
        assertEquals(expectedConfiguration.getValue(), result.getValue());

    }

    protected void doTestSelectConfigurationByName_WithMultipleFiles_success() throws ConfigurationRepositoryException {
        // prepare
        Configuration expectedConfiguration = ConfigurationDaoImplTestHelper.getTestConfiguration();
        Configuration expectedConfigurationInFile2 = ConfigurationDaoImplTestHelper.getTestConfigurationInFile2();

        //test
        Configuration result = configurationDao.selectConfigurationByName(expectedConfiguration.getName());

        //verify
        assertEquals(expectedConfiguration.getValue(), result.getValue());
        assertEquals(expectedConfiguration.getType(), result.getType());

        //test
        Configuration resultInFile2 = configurationDao
                .selectConfigurationByName(expectedConfigurationInFile2.getName());

        //verify
        assertEquals(expectedConfigurationInFile2.getValue(), resultInFile2.getValue());
        assertEquals(expectedConfigurationInFile2.getType(), resultInFile2.getType());

    }

    public void doTestSelectConfigurationByName_WithOneFileInClasspath_success()
            throws ConfigurationRepositoryException {
        Configuration expectedConfiguration = ConfigurationDaoImplTestHelper.getTestConfiguration();

        Configuration result = configurationDao.selectConfigurationByName(expectedConfiguration.getName());
        assertEquals(expectedConfiguration.getValue(), result.getValue());
    }

    protected void doTestSelectConfigurationByName_NoneExistsConfiguration() throws ConfigurationRepositoryException {
        assertNull(configurationDao.selectConfigurationByName("iam.authentication.configNoneExists"));
    }

}
