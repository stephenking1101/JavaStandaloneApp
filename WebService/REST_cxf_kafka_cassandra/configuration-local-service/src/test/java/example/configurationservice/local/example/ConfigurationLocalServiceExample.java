package example.configurationservice.local.example;

import org.springframework.context.support.AbstractApplicationContext;

import example.configuration.type.Configuration;
import example.configurationservice.ConfigurationService;
import example.configurationservice.local.impl.ConfigurationLocalServiceImpl;
import example.configurationservice.local.impl.ConfigurationUpdateCallback;
import example.configurationservice.local.model.ConfigurationEvent;
import example.configurationservice.local.util.SpringContextUtil;
import example.foundation.servicediscovery.ServiceDiscovery;
import example.foundation.servicediscovery.ServiceDiscoveryFactory;

public class ConfigurationLocalServiceExample {

    private String testDp;

    public static void main(String[] args) throws InterruptedException {
        new ConfigurationLocalServiceExample().testLookupSuccessFromLocalFileConfiguredByComponent();
    }

    /**
     * Demo for looking up configuration item.
     */
    public void testLookupSuccessFromLocalFileConfiguredByComponent() throws InterruptedException {
        testDp = "DP-Authentication-Traffic";
        System.setProperty("dp.name", testDp);

        System.setProperty("spring.profiles.active", "ct-yml");

        final Configuration configuration = new Configuration();
        configuration.setName("iam.authentication.config1");
        configuration.setRw(1);

        ServiceDiscovery serviceDiscovery = ServiceDiscoveryFactory.getServiceDiscovery();
        final ConfigurationService configurationService = serviceDiscovery.discover(ConfigurationService.class, null,
                null);

        // TODO no effect to register shutdown hook?
        //((AbstractApplicationContext) SpringContextUtil.getApplicationContext()).registerShutdownHook();

        ConfigurationLocalServiceImpl impl = (ConfigurationLocalServiceImpl) configurationService;
        impl.registerUpdateCallback(configuration.getName(), new ConfigurationUpdateCallback() {
            @Override
            public void nofityChange(ConfigurationEvent event) {
                String value = configurationService.getString(configuration.getName());
                System.out.println("value of " + configuration.getName() + " : " + value);
            }
        });

        System.out.println("Sleep for a while to monitor configuration changes...");

        // sleep 5 mins for demo
        //Thread.sleep(1000 * 60 * 5);
        Thread.sleep(1000 * 5);

        System.out.println("Exiting...");

        ((AbstractApplicationContext) SpringContextUtil.getApplicationContext()).close();

        System.out.println("Done.");

    }

}
