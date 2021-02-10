package com.myorg.core.services.impl;

import com.google.gson.Gson;
import com.myorg.core.beans.ServiceRequestType;
import com.myorg.core.beans.ServiceWorker;
import com.myorg.core.beans.leadgeneration.ZeroBounceResponse;
import com.myorg.core.beans.leadgeneration.ZerobounceStatus;
import com.myorg.core.cache.CacheRegistry;
import com.myorg.core.constants.GenericConstants;
import com.myorg.core.exception.CacheException;
import com.myorg.core.exception.ConnectionException;
import com.myorg.core.services.ZeroBounceService;
import com.myorg.core.utils.CacheUtils;
import com.myorg.core.utils.HttpClientUtils;
import org.apache.http.client.methods.HttpGet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.myorg.core.constants.GenericConstants.CACHE_ZEROBOUNCE_RESPONSE;

@Component(service = ZeroBounceService.class, immediate = true)
@Designate(ocd = ZeroBounceService.Config.class)
public class ZeroBounceServiceImpl implements ZeroBounceService {


    private static final Logger LOG = LoggerFactory.getLogger(ZeroBounceServiceImpl.class);
    private UnaryOperator<Object> loaderFunction;
    private Config config;

    @Reference
    private CacheRegistry cacheRegistry;


    @Activate
    protected final void activate(final Config config) {
        this.config = config;
        loaderFunction = key -> getServiceData((ServiceWorker) key);
        CacheUtils.initializeCache(cacheRegistry, CACHE_ZEROBOUNCE_RESPONSE, loaderFunction, config.cacheTTL(), config.cacheMaxSize());
    }

    @Deactivate
    protected void deactivate() {
        cacheRegistry.invalidateAll(CACHE_ZEROBOUNCE_RESPONSE);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean getServiceData(ServiceWorker serviceWorker) {
        try{
            Gson gson = new Gson();
            String zeroBounceResponseString = HttpClientUtils.getServiceResponse(serviceWorker, config.serviceTimeout());
            ZeroBounceResponse zeroBounceResponse = gson.fromJson(zeroBounceResponseString, ZeroBounceResponse.class);
            return ZerobounceStatus.of(zeroBounceResponse).isAllowed();
        } catch (ConnectionException e){
            LOG.debug("unable to get response from zerobounce service for : {}", serviceWorker, e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Boolean getCacheData(ServiceWorker serviceWorker) {
        LOG.debug("Getting cache data for {}", serviceWorker);
        if (config.printCacheSize()) {
            cacheRegistry.printCacheSize(CACHE_ZEROBOUNCE_RESPONSE);
        }
        try{
            if (serviceWorker.getId() == null) {
                return true;
            }
            Object cacheData = CacheUtils.getCacheData(CACHE_ZEROBOUNCE_RESPONSE, serviceWorker, loaderFunction, cacheRegistry);
            if (cacheData instanceof Boolean) {
                return (Boolean) cacheData;
            }
        } catch (CacheException e){
            LOG.debug("unable to get cached data for : {}", serviceWorker, e);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceWorker getServiceWorker(Map<String, String> inputParameters) {
        try {
            ServiceWorker cacheWorker = new ServiceWorker(inputParameters.get(GenericConstants.EMAIL));
            if (cacheWorker.getId() == null || CacheUtils.isCached(CACHE_ZEROBOUNCE_RESPONSE, cacheWorker, cacheRegistry)) {
                return cacheWorker;
            }
        } catch (CacheException e) {
            LOG.warn("error while getting zerobounce response from cache", e);
        }
        Map<String, String> params = new HashMap<>(inputParameters);
        params.put("api_key", config.zerobounceAPIKey());
        params.put("ip_address", "");
        return new ServiceWorker(inputParameters.get(GenericConstants.EMAIL), ServiceRequestType.ZEROBOUNCE,
                HttpClientUtils.getPayload(config.zerobounceEndpoint(), params, HttpGet.METHOD_NAME));

    }
}
