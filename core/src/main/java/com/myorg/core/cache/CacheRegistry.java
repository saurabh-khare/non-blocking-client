package com.myorg.core.cache;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import static com.myorg.core.constants.GenericConstants.CACHE_RECAPTCHA_RESPONSE;
import static com.myorg.core.constants.GenericConstants.CACHE_TOKEN_RESPONSE;
import static com.myorg.core.constants.GenericConstants.CACHE_ZEROBOUNCE_RESPONSE;

/**
 * {@code CacheRegistry} dynamically initializes {@code ServiceCache} object
 * instances based on allowed caches configuration.</br> It uses Guava Table data
 * structure to main cache instances as a cache is uniquely identified by category
 * and cache name
 */
public interface CacheRegistry {

    @ObjectClassDefinition(name = "Cache Registry", description = "Service cache registry")
    @interface Config {

        @AttributeDefinition(name = "Reload time", description = "Cache will be reloaded in the background after defined time in minutes")
        long ttl() default 60;

        @AttributeDefinition(name = "Maximum size", description = "Maximum number of objects that can be cached")
        long maxSize() default 10000;

        @AttributeDefinition(name = "Allowed Caches", description = "Only caches listed here will be initialized")
        String [] allowedCaches() default {CACHE_ZEROBOUNCE_RESPONSE,CACHE_RECAPTCHA_RESPONSE, CACHE_TOKEN_RESPONSE};

    }

    /**
     * Initializes a {@code ServiceCache} with default ttl and max size
     * and adds it to registry table
     */
    ServiceCache registerCache(final String cacheName);

    /**
     * Initializes a {@code ServiceCache} with supplied values for ttl and max size
     * and adds it to registry table
     */
    ServiceCache registerCache(String cacheName, long ttl, long maxSize);

    /**
     * Remove {@code ServiceCache} from cache registry
     */
    void unregisterCache(final String cacheName);

    /**
     * Get {@code ServiceCache} from registry if present,
     * else null
     */
    ServiceCache getCache(final String cacheName);

    /**
     * Flush whole {@code ServiceCache}
     */
    void invalidateAll(String cacheName);

    /**
     * Flush particular cache area identified with supplied key
     */
    void invalidate(String cacheName, Object key);

    /**
     * Print cache details for given cache
     */
    void printCacheSize(String cacheName);

}
