package com.myorg.core.services.impl;

import com.google.common.collect.ImmutableMap;
import com.myorg.core.beans.ServiceRequestType;
import com.myorg.core.beans.ServiceWorker;
import com.myorg.core.beans.leadgeneration.FormSubmissionResponse;
import com.myorg.core.cache.CacheRegistry;
import com.myorg.core.cache.ServiceCache;
import com.myorg.core.exception.CacheException;
import com.myorg.core.services.LeadGenerationService.Config;
import com.myorg.core.utils.HttpClientUtils;
import mockit.MockUp;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpRequestFutureTask;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.myorg.core.constants.GenericConstants.CACHE_TOKEN_RESPONSE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadGenerationServiceImplTest {
    private static final String API_END_POINT = "some-endpoint";

    private static final String CLIENT_ID = "client-id";

    private static final String CLIENT_SECRET = "client-secret";

    private static final String OAUTH_URL = "oauth-url";

    private static final String USER_NAME = "saurabh-khare";

    private static final String PWD = "password";

    private static final String RECORD_TYPE_ID = "some-record-id";

    private static final String LEAD_SOURCE = "Facebook";

    private static final String COMPANY = "Saurabh Khare";

    private static final String JSON_PATH = "/services/LeadGenrationRequest.json";

    private static final String TOKEN_RESPONSE = "{\n" +
            "    \"access_token\": \"some-token\",\n" +
            "    \"instance_url\": \"some-url\",\n" +
            "    \"id\": \"some-id\",\n" +
            "    \"token_type\": \"Bearer\",\n" +
            "    \"issued_at\": \"1597476589891\",\n" +
            "    \"signature\": \"valid-signature\"\n" +
            "}";

    @InjectMocks
    LeadGenerationServiceImpl leadGenerationServiceImpl;

    @Mock
    Config config;

    @Mock
    CloseableHttpResponse response;

    @Mock
    StatusLine statusLine;

    @Mock
    CacheRegistry cacheRegistry;

    String res = "";

    String mockedAccessToken = "mock-token";

    private MockUp<CloseableHttpClient> closeableHttpClientMockup;

    private MockUp<EntityUtils> entityUtilsMockup;

    @BeforeEach
    void setupMock() {
        MockitoAnnotations.initMocks(this);
        lenient().when(config.formLeadApiEndPoint()).thenReturn(API_END_POINT);
        lenient().when(config.formLeadClientID()).thenReturn(CLIENT_ID);
        lenient().when(config.formLeadClientSecret()).thenReturn(CLIENT_SECRET);
        lenient().when(config.oauthUrl()).thenReturn(OAUTH_URL);
        lenient().when(config.userName()).thenReturn(USER_NAME);
        lenient().when(config.password()).thenReturn(PWD);
        lenient().when(config.recordTypeId()).thenReturn(RECORD_TYPE_ID);
        lenient().when(config.leadSource()).thenReturn(LEAD_SOURCE);
        lenient().when(config.company()).thenReturn(COMPANY);

    }

    private void setUpHttpResponse(String json, int statusCode) {

        closeableHttpClientMockup = new MockUp<CloseableHttpClient>() {
            @mockit.Mock
            CloseableHttpResponse execute(HttpUriRequest request) {
                return response;
            }
        };

        lenient().when(response.getStatusLine()).thenReturn(statusLine);
        lenient().when(response.getStatusLine().getStatusCode()).thenReturn(statusCode);

        entityUtilsMockup = new MockUp<EntityUtils>() {
            @mockit.Mock
            String toString(HttpEntity entity) {
                return json;
            }
        };
    }

    private ServiceWorker getServiceWorker() throws InterruptedException, ExecutionException, TimeoutException {
        Map<String, String> params = ImmutableMap.of(
                "username", config.userName(),
                "password", config.password(),
                "grant_type", "password",
                "client_id", config.formLeadClientID(),
                "client_secret", config.formLeadClientSecret()
        );
        ServiceWorker serviceWorker = new ServiceWorker(config.userName(), ServiceRequestType.AUTH_TOKEN, HttpClientUtils.getPayload(config.oauthUrl(),
                params, HttpPost.METHOD_NAME));
        HttpRequestFutureTask<String> mockTask = Mockito.mock(HttpRequestFutureTask.class);
        lenient().when(mockTask.get(anyLong(), same(TimeUnit.SECONDS))).thenReturn(TOKEN_RESPONSE);
        serviceWorker.setServiceTask(mockTask);
        return serviceWorker;
    }

    private void initializeCache() throws CacheException {
        ServiceCache serviceCache = mock(ServiceCache.class);
        when(serviceCache.isEnabled()).thenReturn(true);
        when(serviceCache.isInitialized()).thenReturn(true);
        when(serviceCache.getData(new ServiceWorker(config.userName()))).thenReturn(mockedAccessToken);
        when(cacheRegistry.getCache(same(CACHE_TOKEN_RESPONSE))).thenReturn(serviceCache);
    }

    @Test
    void testSubmitLeadGeneration() throws Exception {

        URL resourceURL = getClass().getResource(JSON_PATH);
        String json = resourceURL.toString();

        setUpHttpResponse(json, 201);
        FormSubmissionResponse formSubmissionResponse = leadGenerationServiceImpl.submitLeadGeneration(json, mockedAccessToken);
        assertEquals(HttpStatus.SC_OK , formSubmissionResponse.getStatusCode());
    }

    @Test
    void testSubmitLeadGenerationNull() throws Exception {
        URL resourceURL = getClass().getResource(JSON_PATH);
        String json = resourceURL.toString();
        setUpHttpResponse(null, 500);
        FormSubmissionResponse actualResponse = leadGenerationServiceImpl.submitLeadGeneration(json, mockedAccessToken);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
    }

    @Test
    void testGetServiceData() throws Exception {
        ServiceWorker serviceWorker = getServiceWorker();
        Object responsestring = leadGenerationServiceImpl.getServiceData(serviceWorker);
        assertEquals(mockedAccessToken, responsestring);
    }

    @Test
    void testGetServiceResponse() throws Exception {
        initializeCache();
        ServiceWorker serviceWorker = getServiceWorker();
        Object responsestring = leadGenerationServiceImpl.getCacheData(serviceWorker);
        assertEquals(mockedAccessToken, responsestring);
    }

    @Test
    void testServiceWorker() {
        ServiceWorker serviceWorker = leadGenerationServiceImpl.getServiceWorker(Collections.emptyMap());
        assertAll("Validate Lead generation service worker",
                () -> assertEquals(ServiceRequestType.AUTH_TOKEN, serviceWorker.getServiceRequest()),
                () -> assertEquals(config.userName(), serviceWorker.getId())
        );
    }

    @Test
    void testGetRecordTypeId() throws Exception {
        assertEquals(RECORD_TYPE_ID, leadGenerationServiceImpl.getRecordTypeId());
    }

    @Test
    void testGetLeadSource() throws Exception {
        assertEquals(LEAD_SOURCE, leadGenerationServiceImpl.getLeadSource());
    }

    @Test
    void testGetCompany() throws Exception {
        assertEquals(COMPANY, leadGenerationServiceImpl.getCompany());
    }

    @AfterEach
    void shouldTearDown() {
        if (closeableHttpClientMockup != null) {
            closeableHttpClientMockup.tearDown();
        }

        if (entityUtilsMockup != null) {
            entityUtilsMockup.tearDown();
        }
    }

}
