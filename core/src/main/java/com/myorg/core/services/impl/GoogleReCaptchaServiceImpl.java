package com.myorg.core.services.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.myorg.core.beans.ServiceRequestType;
import com.myorg.core.beans.ServiceWorker;
import com.myorg.core.cache.CacheRegistry;
import com.myorg.core.exception.CacheException;
import com.myorg.core.exception.ConnectionException;
import com.myorg.core.services.GoogleReCaptchaService;
import com.myorg.core.services.RestClientService;
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

import static com.myorg.core.constants.GenericConstants.CACHE_RECAPTCHA_RESPONSE;
import static com.myorg.core.constants.GenericConstants.RESPONSE;

/**
*
* This service is used to submit google re-captcha request and contains the configuration for submission endpoint.
*
*/
@Component(service = {RestClientService.class, GoogleReCaptchaService.class}, immediate = true)
@Designate(ocd = GoogleReCaptchaService.Config.class)
public class GoogleReCaptchaServiceImpl implements GoogleReCaptchaService {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleReCaptchaServiceImpl.class);
    
    private static final String SUCCESS = "success";
    
    private Config config;

    private UnaryOperator<Object> loaderFunction;

    @Reference
    private CacheRegistry cacheRegistry;

    @Activate
    protected final void activate(final Config config) {
        this.config = config;
        loaderFunction = key -> getServiceData((ServiceWorker) key);
        CacheUtils.initializeCache(cacheRegistry, CACHE_RECAPTCHA_RESPONSE, loaderFunction, config.cacheTTL(), config.cacheMaxSize());
    }

    @Deactivate
    protected void deactivate() {
        cacheRegistry.invalidateAll(CACHE_RECAPTCHA_RESPONSE);
    }

    /**
     * Attribute for Google Re-Captcha Site Key.
     *
     * @return googleReCaptchaSitetKey.
     */
    public final String getGoogleReCaptchaSiteKey() {
        return config.googleReCaptchaSiteKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean getServiceData(ServiceWorker serviceWorker) {
        try {
            String serviceResponse = HttpClientUtils.getServiceResponse(serviceWorker, config.serviceTimeout());
            if (null != serviceResponse) {
                JsonObject responseJsonObject = new Gson().fromJson(serviceResponse, JsonObject.class);
                if (responseJsonObject.get(SUCCESS) != null) {
                    return responseJsonObject.get(SUCCESS).getAsBoolean();
                } else {
                    LOG.error("No Re-Captcha Status found in the response");
                    return null;
                }
            } else {
                LOG.error("Could not retrieve Re-Captcha Status response from API");
                return null;
            }
        } catch (ConnectionException e) {
            LOG.error("Could not google recaptcha service response");
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
            cacheRegistry.printCacheSize(CACHE_RECAPTCHA_RESPONSE);
        }
        try{
            if (serviceWorker.getId() == null) {
                return true;
            }
            Object cacheData = CacheUtils.getCacheData(CACHE_RECAPTCHA_RESPONSE,
                    serviceWorker, loaderFunction, cacheRegistry);
            if (cacheData instanceof Boolean) {
                return (Boolean) cacheData;
            }
        } catch (CacheException e){
            LOG.debug("unable to get cached data for : {}", serviceWorker, e);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceWorker getServiceWorker(Map<String, String> inputParameters) {
        try {
            ServiceWorker cacheWorker = new ServiceWorker(inputParameters.get(RESPONSE));
            if (cacheWorker.getId() == null || CacheUtils.isCached(CACHE_RECAPTCHA_RESPONSE, cacheWorker, cacheRegistry)) {
                return cacheWorker;
            }
        } catch (CacheException e) {
            LOG.warn("error while retrieving recaptcha response from cache", e);
        }
        Map<String, String> params = new HashMap<>(inputParameters);
        params.put("secret", config.googleReCaptchaSecretKey());
        return new ServiceWorker(inputParameters.get(RESPONSE), ServiceRequestType.RECAPTCHA,
                HttpClientUtils.getPayload(config.googleReCaptchaEndPoint(), params, HttpGet.METHOD_NAME));
    }

}
