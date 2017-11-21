package example.store.cassandra.entity;

import java.util.Map;

import example.store.cassandra.exception.StoreCassandraException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

/**
 * Entity that extend this class must have a field name rawExtension :
 * Map<String,String> and override setRawExtension and getRawExtension
 */
 //The class must provide a default constructor. The default constructor is allowed to be non-public, provided that the security manager, if any, grants the mapper access to it via reflection.
public abstract class AbstractExtendableEntity {

    public static final String EXTENSION_FIELD_NAME = "extension";

    public static final String RAW_EXTENSION_FIELD_NAME = "rawExtension";

    private static ObjectMapper objectMapper = new ObjectMapper();

    abstract public Map<String, String> getRawExtension();

    abstract public void setRawExtension(Map<String, String> extension);

    public Map<String, Object> getExtension() {
        if (getRawExtension() == null) {
            return null;
        }
        Map<String, Object> result = Maps.newHashMap();
        for (Map.Entry<String, String> entry : getRawExtension().entrySet()) {
			if (entry.getValue() == null) {
				result.put(entry.getKey(), null);
				continue;
			}
            try {
                result.put(entry.getKey(), objectMapper.readValue(entry.getValue(), Object.class));
            } catch (Exception e) {
                throw new StoreCassandraException("parse json error", e);
            }
        }
        return result;
    }

    public void setExtension(Map<String, Object> extension) {
        if (extension == null) {
            return;
        }
        Map<String, String> rawExtension = Maps.newHashMap();
        for (Map.Entry<String, Object> entry : extension.entrySet()) {
			if (entry.getValue() == null) {
				rawExtension.put(entry.getKey(), null);
				continue;
			}
            try {
                rawExtension.put(entry.getKey(), objectMapper.writeValueAsString(entry.getValue()));
            } catch (Exception e) {
                throw new StoreCassandraException("generate json error", e);
            }

        }
        setRawExtension(rawExtension);
    }

}
