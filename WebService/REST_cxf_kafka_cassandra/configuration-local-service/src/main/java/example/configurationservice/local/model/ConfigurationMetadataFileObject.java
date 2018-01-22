package example.configurationservice.local.model;

import java.util.Map;

public class ConfigurationMetadataFileObject {

    Map<String, ConfigurationMetadata> metadata;

    public Map<String, ConfigurationMetadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, ConfigurationMetadata> metadata) {
        this.metadata = metadata;
    }
}
