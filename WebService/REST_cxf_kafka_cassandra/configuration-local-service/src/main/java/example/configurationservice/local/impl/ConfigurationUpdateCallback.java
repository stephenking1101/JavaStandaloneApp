package example.configurationservice.local.impl;

import example.configurationservice.local.model.ConfigurationEvent;

public interface ConfigurationUpdateCallback {

    void nofityChange(ConfigurationEvent configurationEvent);
}
