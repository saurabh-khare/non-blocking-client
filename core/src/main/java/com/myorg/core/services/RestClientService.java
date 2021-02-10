package com.myorg.core.services;

import com.myorg.core.beans.ServiceWorker;

import java.util.Map;

/**
 * This interface defines the service contract for all third party
 * integrations. All implementing services are responsible to initialize their caches,
 * define connection timeout and getting service response
 */
public interface RestClientService<T> {

    /**
     * Get service data from endpoint. Service implementation is responsible for
     * defining actual return data type
     * Important point to note is this method will return null in case service
     * returns unexpected response since internally guava cache will not save data
     * in case it is null thereby only caching correct data. This will ensure
     * that on next hit for same key its a cache miss and it initiates a service
     * request to get actual data that can be cached.
     * @param serviceWorker     Service Worker
     * @return  Service data
     */
    T getServiceData(ServiceWorker serviceWorker);

    /**
     * Get cached service response data
     * @param serviceWorker Service worker
     * @return  Service data
     */
    T getCacheData(ServiceWorker serviceWorker);

    /**
     * Returns service worker which is responsible for defining request payload
     * @param inputParameters Input Parameters
     * @return  Service Worker
     */
    ServiceWorker getServiceWorker(Map<String,String> inputParameters);
}
