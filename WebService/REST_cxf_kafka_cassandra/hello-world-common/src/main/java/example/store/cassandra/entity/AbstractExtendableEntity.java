package example.store.cassandra.entity;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.collect.Maps;

import example.store.cassandra.exception.StoreCassandraException;

public abstract class AbstractExtendableEntity {
	private static ObjectMapper objectMapper = new ObjectMapper();

    public AbstractExtendableEntity() {
    }

    public abstract Map<String, String> getRawExtension();

    public abstract void setRawExtension(Map<String, String> var1);

    public Map<String, Object> getExtension() {
        if (this.getRawExtension() == null) {
            return null;
        } else {
            Map<String, Object> result = Maps.newHashMap();
            Iterator i$ = this.getRawExtension().entrySet().iterator();

            while(i$.hasNext()) {
                Entry<String, String> entry = (Entry)i$.next();
                if (entry.getValue() == null) {
                    result.put(entry.getKey(), (Object)null);
                } else {
                    try {
                        result.put(entry.getKey(), objectMapper.readValue((String)entry.getValue(), Object.class));
                    } catch (Exception var5) {
                        throw new StoreCassandraException("parse json error", var5);
                    }
                }
            }

            return result;
        }
    }

    public void setExtension(Map<String, Object> extension) {
        if (extension != null) {
            Map<String, String> rawExtension = Maps.newHashMap();
            Iterator i$ = extension.entrySet().iterator();

            while(i$.hasNext()) {
                Entry<String, Object> entry = (Entry)i$.next();
                if (entry.getValue() == null) {
                    rawExtension.put(entry.getKey(), (String)null);
                } else {
                    try {
                        rawExtension.put(entry.getKey(), objectMapper.writeValueAsString(entry.getValue()));
                    } catch (Exception var6) {
                        throw new StoreCassandraException("generate json error", var6);
                    }
                }
            }

            this.setRawExtension(rawExtension);
        }
    }
}
