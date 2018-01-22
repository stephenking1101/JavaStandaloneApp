package example.configurationservice.local.ct;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("ct-yml")
public class ConfigurationLocalServiceImplYamlCT extends AbstractMonitorFeatureTest {

    protected static Logger logger = LoggerFactory.getLogger(ConfigurationLocalServiceImplYamlCT.class);

    @Before
    public void before() {
        prepareTestData();
    }

    @Test
    public void testYamlConfigurationFileMonitorCreateEvent() throws Exception {
        logger.debug("Start case: testConfigurationFileMonitorCreateEvent");

        doTestConfigurationFileMonitorCreateEvent("ct-yml", "yml");

        logger.debug("End case: testConfigurationFileMonitorCreateEvent");
    }

    @Test
    public void testPropertiesConfigurationFileMonitorUpdateEvent() throws Exception {
        doTestConfigurationFileMonitorUpdateEvent("ct-yml", "yml");
    }

}
