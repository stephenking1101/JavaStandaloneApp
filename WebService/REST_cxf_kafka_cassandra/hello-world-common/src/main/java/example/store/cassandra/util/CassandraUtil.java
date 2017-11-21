package example.store.cassandra.util;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.cxf.common.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import example.store.cassandra.entity.AbstractExtendableEntity;
import example.store.cassandra.lucene.QueryCountParam;
import example.store.cassandra.lucene.QueryParam;

public class CassandraUtil {
	private static Logger logger = LoggerFactory.getLogger(CassandraUtil.class);
	private static final String CASS_CACHE_TABLE_PREFIX = "cass.cache.table.";
    private static final String CASS_CACHE_QUERY_RESULT_PREFIX = "cass.cache.query.result.";
    private static final String CASS_CACHE_QUERY_STATUS_PREFIX = "cass.cache.query.status.";
    private static final String CASS_CACHE_QUERY_COUNT_PREFIX = "cass.cache.query.count.";
    private static final String CASS_CACHE_QUERY_COUNT_STATUS_PREFIX = "cass.cache.query.count.status.";
    public static final String EXTENSION_FIELD_PREFIX = AbstractExtendableEntity.EXTENSION_FIELD_NAME + ".";

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String getQueryParamCacheResultKey(QueryParam queryParam) {
        return getQueryParamCacheKey(queryParam, CASS_CACHE_QUERY_RESULT_PREFIX);
    }

    public static String getQueryParamCacheStatusKey(QueryParam queryParam) {
        return getQueryParamCacheKey(queryParam, CASS_CACHE_QUERY_STATUS_PREFIX);
    }

    public static String getQueryCountParamCacheKey(QueryCountParam queryCountParam, String tableName) {
        return getQueryCountParamCacheKey(queryCountParam, CASS_CACHE_QUERY_COUNT_PREFIX, tableName);
    }

    public static String getQueryCountParamCacheStatusKey(QueryCountParam queryCountParam, String tableName) {
        return getQueryCountParamCacheKey(queryCountParam, CASS_CACHE_QUERY_COUNT_STATUS_PREFIX, tableName);
    }

    private static String getQueryParamCacheKey(QueryParam queryParam, String prefix) {
        int queryHashcode = queryParam.hashCode();
        String cacheKey = prefix + queryHashcode;
        log(queryParam, cacheKey);
        return cacheKey;
    }

    private static String getQueryCountParamCacheKey(QueryCountParam queryCountParam, String prefix, String tableName) {
        int queryHashcode = queryCountParam.hashCode();
        String cacheKey = prefix + tableName + "." + queryHashcode;
        log(queryCountParam, cacheKey);
        return cacheKey;
    }

    /**
     * extension attribute's expression should be : extension.<key>
     *
     * @param attributes
     * @return
     */
    public static Set<String> getExtensionKey(List<String> attributes, Class entityClazz) {
        Set<String> result = Sets.newHashSet();
        if (!isExtendableEntity(entityClazz) || CollectionUtils.isEmpty(attributes)) {
            return result;
        }
        for (String attr : attributes) {
            if (attr.startsWith(EXTENSION_FIELD_PREFIX)) {
                logger.debug("find extension attribute: {}", attr);
                String key = getExtensionKey(attr);
                logger.debug("key in extension is: {}", key);
                result.add(key);
            }
        }
        return result;
    }

    public static boolean isExtensionField(String field, Class entityClazz) {
        return isExtendableEntity(entityClazz) && field.startsWith(EXTENSION_FIELD_PREFIX);
    }

    public static boolean isExtendableEntity(Class entityClazz) {
        return AbstractExtendableEntity.class.isAssignableFrom(entityClazz);
    }

    public static String getExtensionKey(String attr) {
        return attr.substring(EXTENSION_FIELD_PREFIX.length());
    }

    private static void log(QueryParam queryParam, String queryKey) {
        if (logger.isDebugEnabled()) {
            try {
                String queryParamJson = objectMapper.writeValueAsString(queryParam);
                logger.debug("The redis key of {} is:{}", queryParamJson, queryKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void log(QueryCountParam queryCountParam, String queryKey) {
        if (logger.isDebugEnabled()) {
            try {
                String queryCountParamJson = objectMapper.writeValueAsString(queryCountParam);
                logger.debug("The redis key of {} is:{}", queryCountParamJson, queryKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getCassTableName(Class entityClazz) {
        if (!entityClazz.isAnnotationPresent(Table.class)) {
            String msg = "Table annotation missing in class ";
            return handelTableNameNotFound(entityClazz, msg);
        }

        Table tableAnnotation = (Table) entityClazz.getAnnotation(Table.class);
        String tableName = tableAnnotation.name();
        if (Strings.isNullOrEmpty(tableName)) {
            String msg = "Table name is empty in class ";
            handelTableNameNotFound(entityClazz, msg);
        }
        return tableName;
    }

    private static String handelTableNameNotFound(Class entityClazz, String msg) {
        throw new IllegalArgumentException(msg + entityClazz.getSimpleName());
    }

    public static String getTableAssociatedCacheRedisKey(String tableName) {
        return CASS_CACHE_TABLE_PREFIX + tableName;
    }

    public static void destoryThreadPool(ExecutorService service) {
        if (service == null) {
            return;
        }
        service.shutdown();
        try {
            if (!service.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.info("forse shutdown thread pool");
                service.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("shutdown thread pool get interrupted", e);
        }
    }
}
