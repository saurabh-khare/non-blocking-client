package com.myorg.core.cache;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component(immediate = true, service = CacheRegistry.class)
@Designate(ocd = CacheRegistry.Config.class)
public class CacheRegistryImpl implements CacheRegistry {

    private Config config;
    private List<String> allowedCaches;

    private Map<String, ServiceCache> registry;

    @Activate
    public void activate(final Config config) {
        this.config = config;
        this.allowedCaches = Arrays.asList(Optional.ofNullable(config.allowedCaches()).orElse(new String[0]));
        registry = new ConcurrentHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceCache registerCache(String cacheName) {
        if(!allowedCaches.contains(cacheName.trim())) {
            return null;
        }
        ServiceCache cache = new ServiceCache(cacheName, config.ttl(), config.maxSize());
        cache.setEnabled(true);
        registry.put(cacheName, cache);
        return cache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceCache registerCache(String cacheName, long ttl, long maxSize) {
        if(!allowedCaches.contains(cacheName.trim())) {
            return null;
        }
        ServiceCache cache = new ServiceCache(cacheName,ttl,maxSize);
        cache.setEnabled(true);
        registry.put(cacheName, cache);
        return cache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterCache(String cacheName) {
        registry.remove(cacheName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceCache getCache(String cacheName) {
        return registry.get(cacheName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invalidateAll(String cacheName) {
        ServiceCache cache = registry.get(cacheName);
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invalidate(String cacheName, Object key) {
        ServiceCache cache = registry.get(cacheName);
        if (cache != null) {
            cache.invalidate(key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printCacheSize(String cacheName) {
        ServiceCache cache = registry.get(cacheName);
        if (cache != null) {
           cache.printCacheSize();
        }
    }

}
