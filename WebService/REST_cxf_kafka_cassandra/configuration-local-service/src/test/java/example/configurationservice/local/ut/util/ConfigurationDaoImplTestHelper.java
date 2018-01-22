package example.configurationservice.local.ut.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import example.configuration.type.Configuration;
import example.configuration.type.ConfigurationList;
import example.configurationservice.local.locator.FileLocator;

public class ConfigurationDaoImplTestHelper {

    public static final int TEST_ITMES_COUNT = 8;

    public static FileLocator getMockFileLocator(String... fileUrl) {
        List<String> files = Arrays.asList(fileUrl);
        return getMockFileLocator(files);
    }

    public static FileLocator getMockFileLocator(List<String> fileUrls) {
        FileLocator fileLocator = mock(FileLocator.class);
        when(fileLocator.locateFileUrl()).thenReturn(fileUrls);
        return fileLocator;
    }

    public static Configuration getTestConfiguration() {
        Configuration expectedConfiguration = new Configuration();
        expectedConfiguration.setName("iam.authentication.config1");
        expectedConfiguration.setValue("value1");
        expectedConfiguration.setType("java.lang.String");
        expectedConfiguration.setRw(1);
        return expectedConfiguration;
    }

    public static Configuration getTestConfigurationIntValue() {
        Configuration expectedConfiguration = new Configuration();
        expectedConfiguration.setName("iam.authentication.config2");
        expectedConfiguration.setValue("1001");
        expectedConfiguration.setType("java.lang.Integer");
        expectedConfiguration.setRw(1);
        return expectedConfiguration;
    }

    public static Configuration getTestConfigurationIntValueStringQuoted() {
        Configuration expectedConfiguration = new Configuration();
        expectedConfiguration.setName("iam.authentication.config3");
        expectedConfiguration.setValue("1001");
        expectedConfiguration.setType("java.lang.Integer");
        expectedConfiguration.setRw(1);
        return expectedConfiguration;
    }

    public static Configuration getTestConfigurationInFile2() {
        Configuration expectedConfiguration = new Configuration();
        expectedConfiguration.setName("iam.authentication.configInFile2");
        expectedConfiguration.setValue("value1InFile2");
        expectedConfiguration.setType("java.lang.String");
        expectedConfiguration.setRw(1);
        return expectedConfiguration;
    }

    public static ConfigurationList getFuzzyTestConfigurationList() {
        ConfigurationList configList = new ConfigurationList();
        List<Configuration> list = new ArrayList<Configuration>();

        Configuration expectedConfiguration = new Configuration();
        expectedConfiguration.setName("iam.authentication.configFuzzyTest1");
        expectedConfiguration.setValue("valueFuzzyTest1");
        expectedConfiguration.setType("java.lang.String");
        expectedConfiguration.setRw(1);

        Configuration expectedConfiguration2 = new Configuration();
        expectedConfiguration2.setName("iam.authentication.configFuzzyTest2");
        expectedConfiguration2.setValue("valueFuzzyTest2");
        expectedConfiguration2.setType("java.lang.String");
        expectedConfiguration2.setRw(1);

        list.add(expectedConfiguration);
        list.add(expectedConfiguration2);

        configList.getConfigurations().addAll(list);

        return configList;
    }

}
