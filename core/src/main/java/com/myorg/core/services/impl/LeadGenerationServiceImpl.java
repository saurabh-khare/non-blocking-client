package com.myorg.core.services.impl;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.myorg.core.beans.ServiceRequestType;
import com.myorg.core.beans.ServiceWorker;
import com.myorg.core.beans.leadgeneration.FormSubmissionResponse;
import com.myorg.core.cache.CacheRegistry;
import com.myorg.core.constants.GenericConstants;
import com.myorg.core.exception.CacheException;
import com.myorg.core.exception.ConnectionException;
import com.myorg.core.services.LeadGenerationService;
import com.myorg.core.utils.CacheUtils;
import com.myorg.core.utils.HttpClientUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.myorg.core.constants.GenericConstants.CACHE_TOKEN_RESPONSE;

/**
 *
 * This service is used to submit lead generation request and contains the configuration for submission endpoint.
 *
 */
@Component(service = LeadGenerationService.class, immediate = true)
@Designate(ocd = LeadGenerationService.Config.class)
public class LeadGenerationServiceImpl implements LeadGenerationService {

    public static final String POST = "POST";

    private static final Logger LOG = LoggerFactory.getLogger(LeadGenerationServiceImpl.class);

    private static final String ACCESS_TOKEN = "access_token";

    private Config config;

    private UnaryOperator<Object> loaderFunction;

    @Reference
    private CacheRegistry cacheRegistry;


    @Activate
    protected final void activate(final Config config) {
        this.config = config;
        loaderFunction = key -> getServiceData((ServiceWorker) key);
        CacheUtils.initializeCache(cacheRegistry, CACHE_TOKEN_RESPONSE, loaderFunction, config.cacheTTL(), config.cacheMaxSize());
    }

    @Deactivate
    protected void deactivate() {
        cacheRegistry.invalidateAll(CACHE_TOKEN_RESPONSE);
    }

    public String getRecordTypeId() {
        return config.recordTypeId();
    }

    public String getLeadSource() {
        return config.leadSource();
    }

    public String getCompany() {
        return config.company();
    }

    /**
     * This method takes the lead generation request json string and submits the same to lead generation API.
     *
     * @param leadGenerationRequest
     * @param acctoken
     * @return
     */
    @Override
    public FormSubmissionResponse submitLeadGeneration(String leadGenerationRequest, String acctoken) {

        if (StringUtils.isEmpty(acctoken)) {
            return new FormSubmissionResponse(false, GenericConstants.ERROR_CODE_INVALID_AUTH_HEADER, "token is empty", Collections.emptyList());
        }
        HttpPost post = new HttpPost(config.formLeadApiEndPoint());
        post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + acctoken);
        post.setHeader(HttpHeaders.CONTENT_TYPE, GenericConstants.APPLICATION_JSON_CONTENT_TYPE);
        post.setEntity(new StringEntity(leadGenerationRequest, ContentType.APPLICATION_JSON));
        try(CloseableHttpClient httpClient = HttpClients.createDefault()){
            HttpResponse response = httpClient.execute(post);
            String responseStr = EntityUtils.toString(response.getEntity());
            Gson gson = new Gson();
            FormSubmissionResponse formSubmissionResponse;
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                formSubmissionResponse = gson.fromJson(responseStr, FormSubmissionResponse.class);
            } else {
                formSubmissionResponse = new FormSubmissionResponse();
                formSubmissionResponse.setSuccess(false);
                formSubmissionResponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                Type listType = new TypeToken<List<FormSubmissionResponse.Error>>() {}.getType();
                List<FormSubmissionResponse.Error> error = gson.fromJson(responseStr, listType);
                formSubmissionResponse.setErrors(error);
                formSubmissionResponse.setStatusCode(response.getStatusLine().getStatusCode());
            }
            return formSubmissionResponse;
        } catch (IOException e){
            LOG.error("error while submitting lead", e);
            FormSubmissionResponse formSubmissionResponse =  new FormSubmissionResponse(false, "SERVER_ERROR", "Internal Server Error", Collections.emptyList());
            formSubmissionResponse.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return formSubmissionResponse;
        }

    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public String getCacheData(ServiceWorker serviceWorker) {
        LOG.debug("Getting cache data for {}", serviceWorker);
        if (config.printCacheSize()) {
            cacheRegistry.printCacheSize(CACHE_TOKEN_RESPONSE);
        }
        try{
            Object cacheData = CacheUtils.getCacheData(CACHE_TOKEN_RESPONSE, serviceWorker, loaderFunction, cacheRegistry);
            if (cacheData instanceof String) {
                return (String) cacheData;
            }
        } catch (CacheException e){
            LOG.debug("unable to find cached data for auth token", e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public String getServiceData(ServiceWorker serviceWorker) {
        try {
            String sessionIdResponse = HttpClientUtils.getServiceResponse(serviceWorker, config.serviceTimeout());
            if (null != sessionIdResponse) {
                JsonObject responseJsonObject = new Gson().fromJson(sessionIdResponse, JsonObject.class);
                if (responseJsonObject.get(ACCESS_TOKEN) != null) {
                    return responseJsonObject.get(ACCESS_TOKEN).getAsString();
                } else {
                    LOG.error("Session ID not found in the response");
                    return null;
                }
            } else {
                LOG.error("Could not retrieve session ID from API");
                return null;
            }
        } catch (ConnectionException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceWorker getServiceWorker(Map<String, String> inputParameters) {
        try {
            ServiceWorker cacheWorker = new ServiceWorker(config.userName());
            if (CacheUtils.isCached(CACHE_TOKEN_RESPONSE, cacheWorker, cacheRegistry)) {
                return cacheWorker;
            }
        } catch (CacheException e) {
            LOG.warn("error while retrieving token from cache", e);
        }
        Map<String, String> params = ImmutableMap.of(
                "username", config.userName(),
                "password", config.password(),
                "grant_type", "password",
                "client_id", config.formLeadClientID(),
                "client_secret", config.formLeadClientSecret()
        );
        return new ServiceWorker(config.userName(), ServiceRequestType.AUTH_TOKEN, HttpClientUtils.getPayload(config.oauthUrl(),
                params, HttpPost.METHOD_NAME));
    }

}
