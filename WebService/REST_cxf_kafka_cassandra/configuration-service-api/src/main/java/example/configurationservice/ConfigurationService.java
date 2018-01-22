package example.configurationservice;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.core.convert.ConversionException;

/**
 * Defines the WSFConfiguration lookup interface.
 */
public interface ConfigurationService {
    /**
     * Get a boolean associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated boolean.
     *
     * @throws ConversionException is thrown if the key maps to an
     *             object that is not a Boolean.
     *
     * @throws NoSuchElementException is thrown if the key doesn't
     *             maps to any object in the database and the cache
     */
    boolean getBoolean(String key);

    /**
     * Get a boolean associated with the given configuration key. If
     * the key doesn't map to an existing object, the default value is
     * returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated boolean.
     *
     * @throws ConversionException is thrown if the key maps to an
     *             object that is not a Boolean.
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Get a double associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated double.
     *
     * @throws ConversionException is thrown if the key maps to an
     *             object that is not a Double.
     * @throws NoSuchElementException is thrown if the key doesn't
     *             maps to any object in the database and the cache
     */
    double getDouble(String key);

    /**
     * Get a double associated with the given configuration key. If
     * the key doesn't map to an existing object, the default value is
     * returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated double.
     *
     * @throws ConversionException is thrown if the key maps to an
     *             object that is not a Double.
     */
    double getDouble(String key, double defaultValue);

    /**
     * Get a int associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated int.
     *
     * @throws ConversionException is thrown if the key maps to an
     *             object that is not a Integer.
     * @throws NoSuchElementException is thrown if the key doesn't
     *             maps to any object in the database and the cache
     */
    int getInt(String key);

    /**
     * Get a int associated with the given configuration key. If the
     * key doesn't map to an existing object, the default value is
     * returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated int.
     *
     * @throws ConversionException is thrown if the key maps to an
     *             object that is not a Integer.
     */
    int getInt(String key, int defaultValue);

    /**
     * Get a string associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated string.
     *
     * @throws ConversionException is thrown if the key maps to an
     *             object that is not a String.
     * @throws NoSuchElementException is thrown if the key doesn't
     *             maps to any object in the database and the cache
     */
    String getString(String key);

    /**
     * Get a string associated with the given configuration key. If
     * the key doesn't map to an existing object, the default value is
     * returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated string if key is found and has valid
     *         format, default value otherwise.
     *
     * @throws ConversionException is thrown if the key maps to an
     *             object that is not a String.
     */
    String getString(String key, String defaultValue);

    /**
     * Get a value Object associated with the given configuration
     * key(don't care return value's type).
     *
     * @param key
     * @return Object
     *
     * @throws NoSuchElementException is thrown if the key doesn't
     *             maps to any object in the database and the cache
     */
    Object getObject(String key);

    /**
     * fuzzy search the given configuration key and return the key
     * value map
     *
     * @param key
     * @return a map with the concrete key and it's value
     */
    Map<String, Object> searchObject(String key);
}
