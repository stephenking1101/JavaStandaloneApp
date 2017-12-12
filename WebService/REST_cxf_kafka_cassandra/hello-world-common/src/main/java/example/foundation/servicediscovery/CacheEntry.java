package example.foundation.servicediscovery;

/**
 * The entry type in the <code>BasicCache</code>
 * @param <Value>
 */
public class CacheEntry<Value> {
    private long validTo;
    private Value itsObject;

    /**
     * @param validTo
     * @param object
     *
     */
    CacheEntry(long validTo, Value object) {
        this.validTo = validTo;
        itsObject = object;
    }

    /**
     * Check if the entry is expired.
     *
     * @return true if entry is still valid in cache.
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > validTo;
    }

    /**
     * @return cached value
     */
    public Value getValue() {
        return itsObject;
    }
}
