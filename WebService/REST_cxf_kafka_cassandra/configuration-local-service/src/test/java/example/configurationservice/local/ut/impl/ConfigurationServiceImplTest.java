package example.configurationservice.local.ut.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import example.configuration.type.Configuration;
import example.configuration.type.ConfigurationList;
import example.configurationservice.ConfigurationService;
import example.configurationservice.local.dao.ReadonlyConfigurationDAO;
import example.configurationservice.local.exception.ConfigurationRepositoryException;
import example.configurationservice.local.impl.ConfigurationLocalServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationServiceImplTest {

    @Mock
    private ReadonlyConfigurationDAO configurationDao;

    @InjectMocks
    private ConfigurationService configurationService = new ConfigurationLocalServiceImpl();

    @BeforeClass
    public static void beforeClass() {
        // clear test mode to test cache feature
        System.clearProperty("SIG_Running_Mode");
    }

    @Test
    public void testGetBoolean_success() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("boolean_config1");
        configuration.setValue("true");
        configuration.setType("java.lang.Boolean");
        configuration.setRw(1);

        when(configurationDao.selectConfigurationByName(configuration.getName())).thenReturn(configuration);

        boolean value = configurationService.getBoolean(configuration.getName());
        assertEquals(true, value);
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetBoolean_failure() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("boolean_config2");
        configuration.setValue("true");
        configuration.setType("java.lang.Boolean");

        when(configurationDao.selectConfigurationByName(configuration.getName()))
                .thenThrow(new ConfigurationRepositoryException());
        boolean value = false;
        value = configurationService.getBoolean(configuration.getName());
        assertEquals(false, value);
    }

    @Test
    public void testGetRWBooleanFromCache_success() throws ConfigurationRepositoryException {
        Configuration configuration1 = new Configuration();
        configuration1.setName("boolean_config3");
        configuration1.setValue("true");
        configuration1.setType("java.lang.Boolean");
        configuration1.setRw(1);

        when(configurationDao.selectConfigurationByName(configuration1.getName())).thenReturn(configuration1);

        boolean value1 = configurationService.getBoolean(configuration1.getName());
        assertEquals(true, value1);

        Configuration configuration2 = new Configuration();
        configuration2.setName("boolean_config3");
        configuration2.setValue("false");
        configuration2.setType("java.lang.Boolean");
        configuration1.setRw(1);
        when(configurationDao.selectConfigurationByName(configuration2.getName())).thenReturn(configuration2);

        boolean value2 = configurationService.getBoolean(configuration1.getName());
        assertEquals(true, value2);

    }

    @Test
    public void testGetReadOnlyBooleanFromCache_success() throws ConfigurationRepositoryException {
        Configuration configuration1 = new Configuration();
        configuration1.setName("boolean_config_r");
        configuration1.setValue("true");
        configuration1.setType("java.lang.Boolean");
        configuration1.setRw(0);

        when(configurationDao.selectConfigurationByName(configuration1.getName())).thenReturn(configuration1);

        boolean value1 = configurationService.getBoolean(configuration1.getName());
        assertEquals(true, value1);

        Configuration configuration2 = new Configuration();
        configuration2.setName("boolean_config_r");
        configuration2.setValue("false");
        configuration2.setType("java.lang.Boolean");
        configuration1.setRw(0);
        when(configurationDao.selectConfigurationByName(configuration2.getName())).thenReturn(configuration2);

        boolean value2 = configurationService.getBoolean(configuration1.getName());
        assertEquals(true, value2);

    }

    @Test
    public void testGetBoolean_returnActualValue() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("boolean_config4");
        configuration.setValue("false");
        configuration.setType("java.lang.Boolean");
        configuration.setRw(0);

        when(configurationDao.selectConfigurationByName(configuration.getName())).thenReturn(configuration);

        boolean value = configurationService.getBoolean(configuration.getName(), true);
        assertEquals(false, value);
    }

    @Test
    public void testGetBoolean_returnDefalutValue() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("boolean_config5");
        configuration.setValue("false");
        configuration.setType("java.lang.Boolean");
        configuration.setRw(1);

        when(configurationDao.selectConfigurationByName(configuration.getName()))
                .thenThrow(new ConfigurationRepositoryException());
        boolean value = configurationService.getBoolean(configuration.getName(), true);
        assertEquals(true, value);
    }

    @Test(expected = RuntimeException.class)
    public void testGetBoolean_type_error() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("boolean_config6");
        configuration.setValue("true");
        configuration.setType("java.lang.String");
        configuration.setRw(1);

        when(configurationDao.selectConfigurationByName(configuration.getName())).thenReturn(configuration);
        boolean value = false;
        value = configurationService.getBoolean(configuration.getName());
        assertEquals(false, value);
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetBoolean_failure_db_return_null() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("boolean_config7");
        configuration.setValue("true");
        configuration.setType("java.lang.Boolean");
        configuration.setRw(0);

        when(configurationDao.selectConfigurationByName(configuration.getName())).thenReturn(null);
        //when(georedConfigurationDao.selectConfigurationByName(configuration.getName())).thenReturn(null);

        boolean value = false;
        value = configurationService.getBoolean(configuration.getName());
        assertEquals(false, value);
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetBoolean_return_exception_from_cache() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("boolean_config8");
        configuration.setValue("true");
        configuration.setType("java.lang.Boolean");
        configuration.setRw(0);

        when(configurationDao.selectConfigurationByName(configuration.getName()))
                .thenThrow(new ConfigurationRepositoryException());
        boolean value = false;
        try {
            value = configurationService.getBoolean(configuration.getName());
        } catch (Exception e) {
            assertNotNull(e);
            assertEquals(true, e instanceof NoSuchElementException);
        }
        assertEquals(false, value);

        reset(configurationDao);
        when(configurationDao.selectConfigurationByName(configuration.getName())).thenReturn(configuration);
        value = configurationService.getBoolean(configuration.getName());
        assertEquals(false, value);
    }

    @Test
    public void testGetDouble_success() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("double_config1");
        configuration.setValue(String.valueOf(Math.PI));
        configuration.setType("java.lang.Double");
        configuration.setRw(1);

        when(configurationDao.selectConfigurationByName(configuration.getName())).thenReturn(configuration);

        Double value = configurationService.getDouble(configuration.getName());
        assertEquals(Double.valueOf(configuration.getValue()), value);
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetDouble_failure() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("double_config2");
        configuration.setValue(String.valueOf(Math.PI));
        configuration.setType("java.lang.Double");

        when(configurationDao.selectConfigurationByName(configuration.getName()))
                .thenThrow(new ConfigurationRepositoryException());
        Double value = 0.0d;
        value = configurationService.getDouble(configuration.getName());
        assertEquals(new Double(0), value);
    }

    @Test
    public void testGetRWDoubleFromCache_success() throws ConfigurationRepositoryException {
        Configuration configuration1 = new Configuration();
        configuration1.setName("double_config3");
        configuration1.setValue(String.valueOf(Math.PI));
        configuration1.setType("java.lang.Double");
        configuration1.setRw(1);

        when(configurationDao.selectConfigurationByName(configuration1.getName())).thenReturn(configuration1);

        Double value1 = configurationService.getDouble(configuration1.getName());
        assertEquals(new Double(Math.PI), value1);

        Configuration configuration2 = new Configuration();
        configuration2.setName("double_config3");
        configuration2.setValue(String.valueOf(Math.PI + 2));
        configuration2.setType("java.lang.Double");
        configuration2.setRw(1);
        when(configurationDao.selectConfigurationByName(configuration2.getName())).thenReturn(configuration2);

        Double value2 = configurationService.getDouble(configuration1.getName());
        assertEquals(new Double(Math.PI), value2);

    }

    @Test
    public void testGetDouble_returnActualValue() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("double_config4");
        configuration.setValue(String.valueOf(Math.PI));
        configuration.setType("java.lang.Double");
        configuration.setRw(1);

        when(configurationDao.selectConfigurationByName(configuration.getName())).thenReturn(configuration);

        Double value = configurationService.getDouble(configuration.getName(), Math.PI + 2);
        assertEquals(new Double(Math.PI), value);
    }

    @Test
    public void testGetDouble_returnDefalutValue() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("double_config5");
        configuration.setValue(String.valueOf(Math.PI));
        configuration.setType("java.lang.Double");
        configuration.setRw(0);

        when(configurationDao.selectConfigurationByName(configuration.getName()))
                .thenThrow(new ConfigurationRepositoryException());
        Double value = configurationService.getDouble(configuration.getName(), Math.PI + 2);
        assertEquals(new Double(Math.PI + 2), value);
    }

    @Test
    public void testGetInt_success() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("int_config1");
        configuration.setValue(String.valueOf(1));
        configuration.setType("java.lang.Integer");
        configuration.setRw(1);

        when(configurationDao.selectConfigurationByName(configuration.getName())).thenReturn(configuration);

        Integer value = configurationService.getInt(configuration.getName());
        assertEquals(Integer.valueOf(configuration.getValue()), value);
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetInt_failure() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("int_config2");
        configuration.setValue(String.valueOf(1));
        configuration.setType("java.lang.Integer");
        configuration.setRw(0);

        when(configurationDao.selectConfigurationByName(configuration.getName()))
                .thenThrow(new ConfigurationRepositoryException());
        Integer value = 1;
        value = configurationService.getInt(configuration.getName());
        assertEquals(new Integer(1), value);
    }

    @Test
    public void testGetIntFromCache_success() throws ConfigurationRepositoryException {
        Configuration configuration1 = new Configuration();
        configuration1.setName("int_config3");
        configuration1.setValue(String.valueOf(1));
        configuration1.setType("java.lang.Integer");
        configuration1.setRw(1);

        when(configurationDao.selectConfigurationByName(configuration1.getName())).thenReturn(configuration1);

        Integer value1 = configurationService.getInt(configuration1.getName());
        assertEquals(new Integer(1), value1);

        Configuration configuration2 = new Configuration();
        configuration2.setName("int_config3");
        configuration2.setValue(String.valueOf(2));
        configuration2.setType("java.lang.Integer");
        configuration2.setRw(1);
        when(configurationDao.selectConfigurationByName(configuration2.getName())).thenReturn(configuration2);

        Integer value2 = configurationService.getInt(configuration1.getName());
        assertEquals(new Integer(1), value2);
    }

    @Test
    public void testGetInt_returnActualValue() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("int_config4");
        configuration.setValue(String.valueOf(1));
        configuration.setType("java.lang.Integer");
        configuration.setRw(1);

        when(configurationDao.selectConfigurationByName(configuration.getName())).thenReturn(configuration);

        Integer value = configurationService.getInt(configuration.getName(), 2);
        assertEquals(new Integer(1), value);
    }

    @Test
    public void testGetInt_returnDefalutValue() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("int_config5");
        configuration.setValue(String.valueOf(1));
        configuration.setType("java.lang.Integer");
        configuration.setRw(1);

        when(configurationDao.selectConfigurationByName(configuration.getName()))
                .thenThrow(new ConfigurationRepositoryException());
        Integer value = configurationService.getInt(configuration.getName(), 2);
        assertEquals(new Integer(2), value);
    }

    @Test
    public void testGetString_success() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("string_config1");
        configuration.setValue("value1");
        configuration.setType("java.lang.String");
        configuration.setRw(0);

        when(configurationDao.selectConfigurationByName(configuration.getName())).thenReturn(configuration);

        String value = configurationService.getString(configuration.getName());
        assertEquals("value1", value);
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetString_failure() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("string_config2");
        configuration.setValue("value1");
        configuration.setType("java.lang.String");
        configuration.setRw(0);

        when(configurationDao.selectConfigurationByName(configuration.getName()))
                .thenThrow(new ConfigurationRepositoryException());
        String value = configurationService.getString(configuration.getName());
        //assertEquals("", value);
        fail("Should throw exception");
    }

    @Test
    public void testGetRWStringFromCache_success() throws ConfigurationRepositoryException {
        Configuration configuration1 = new Configuration();
        configuration1.setName("string_config3");
        configuration1.setValue("value1");
        configuration1.setType("java.lang.String");
        configuration1.setRw(1);

        when(configurationDao.selectConfigurationByName(configuration1.getName())).thenReturn(configuration1);

        String value1 = configurationService.getString(configuration1.getName());
        assertEquals(configuration1.getValue(), value1);

        Configuration configuration2 = new Configuration();
        configuration2.setName("string_config3");
        configuration2.setValue("value2");
        configuration2.setType("java.lang.String");
        configuration2.setRw(1);
        when(configurationDao.selectConfigurationByName(configuration2.getName())).thenReturn(configuration2);

        String value2 = configurationService.getString(configuration1.getName());
        assertEquals("value1", value2);

    }

    @Test
    public void testGetString_returnActualValue() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("string_config4");
        configuration.setValue("value1");
        configuration.setType("java.lang.String");
        configuration.setRw(0);

        when(configurationDao.selectConfigurationByName(configuration.getName())).thenReturn(configuration);

        String value = configurationService.getString(configuration.getName(), "value2");
        assertEquals(configuration.getValue(), value);
    }

    @Test
    public void testGetString_returnDefalutValue() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("string_config5");
        configuration.setValue("value1");
        configuration.setType("java.lang.String");

        when(configurationDao.selectConfigurationByName(configuration.getName()))
                .thenThrow(new ConfigurationRepositoryException());
        String value = configurationService.getString(configuration.getName(), "value2");
        assertEquals("value2", value);
    }

    @Test
    public void testGetReadyOnlyStringFromCache() throws ConfigurationRepositoryException {
        Configuration configuration1 = new Configuration();
        configuration1.setName("string_config7");
        configuration1.setValue("value1");
        configuration1.setType("java.lang.String");
        configuration1.setRw(0);

        when(configurationDao.selectConfigurationByName(configuration1.getName())).thenReturn(configuration1);

        String value1 = configurationService.getString(configuration1.getName());
        assertEquals(configuration1.getValue(), value1);

        Configuration configuration2 = new Configuration();
        configuration2.setName("string_config7");
        configuration2.setValue("value2");
        configuration2.setType("java.lang.String");
        configuration2.setRw(0);
        when(configurationDao.selectConfigurationByName(configuration2.getName())).thenReturn(configuration2);

        String value2 = configurationService.getString(configuration1.getName());
        assertEquals("value1", value2);
    }

    @Test
    public void testGetObject_success() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("obj_config1");
        configuration.setValue("value1");
        configuration.setType("java.lang.String");
        configuration.setRw(0);

        when(configurationDao.selectConfigurationByName(configuration.getName())).thenReturn(configuration);

        Object value = configurationService.getObject(configuration.getName());
        assertEquals("value1", value.toString());
    }

    @Test
    public void testGetObject_failure() throws ConfigurationRepositoryException {
        Configuration configuration = new Configuration();
        configuration.setName("obj_config2");
        configuration.setValue("value2");
        configuration.setRw(0);

        when(configurationDao.selectConfigurationByName(configuration.getName())).thenReturn(null);
        try {
            configurationService.getObject(configuration.getName());
            fail();
        } catch (NoSuchElementException e) {
            Assert.assertTrue(true);
        } catch (Exception e1) {
            fail();
        }
    }

    @Test
    public void testNONGEOSearchObject() throws ConfigurationRepositoryException {
        //prepare
        String fuzzyName = "conf";
        Configuration configuration = new Configuration();
        configuration.setName("obj_config1");
        configuration.setValue("value1");
        configuration.setRw(0);
        configuration.setType("java.lang.String");
        ConfigurationList confList = new ConfigurationList();
        confList.getConfigurations().addAll(Arrays.asList(configuration));
        when(configurationDao.selectConfigurationByFuzzyName(fuzzyName)).thenReturn(confList);

        //verify
        Map<String, Object> result = configurationService.searchObject(fuzzyName);

        Assert.assertEquals("value1", result.get("obj_config1"));

    }

    @Test
    public void testNONGEOSearchObjectResultEmpty() throws ConfigurationRepositoryException {
        //prepare
        String fuzzyName = "conf";

        ConfigurationList confList = new ConfigurationList();
        when(configurationDao.selectConfigurationByFuzzyName(fuzzyName)).thenReturn(confList);

        //verify
        Map<String, Object> result = configurationService.searchObject(fuzzyName);

        Assert.assertEquals(0, result.size());

    }

}
