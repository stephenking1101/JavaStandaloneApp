package example.configurationservice.local.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;

import example.configuration.type.Configuration;
import example.configuration.type.ConfigurationList;
import example.configurationservice.ConfigurationService;
import example.configurationservice.local.dao.MonitorableReadonlyConfigurationDAO;
import example.configurationservice.local.dao.ReadonlyConfigurationDAO;
import example.configurationservice.local.exception.ConfigurationRepositoryException;
import example.configurationservice.local.type.UnexistedConfiguration;
import example.configurationservice.local.util.ConfigurationLocalServiceCommonUtils;
import example.foundation.servicediscovery.SystemInfo;

/**
 * The purpose of this class is ... local configuration files data
 * access object
 */
@Component("configurationServiceImpl")
public class ConfigurationLocalServiceImpl implements ConfigurationService {

    private Logger logger = LoggerFactory
            .getLogger(ConfigurationLocalServiceCommonUtils.getLoggerName(ConfigurationLocalServiceImpl.class));

    private static final String ERROR_TPL = "Failed to load configuration item %s from local configuration files.";
    private static final String NO_SUCH_ELEMENT_EXCEPTION_MSG = "' doesn't map to an existing object.";

    /**
     * The cache timeout value for RW configuration
     */
    private static long DEFAULT_CACHE_TIME = 30000;

    private long cacheTime = DEFAULT_CACHE_TIME;

    private boolean cacheEnabled = true;

    /**
     * This is for readonly configurations which does not need to
     * reload.
     */
    private Map<String, Configuration> readOnlyConfigurationCache = new ConcurrentHashMap<String, Configuration>(1000);

    private LoadingCache<String, Configuration> cache = null;

    private Configuration doLoad(final String key) {
        Configuration configuration = readOnlyConfigurationCache.get(key);

        if (configuration == null) {
            configuration = lookupFromDb(key);
            if (isReadonlyConfiguration(configuration)) {
                readOnlyConfigurationCache.put(key, configuration);
            }
        }

        return configuration;
    }

    private ReadonlyConfigurationDAO configurationDao = null;

    public ConfigurationLocalServiceImpl() {
        initCache();
    }

    private void initCache() {
        cacheTime = DEFAULT_CACHE_TIME;
        SystemInfo.SystemMode mode = SystemInfo.getSystemMode();
        logger.info("Initializing cache: system mode:" + mode);
        if (SystemInfo.SystemMode.Testing.equals(mode)) {
            cacheTime = 0;
        }

        cache = CacheBuilder.newBuilder().maximumSize(Long.MAX_VALUE).expireAfterWrite(cacheTime, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<String, Configuration>() {
                    @Override public Configuration load(final String key) throws Exception {
                        return doLoad(key);
                    }

                });
    }

    public void setConfigurationDao(MonitorableReadonlyConfigurationDAO configurationDao)
            throws ConfigurationRepositoryException {
        this.configurationDao = configurationDao;
        startMonitor();
    }

    /**
     * Lookup object matching name in configuration repository. The
     * object will be cached in memory for 30 seconds to reduce
     * repository access.
     *
     * @param aName name
     * @return object
     * @throws NoSuchElementException if match was not found.
     */
    private Configuration configLookup(String aName) {
        Configuration configuration;
        try {
            if (isCacheEnabled()) {
                configuration = cache.get(aName);
            } else {
                configuration = doLoad(aName);
            }

            if (configuration instanceof UnexistedConfiguration) {
                NoSuchElementException noSuchElementException = new NoSuchElementException(
                        '\'' + aName + NO_SUCH_ELEMENT_EXCEPTION_MSG);

                if (((UnexistedConfiguration) configuration).getException() != null) {
                    noSuchElementException
                            .setStackTrace(((UnexistedConfiguration) configuration).getException().getStackTrace());
                }
                throw noSuchElementException; //NOSONAR
            }
            return configuration;
        } catch (ExecutionException e) {

            NoSuchElementException noSuchElementException = new NoSuchElementException(
                    '\'' + aName + NO_SUCH_ELEMENT_EXCEPTION_MSG);

            noSuchElementException.setStackTrace(e.getStackTrace());
            throw noSuchElementException; //NOSONAR
        }
    }

    private boolean isReadonlyConfiguration(Configuration configuration) {
        boolean isReadonly = false;
        if (!(configuration instanceof UnexistedConfiguration)) {
            if ((configuration.getRw() != null) && (configuration.getRw() == 0)) {
                isReadonly = true;
            }
        }
        return isReadonly;
    }

    /**
     * query from repository
     *
     * @param aName name of configuration item
     * @return result from lodal repository, UnexistedConfiguration if
     *         record not found or any error happened
     */
    private Configuration lookupFromDb(String aName) {

        try {
            Configuration configuration = configurationDao.selectConfigurationByName(aName);
            if (configuration == null) {
                return new UnexistedConfiguration("Configuration item " + aName + " not found from database.");
            }

            return configuration;
        } catch (ConfigurationRepositoryException e) {
            String errorMsg = String.format(ERROR_TPL, aName);
            logger.warn(errorMsg);
            return new UnexistedConfiguration(errorMsg, e);
        }
    }

    @SuppressWarnings("unchecked")
    private <V> V getPrimitiveValue(String key, String defaultType) {
        try {
            Configuration configuration = configLookup(key);
            String type = getFinalType(defaultType, configuration);
            return (V) TypeConverter.convert(type, configuration.getValue());
        } catch (NoSuchElementException e) {
            if (logger.isWarnEnabled()) {
                logger.warn(String.format(ERROR_TPL, key));
            }
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private <V> V getPrimitiveValue(String key, V defaultValue, String defaultType) {
        try {
            Configuration configuration = configLookup(key);
            String type = getFinalType(defaultType, configuration);
            return (V) TypeConverter.convert(type, configuration.getValue());
        } catch (NoSuchElementException e) {
            return defaultValue;
        }
    }

    private String getFinalType(String defaultType, Configuration configuration) {
        String type;
        if ((defaultType == null) || StringUtils.isNotBlank(configuration.getType())) {
            type = configuration.getType();
        } else {
            type = defaultType;
        }
        return type;
    }

    @Override
    public boolean getBoolean(String key) {
        return (Boolean) getPrimitiveValue(key, "java.lang.Boolean");
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return getPrimitiveValue(key, defaultValue, "java.lang.Boolean");
    }

    @Override
    public double getDouble(String key) {
        return (Double) getPrimitiveValue(key, "java.lang.Double");
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return getPrimitiveValue(key, defaultValue, "java.lang.Double");
    }

    @Override
    public int getInt(String key) {
        return (Integer) getPrimitiveValue(key, "java.lang.Integer");
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return getPrimitiveValue(key, defaultValue, "java.lang.Integer");
    }

    @Override
    public String getString(String key) {
        return String.valueOf(getPrimitiveValue(key, "java.lang.String"));
    }

    @Override
    public String getString(String key, String defaultValue) {
        return getPrimitiveValue(key, defaultValue, "java.lang.String");
    }

    @Override
    public Object getObject(String key) {
        try {
            return getPrimitiveValue(key, null);
        } catch (NoSuchElementException e) {
            if (logger.isWarnEnabled()) {
                logger.warn(String.format(ERROR_TPL, key));
            }
            throw e;
        }
    }

    @Override
    public Map<String, Object> searchObject(String key) {

        try {
            Map<String, Object> result = new HashMap<String, Object>();
            ConfigurationList configList = configurationDao.selectConfigurationByFuzzyName(key);
            if (configList != null) {
                for (Configuration conf : configList.getConfigurations()) {
                    result.put(conf.getName(), TypeConverter.convert(conf.getType(), conf.getValue()));
                }
            }
            return result;
        } catch (ConfigurationRepositoryException e) {
            throw new RuntimeException(e);
        }

    }

    @SuppressWarnings("unchecked")
    static class TypeConverter {

        private static Map<String, Converter> converterMap;

        static {
            converterMap = Maps.newHashMap();

            converterMap.put(Boolean.class.getCanonicalName(), new Converter() {

                @Override
                public <T> T convert(String value) {
                    return (T) Boolean.valueOf(value);
                }
            });

            converterMap.put(Double.class.getCanonicalName(), new Converter() {
                @Override
                public <T> T convert(String value) {
                    return (T) Double.valueOf(value);
                }
            });

            converterMap.put(Integer.class.getCanonicalName(), new Converter() {
                @Override
                public <T> T convert(String value) {
                    return (T) Integer.valueOf(value);
                }
            });

            converterMap.put(Long.class.getCanonicalName(), new Converter() {
                @Override
                public <T> T convert(String value) {
                    return (T) Long.valueOf(value);
                }
            });

            converterMap.put(String.class.getCanonicalName(), new Converter() {
                @Override
                public <T> T convert(String value) {
                    return (T) value;
                }
            });
        }

        public static <T> T convert(String type, String value) {
            if (type == null) {
                if (type == null) {
                    throw new RuntimeException(
                            "configuration type is null, please ensure metadata of the configuration item is defined in metadata file.");
                }
            }
            Converter converter = converterMap.get(type);
            if (converter == null) {
                throw new RuntimeException('\'' + type + "' doesn't map to a " + type + " object");
            }

            return (T) converter.convert(value);
        }

        private interface Converter {
            <T> T convert(String value);
        }
    }

    public void clearCache() {
        cache.cleanUp();
        logger.info("Configuration Local Service cache is cleanup.");
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
        logger.info("Cache enabled:" + this.cacheEnabled);
    }

    public void startMonitor() throws ConfigurationRepositoryException {
        startMonitor(true);
    }

    /**
     * @param isStartMonitor if false, just load data once without
     *            starting the monitor
     */
    public void startMonitor(boolean isStartMonitor) throws ConfigurationRepositoryException {
        if (configurationDao instanceof MonitorableReadonlyConfigurationDAO) {
            ((MonitorableReadonlyConfigurationDAO) this.configurationDao).startMonitor(isStartMonitor);
        }
    }

    public void stopMonitor() {
        if (configurationDao instanceof MonitorableReadonlyConfigurationDAO) {
            ((MonitorableReadonlyConfigurationDAO) this.configurationDao).stopMonitor();
        }
    }

    public void registerUpdateCallback(String configurationKey, ConfigurationUpdateCallback callback) {
        if (this.configurationDao instanceof MonitorableReadonlyConfigurationDAO) {
            ((MonitorableReadonlyConfigurationDAO) this.configurationDao).registerUpdateCallback(configurationKey, callback);
        }
    }
}
