package com.myorg.core.utils;

import com.myorg.core.cache.CacheRegistry;
import com.myorg.core.cache.ServiceCache;
import com.myorg.core.exception.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * This is utility class to provide various accessors, validators and initializers to cache
 */
public class CacheUtils {

    private CacheUtils(){}

    private static final Logger LOG = LoggerFactory.getLogger(CacheUtils.class);

    /**
     * Initialize cache of certain category and name with default cache configuration
     * @param cacheRegistry Cache Registry
     * @param category      Cache category
     * @param cacheName     Cache name
     * @param loader        Loader function to use for getting data
     */
    public static void initializeCache(CacheRegistry cacheRegistry, String cacheName, Function<Object, Object> loader) {
        try {
            LOG.info("initialize cache {} cache..", cacheName);
            ServiceCache serviceCache = cacheRegistry.registerCache(cacheName);
            if (serviceCache != null) {
                LOG.info("{} cache exists..", cacheName);
                serviceCache.init(loader);
                LOG.info("{} cache initialized..", cacheName);
            } else {
                LOG.error("Unable to initializeCache: {}", cacheName);
            }
        } catch (Exception e) {
            LOG.error("Exception occurred while initializing cache {}", e);
        }

    }

    /**
     * Initialize cache of certain category and name with supplied cache configuration
     * @param cacheRegistry Cache Registry
     * @param category      Cache category
     * @param cacheName     Cache name
     * @param loader        Loader function to use for getting data
     */
    public static void initializeCache(CacheRegistry cacheRegistry, String cacheName, Function<Object, Object> loader, long ttl, long maxSize) {
        try {
            LOG.info("initialize cache {} cache..", cacheName);
            ServiceCache serviceCache = cacheRegistry.registerCache(cacheName, ttl, maxSize);
            if (serviceCache != null) {
                LOG.info("{} cache exists..", cacheName);
                serviceCache.init(loader);
                LOG.info("{} cache initialized..", cacheName);
            } else {
                LOG.error("Unable to initializeCache: {}", cacheName);
            }
        } catch (Exception e) {
            LOG.error("Exception occurred while initializing cache {}", e);
        }

    }

    /**
     * Get data from cache if present for supplied category and name
     * @param category          Cache category
     * @param cacheName         Cache name
     * @param key               Cache key
     * @param loader            Loader function to initialize cache if not already initialized
     * @param cacheRegistry     Cache registry
     * @return                  Cache data
     * @throws CacheException   Exception while getting cache data
     */
    public static Object getCacheData(String cacheName, Object key,
                                      Function<Object, Object> loader, CacheRegistry cacheRegistry) throws CacheException {
        ServiceCache serviceCache = cacheRegistry.getCache(cacheName);
        if (serviceCache == null || !serviceCache.isEnabled()) {
            return null;
        }
        if (!serviceCache.isInitialized()) {
            LOG.debug("Cache {} not initialized. Initializing now.", cacheName);
            serviceCache.init(loader);
            LOG.debug("Cache {} initialized", cacheName);
        }
        LOG.debug("get from cache");
        return serviceCache.getData(key);
    }

    /**
     * Check if data for given key is present in cache or not
     * @param category          Cache category
     * @param cacheName         Cache name
     * @param key               Cache key
     * @param cacheRegistry     Cache registry
     * @return                  flag to indicate if key exists or not
     * @throws CacheException   Exception while accessing cache data
     */
    public static boolean isCached(String cacheName, Object key, CacheRegistry cacheRegistry) throws CacheException {
        ServiceCache serviceCache = cacheRegistry.getCache(cacheName);
        if (serviceCache == null || !serviceCache.isEnabled()) {
            return false;
        }
        return serviceCache.getCacheMap() != null && serviceCache.getCacheMap().containsKey(key);
    }


}
