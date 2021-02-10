package com.myorg.core.cache;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.myorg.core.exception.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * {@code ServiceCache} uses Guava
 * cache framework to store cache data
 */
public class ServiceCache {

    private static final Logger log = LoggerFactory.getLogger(ServiceCache.class);

    private String name;
    private long ttl;
    private long max;
    private boolean enabled;
    private boolean initialized;

    private Function<Object, Object> loader;
    private LoadingCache<Object, Object> loadingCache;
    private final ExecutorService loadingCacheExecutor;

    public ServiceCache(String cacheName, long ttl, long maxSize) {
        this.name = cacheName;
        this.ttl = ttl;
        this.max = maxSize;
        this.loadingCacheExecutor = Executors.newSingleThreadExecutor();
    }

    public void init(Function<Object, Object> loader) {
        try{
            this.loader = loader;
            loadingCache = buildCache(loader, loadingCacheExecutor);
            initialized = true;
            log.info("cache {} initialized", name);
        } catch (Exception e) {
            log.error("Exception occurred in init method", e);
        }
    }


    public boolean isEnabled() {
        return enabled;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    /**
     * Invalidates whole cache
     */
    public void invalidateAll() {
        if (isInitialized() && isEnabled()) {
            loadingCache.invalidateAll();
        }
    }

    /**
     * Invalidates particular cache area
     * identified by the key
     * @param key   Cache key
     */
    public void invalidate(Object key) {
        if (isInitialized() && isEnabled()) {
            loadingCache.invalidate(key);
        }
    }

    /**
     * Gets data from cache for supplied key. If cache is initialized and enabled
     * then get the value from cache or else guava cache internally executes the
     * loader function (API call) to get the data
     * @param key   Cache key
     * @return      Cache data
     * @throws CacheException   Thrown in case of any error while getting data from cache
     */
    public Object getData(Object key) throws CacheException {
        try {
            if (isInitialized()) {
                if (isEnabled()) {
                    log.debug("cache {} initialized & enabled, getting data through cache", name);
                    return loadingCache.get(key);
                } else {
                    log.debug("cache {} initialized but disabled, getting data directly", name);
                    return loader.apply(key);
                }
            }
            log.error("cache {} not initialized", name);
            return null;
        } catch (ExecutionException e) {
            log.error("error occurred while executing cache loader function for key: {}", key, e);
            return null;
        } catch (InvalidCacheLoadException e) {
            log.error("error occurred while fetching cache data for key: {}", key, e);
            throw new CacheException("CacheLoader returned null for " + key);
        }
    }

    /**
     * Logs cache details
     */
    public void printCacheSize() {
        try {
            if (isInitialized()) {
                Iterator<Entry<Object, Object>> it = loadingCache.asMap().entrySet().iterator();
                long cacheSize = 0;
                StringBuilder keys = new StringBuilder();
                while (it.hasNext()) {
                    Entry<Object,Object> entry = it.next();
                    keys.append(entry.getKey()).append(" ");
                    String value = entry.getValue().toString();
                    cacheSize += value.getBytes().length;
                }
                float cacheSizeKB = (float) cacheSize / 1000;
                float cacheSizeMB = (float) cacheSize / 1000000;
                log.info("### The cache {} contains keys {}", name, keys);
                log.info(String.format("### The cache %s has %s entries, size is %s KB or %s MB", name, loadingCache.size(), cacheSizeKB, cacheSizeMB));
            }
        } catch (Exception e) {
            log.error("error occurred while calculation cache size", e);
        }

    }

    /**
     * Builds Guava cache based on supplied parameters
     * @param loader
     * @param executor
     * @return
     */
    private <K, V> LoadingCache<K, V> buildCache(Function<K, V> loader, final ExecutorService executor) {
        return CacheBuilder.newBuilder().expireAfterWrite(ttl, TimeUnit.MINUTES).maximumSize(max)
                .build(new CacheLoader<K, V>() {
                    @Override
                    public V load(K key) {
                        return loader.apply(key);
                    }

                    @Override
                    public ListenableFuture<V> reload(K key, V value) {
                        log.debug("cache {} starting load task...", name);
                        ListenableFutureTask<V> task = ListenableFutureTask.create(() -> loader.apply(key));
                        executor.execute(task);
                        return task;
                    }
                });
    }

    public ConcurrentMap<Object, Object> getCacheMap(){
        if (loadingCache != null) {
            return loadingCache.asMap();
        } else {
            return null;
        }
    }

}
