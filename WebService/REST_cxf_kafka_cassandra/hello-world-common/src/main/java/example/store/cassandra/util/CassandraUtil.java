package example.store.cassandra.util;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import example.store.cassandra.lucene.QueryParam;

public class CassandraUtil {
	private static Logger logger = LoggerFactory.getLogger(CassandraUtil.class);
    private static final String CASS_CACHE_TABLE_PREFIX = "cass.cache.table.";
    private static final String CASS_CACHE_QUERY_RESULT_PREFIX = "cass.cache.query.result.";
    private static final String CASS_CACHE_QUERY_STATUS_PREFIX = "cass.cache.query.status.";
    private static ObjectMapper objectMapper = new ObjectMapper();

    public CassandraUtil() {
    }

    public static String getQueryParamCacheResultKey(QueryParam queryParam) {
        int queryHashcode = queryParam.hashCode();
        String queryKey = "cass.cache.query.result." + queryHashcode;
        log(queryParam, queryKey);
        return queryKey;
    }

    public static String getQueryParamCacheStatusKey(QueryParam queryParam) {
        int queryHashcode = queryParam.hashCode();
        String statusKey = "cass.cache.query.status." + queryHashcode;
        log(queryParam, statusKey);
        return statusKey;
    }

    private static void log(QueryParam queryParam, String queryKey) {
        if (logger.isDebugEnabled()) {
            try {
                String queryParamJson = objectMapper.writeValueAsString(queryParam);
                logger.debug("The redis key of {} is:{}", queryParamJson, queryKey);
            } catch (IOException var3) {
                var3.printStackTrace();
            }
        }

    }

    public static String getTableAssociatedCacheRedisKey(String tableName) {
        return "cass.cache.table." + tableName;
    }

    public static void destoryThreadPool(ExecutorService service) {
        if (service != null) {
            service.shutdown();

            try {
                if (!service.awaitTermination(10L, TimeUnit.SECONDS)) {
                    logger.info("forse shutdown thread pool");
                    service.shutdownNow();
                }
            } catch (InterruptedException var2) {
                logger.error("shutdown thread pool get interrupted", var2);
            }

        }
    }
}
