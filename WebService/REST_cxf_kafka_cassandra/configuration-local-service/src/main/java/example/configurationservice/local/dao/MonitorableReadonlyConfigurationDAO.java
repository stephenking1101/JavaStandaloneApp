package example.configurationservice.local.dao;

import example.configurationservice.local.exception.ConfigurationRepositoryException;
import example.configurationservice.local.impl.ConfigurationUpdateCallback;

public interface MonitorableReadonlyConfigurationDAO extends ReadonlyConfigurationDAO {

    void startMonitor() throws ConfigurationRepositoryException;

    /**
     * @param isStartMonitor if false, just load data once without
     *            monitoring the files
     */
    void startMonitor(boolean isStartMonitor) throws ConfigurationRepositoryException;

    void stopMonitor();

    void registerUpdateCallback(String configurationKey, ConfigurationUpdateCallback callback);
}
