package example.configurationservice.local.model;

public class ConfigurationEvent {
    private ConfigurationEventTypeEnum eventType;
    private String configurationKey;

    public ConfigurationEventTypeEnum getEventType() {
        return eventType;
    }

    public void setEventType(ConfigurationEventTypeEnum eventType) {
        this.eventType = eventType;
    }

    public String getConfigurationKey() {
        return configurationKey;
    }

    public void setConfigurationKey(String configurationKey) {
        this.configurationKey = configurationKey;
    }

    @Override
    public String toString() {
        return "ConfigurationEvent{eventType='" + eventType + "', configurationKey='" + configurationKey + "'}";
    }

}
