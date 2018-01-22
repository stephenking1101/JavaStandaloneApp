package example.configurationservice.local.metadata;

import example.configurationservice.local.exception.ConfigurationRepositoryException;
import example.configurationservice.local.model.ConfigurationMetadata;

public interface ConfigurationMetadataRepository {

    void startMonitor() throws ConfigurationRepositoryException;

    void startMonitor(boolean isStartMonitor) throws ConfigurationRepositoryException;

    void stopMonitor();

    ConfigurationMetadata selectConfigurationMetadataByName(String name) throws ConfigurationRepositoryException;
}
