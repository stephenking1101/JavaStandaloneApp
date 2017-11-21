package example.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import example.exception.AppException;

/**
 * JacksonMapConvertor using version 2.0.2 jackson 3pp libs
 *
 * Serialize map into JSON string, and deserialize vice versa. The
 * value of the map must be in scalar types (String, Boolean, Number,
 * nulls)
 */
public class JacksonMapConvertor {

    protected final static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setAnnotationIntrospector(new AnnotationIntrospectorPair(new JacksonAnnotationIntrospector(),
                new JaxbAnnotationIntrospector(TypeFactory.defaultInstance())));
    }

    public static String toStringJson(Map<String, ?> map) throws AppException {
        try {
            return map == null ? "" : mapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new AppException(e);
        }
    }

    static public byte[] toJson(Map<String, Object> map) throws AppException {
        try {
            return mapper.writeValueAsBytes(map);
        } catch (Exception e) {
            throw new AppException(e);
        }
    }

    @SuppressWarnings("unchecked")
    static public Map<String, Object> fromJson(byte[] json) throws AppException {
        try {
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new AppException(e);
        }
    }

    @SuppressWarnings("unchecked")
    static public Map<String, String> fromJson(String json) throws AppException {
        if (StringUtils.isBlank(json)) {
        	return null;
        }

    	try {
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new AppException(e);
        }
    }

    @SuppressWarnings("unchecked")
    static public Map<String, List<Object>> mapFromJson(String json) throws AppException {
        try {
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new AppException(e);
        }
    }

    @SuppressWarnings("unchecked")
	static public Map<String, Object> objectMapFromJson(String json) throws AppException {
        if (StringUtils.isBlank(json)) {
            return new HashMap<String, Object>();
        }
        try {
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new AppException(e);
        }
    }

    static public String objectToJsonString(Object object) throws AppException {
    	try {
//    		StringWriter writer = new StringWriter();
//    		JsonGenerator jsonGenerator = mapper.getJsonFactory().createJsonGenerator(writer);
//    		mapper.writeValue(jsonGenerator, object);
//    		jsonGenerator.close();
//            String json = writer.toString();
//            writer.close();
//            return json;
    		return mapper.writeValueAsString(object);
    	} catch (Exception e) {
    		throw new AppException(e);
    	}
    }

    static public <T> T jsonStringToObject(String json, Class<T> responseClass) throws AppException {
    	try {
    		return mapper.readValue(json, responseClass);
    	} catch (Exception e) {
    		throw new AppException(e);
    	}
    }
}
