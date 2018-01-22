package example.exception;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.google.common.collect.Maps;

import example.util.JacksonMapConvertor;

public class ErrorConfigurationHelper {
	private static Logger logger = LoggerFactory.getLogger(AppCommonConstants.COMMON_LOGGER_NAME);

    private static String filePath = "classpath:aa_error_config.json";

    private static Map<String, Object> errConfigMap = Maps.newHashMap();

    private static ResourceLoader resourceLoader = new DefaultResourceLoader();

    static {
        errConfigMap = getConfigEntities();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getError(String errorCode) {
        if (errConfigMap == null) {
            errConfigMap = getConfigEntities();
        }

        if (errConfigMap.get(errorCode) == null) {
            logger.error("Configuration not found for this error {}", errorCode);
            return null;
        }

        // deep clone
        Map<String, Object> retMap = Maps.newHashMap();
        retMap.putAll((Map<String, Object>) errConfigMap.get(errorCode));
        return retMap;

    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getConfigEntities() {
        try {
            String content = readFile(filePath);
            return JacksonMapConvertor.jsonStringToObject(content, Map.class);
        } catch (Exception e) {
            logger.error("json file {} parse fail:", filePath, e);
            return null;
        }
    }

    private static String readFile(String path) {
        Resource resource = resourceLoader.getResource(path);
        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                buffer.append(tempString);
            }
        } catch (IOException e) {
            logger.error("{} read fail:", path, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("{} close fail:", path, e);
                }
            }
        }
        return buffer.toString();
    }
}
