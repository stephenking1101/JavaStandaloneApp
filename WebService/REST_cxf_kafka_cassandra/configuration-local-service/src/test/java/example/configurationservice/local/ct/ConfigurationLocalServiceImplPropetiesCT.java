package example.configurationservice.local.ct;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("ct-properties")
public class ConfigurationLocalServiceImplPropetiesCT extends AbstractMonitorFeatureTest {

    protected static Logger logger = LoggerFactory.getLogger(ConfigurationLocalServiceImplPropetiesCT.class);

    @Before
    public void before() {
        System.setProperty("service.base.dir", "src/test/resources/test-files/ct/metadata");
        prepareTestData();
    }

    @Test
    public void testPropertiesConfigurationFileMonitorCreateEvent() throws Exception {
        doTestConfigurationFileMonitorCreateEvent("ct-properties", "properties");
    }

    @Test
    public void testPropertiesConfigurationFileMonitorUpdateEvent() throws Exception {
        doTestConfigurationFileMonitorUpdateEvent("ct-properties", "properties");
    }
}
