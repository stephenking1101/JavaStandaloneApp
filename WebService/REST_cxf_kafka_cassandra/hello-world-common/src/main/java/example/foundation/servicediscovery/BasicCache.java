package example.foundation.servicediscovery;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * All purpose primitive cache facility. <br>
 * Entries will get a timestamp when <code>put()</code> in the cache.
 * For every successful <code>get()</code> the timestamp will be
 * evaluated, and if timestamp is older then the cache's timeout value
 * then the entry will be omitted. <br>
 * The cache has a maximum size limit. The oldest entry will be
 * flushed out when <code>put()</code> to the full cache.
 * 
 * NOTE: The cache may be disabled if SIG is running on Testing Mode (add
 * system parameter -DSIG_Running_Mode=Testing).
 * @param <Key> 
 * @param <Value> 
 */
public class BasicCache<Key, Value> {

    private final static int DEFAULT_SIZE = 1000;

    private Map<Key, CacheEntry<Value>> cache;
    private long timeToLive;
    private int size;
    
    //Change this parameter to static
    private static boolean isCacheEnabled = true;//by default, caching is enabled
    
    static {
        SystemInfo.SystemMode mode = SystemInfo.getSystemMode();
        if(SystemInfo.SystemMode.Testing.equals(mode)) {
            isCacheEnabled = false;//disable cache
        }
    }
    
    /**
     * Creates a new BasicCache object with default size (1000).
     * 
     * @param timeToLive Time in milliseconds how long each CacheEntry
     *            is valid in cache.
     */
    public BasicCache(long timeToLive) {
        this(timeToLive, DEFAULT_SIZE);
    }

    /**
     * Creates a new BasicCache object.
     * 
     * @param timeToLive Time in milliseconds how long each CacheEntry
     *            is valid in cache.
     * @param cacheSize The maximum number of the cached entries.
     */
    public BasicCache(long timeToLive, int cacheSize) {
        
        this.timeToLive = timeToLive;
        size = cacheSize;

        cache = new LinkedHashMap<Key, CacheEntry<Value>>() {
            private static final long serialVersionUID = 8475466421916292999L;

            @Override
            protected boolean removeEldestEntry(@SuppressWarnings({ "unused", "rawtypes" }) Map.Entry eldest) {
                return size() > size;
            }
        };
        
    }
    
    /**
    /**
     * Put an object in the cache.
     * 
     * @param key Key to object
     * @param value Object to cache
     */
    public synchronized void put(Key key, Value value) {
        if(isCacheEnabled) {
            CacheEntry<Value> object = new CacheEntry<Value>(System.currentTimeMillis() + timeToLive, value);
            cache.put(key, object);            
        }
    }

    /**
     * Remove an object from the cache.
     * 
     * @param key Key to object
     */
    public synchronized void remove(Key key) {
        if(isCacheEnabled) {
            cache.remove(key);
        }        
    }

    /**
     * Removes all objects from the cache.
     */
    public synchronized void clear() {
        if(isCacheEnabled) {
            cache.clear();            
        }        
    }

    /**
     * Get the <code>CacheEntry</code> with the key from cache.
     * 
     * @param key Key of the CacheEntry
     * @return the CacheEntry
     */
    public synchronized CacheEntry<Value> getCacheEntry(Key key) {
        if(isCacheEnabled) {
            return cache.get(key);
        }
        return null;
    }

    /**
     * Get an object from cache.
     * 
     * @param key Key to cached object.
     * 
     * @return Cached object, null if not found or entry is expired.
     */
    public Value get(Key key) {
        if(!isCacheEnabled) {
            return null;
        }
        
        CacheEntry<Value> entry = getCacheEntry(key);

        if (entry == null) {
            return null;
        }

        if (entry.isExpired()) {
            return null;
        }

        return entry.getValue();
    }

    /**
     * Returns the size of the cache.
     * 
     * @return int how many entries are cached.
     */
    public int size() {
        if(isCacheEnabled) {
            return cache.size();            
        }
        return 0;
    }

}