package example.util;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import example.exception.AppException;

public class JacksonMapConvertor {
	protected static final ObjectMapper mapper = new ObjectMapper();

    public JacksonMapConvertor() {
    }

    public static String toStringJson(Map<String, ?> map) throws AppException {
        try {
            return map == null ? "" : mapper.writeValueAsString(map);
        } catch (Exception var2) {
            throw new AppException(var2);
        }
    }

    public static byte[] toJson(Map<String, Object> map) throws AppException {
        try {
            return mapper.writeValueAsBytes(map);
        } catch (Exception var2) {
            throw new AppException(var2);
        }
    }

    public static Map<String, Object> fromJson(byte[] json) throws AppException {
        try {
            return (Map)mapper.readValue(json, Map.class);
        } catch (Exception var2) {
            throw new AppException(var2);
        }
    }

    public static Map<String, String> fromJson(String json) throws AppException {
        if (StringUtils.isBlank(json)) {
            return null;
        } else {
            try {
                return (Map)mapper.readValue(json, Map.class);
            } catch (Exception var2) {
                throw new AppException(var2);
            }
        }
    }

    public static Map<String, List<Object>> mapFromJson(String json) throws AppException {
        try {
            return (Map)mapper.readValue(json, Map.class);
        } catch (Exception var2) {
            throw new AppException(var2);
        }
    }

    public static Map<String, Object> objectMapFromJson(String json) throws AppException {
        if (StringUtils.isBlank(json)) {
            return new HashMap();
        } else {
            try {
                return (Map)mapper.readValue(json, Map.class);
            } catch (Exception var2) {
                throw new AppException(var2);
            }
        }
    }

    public static String objectToJsonString(Object object) throws AppException {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception var2) {
            throw new AppException(var2);
        }
    }

    public static <T> T jsonStringToObject(String json, Class<T> responseClass) throws AppException {
        try {
            return mapper.readValue(json, responseClass);
        } catch (Exception var3) {
            throw new AppException(var3);
        }
    }

    static {
        mapper.setAnnotationIntrospector(AnnotationIntrospector.pair(new JacksonAnnotationIntrospector(), new JaxbAnnotationIntrospector()));
    }
}
