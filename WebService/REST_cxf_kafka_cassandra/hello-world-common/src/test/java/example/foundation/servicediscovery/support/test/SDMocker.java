package example.foundation.servicediscovery.support.test;

import java.util.List;

import example.foundation.servicediscovery.ServiceDiscoveryFactory;
import example.foundation.servicediscovery.support.AddressProvider;
import example.foundation.servicediscovery.support.ServiceAddressFinder;
import example.foundation.servicediscovery.support.ServiceDefinition;
import example.foundation.servicediscovery.support.util.Service;

public class SDMocker {
    public static final MockedService Test_Service;

    static {
        Test_Service = new MockedService("test_service");
        Test_Service.setServiceUriWithContext("test");
    }

    private static InMemoryAddressFinder SDFinder = null;

    public static synchronized InMemoryAddressFinder getSD() {
        if (SDFinder == null) {
            SDFinder = new InMemoryAddressFinder();
            ServiceDiscoveryFactory.getMocks().put(ServiceAddressFinder.class, SDFinder);
        }
        return SDFinder;
    }

    public static synchronized void resetSD() {
        SDFinder = null;
        ServiceDiscoveryFactory.getMocks().remove(ServiceAddressFinder.class);
    }

    public static class MockedService extends ServiceDefinition {
        private int port = 0;

        protected MockedService(String serviceName) {
            super(ServiceDefinition.getInstance(serviceName));
            if ((this.getServiceName() == null) && (serviceName != null)) {
                this.setServiceName(serviceName);
            }
        }

        private Service.STATUS toStatus(boolean passing) {
            return passing ? Service.STATUS.ACTIVE : Service.STATUS.DEACTIVE;
        }

        public void registerOnce() {
            if (this.port <= 0) {
                port = AutoPorts.getNextPort();
                registerUnique(port, true);
            }
        }

        public void register(int localPort, boolean passing) {
            this.port = localPort;
            getSD().putLocalService(getServiceName(), localPort, getServiceUriFormat(), toStatus(passing));
        }

        public void register(String prefixUrl, boolean passing) throws Exception {
            this.port = Integer.valueOf(prefixUrl.split(":")[1]);
            getSD().putService(getServiceName(), prefixUrl, getServiceUriFormat(), toStatus(passing));
        }

        public void register(Service service) {
            getSD().put(service);
            this.port = Integer.valueOf(service.getPrefix_URI().split(":")[1]);
        }

        public void registerUnique(int localPort, boolean passing) {
            deregister();
            register(localPort, passing);
        }

        public void registerUnique(String prefixUrl, boolean passing) throws Exception {
            deregister();
            register(prefixUrl, passing);
        }

        public void registerUnique(Service service) {
            deregister();
            register(service);
        }

        public void deregister() {
            SDMocker.getSD().remove(getServiceName());
            this.port = 0;
        }

        public AddressProvider getAddressProvider() {
            return new AddressProvider(getServiceName());
        }

        public int getLatestPort() {
            return this.port;
        }

        public void updateServiceStatus(Service.STATUS status) {
            List<Service> serviceList = getRegisteredService();
            for (Service service : serviceList) {
                service.setStatus(status);
                register(service);
            }
        }

        public List<Service> getRegisteredService() {
            return getSD().getAll(getServiceName());
        }

        public String getActualUrl() {
            return getAddressProvider().getAddress();
        }

        @Override
        public String getHealthCheckUri() {
            return getActualUrl() + healthCheckPostFix;
        }
    }
}
