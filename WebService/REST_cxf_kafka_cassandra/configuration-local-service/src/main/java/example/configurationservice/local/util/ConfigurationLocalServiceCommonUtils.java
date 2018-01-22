package example.configurationservice.local.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import example.configurationservice.local.model.ConfigurationEvent;
import example.configurationservice.local.model.ConfigurationEventTypeEnum;
import example.foundation.servicediscovery.SystemInfo;

public class ConfigurationLocalServiceCommonUtils {
    private static Logger logger = LoggerFactory.getLogger(getLoggerName(ConfigurationLocalServiceCommonUtils.class));

    //protected static boolean testMode = false;

    public static boolean isTestMode() {
        boolean testMode = false;
        SystemInfo.SystemMode mode = SystemInfo.getSystemMode();
        if (SystemInfo.SystemMode.Testing.equals(mode)) {
            testMode = true;
        }
        return testMode;
    }

    /**
     * @param specificClass
     * @return
     */
    public static String getLoggerName(Class<?> specificClass) {
        return ConfigurationLocalServiceConstants.CONFIGURATION_LOCAL_SERVICE_LOGGER_NAME + "."
                + specificClass.getSimpleName();
    }

    public static long getDefaultInterval() {
        long interval;
        if (isTestMode()) {
            interval = ConfigurationLocalServiceConstants.DEFAULT_MONITOR_INTERVAL_FOR_TEST;
        } else {
            interval = ConfigurationLocalServiceConstants.DEFAULT_MONITOR_INTERVAL;
        }
        return interval;

    }

    public static List<ConfigurationEvent> diffDataRepo(Map<String, Object> oldData, Map<String, Object> newData) {
        List<ConfigurationEvent> eventList = new ArrayList<ConfigurationEvent>();
        if ((oldData == null) && (newData == null)) {
            logger.debug("Both old and new data map are null, no configuration events");
        } else if ((oldData != null) && (newData == null)) {
            logger.debug("Old data map is not null but new data map is null, make keys in old data map as delete event");
            eventList.addAll(constructConfigurationEvent(oldData, ConfigurationEventTypeEnum.DELETE));
        } else if ((oldData == null) && (newData != null)) {
            logger.debug("Old data map is null but new data map is not null, make keys in new data map as create event");
            eventList.addAll(constructConfigurationEvent(newData, ConfigurationEventTypeEnum.CREATE));
        } else {
            logger.debug("Both old data map and new data map are not null, get the difference between them");
            MapDifference<String, Object> differenceMap = Maps.difference(oldData, newData);
            eventList.addAll(constructConfigurationEvent(differenceMap.entriesOnlyOnRight(),
                    ConfigurationEventTypeEnum.CREATE));
            eventList.addAll(constructConfigurationEvent(differenceMap.entriesOnlyOnLeft(),
                    ConfigurationEventTypeEnum.DELETE));
            eventList.addAll(constructConfigurationEvent(differenceMap.entriesDiffering(),
                    ConfigurationEventTypeEnum.UPDATE));
        }
        return eventList;
    }

    private static List<ConfigurationEvent> constructConfigurationEvent(Map<String, ?> keyMap,
            ConfigurationEventTypeEnum eventType) {
        List<ConfigurationEvent> eventList = new ArrayList<ConfigurationEvent>();

        if (!CollectionUtils.isEmpty(keyMap)) {
            for (String configurationKey : keyMap.keySet()) {
                ConfigurationEvent event = new ConfigurationEvent();
                event.setConfigurationKey(configurationKey);
                event.setEventType(eventType);
                logger.debug("Add configuration event: {} into list", event);
                eventList.add(event);
            }
        }

        return eventList;
    }

}
