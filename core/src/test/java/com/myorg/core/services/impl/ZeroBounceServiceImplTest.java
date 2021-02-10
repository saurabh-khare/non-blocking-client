package com.myorg.core.services.impl;

import com.google.common.collect.ImmutableMap;
import com.myorg.core.beans.ServiceRequestType;
import com.myorg.core.beans.ServiceWorker;
import com.myorg.core.cache.CacheRegistry;
import com.myorg.core.cache.ServiceCache;
import com.myorg.core.exception.CacheException;
import com.myorg.core.services.ZeroBounceService;
import com.myorg.core.utils.HttpClientUtils;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpRequestFutureTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.myorg.core.constants.GenericConstants.CACHE_ZEROBOUNCE_RESPONSE;
import static com.myorg.core.constants.GenericConstants.EMAIL;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class ZeroBounceServiceImplTest {

    private static String ZB_ENPOINT = "https://api.zerobounce.net/v2/validate";
    private static String ZB_API_KEY = "random_key";
    private static long ZB_TIMEOUT = 3;
    private static final boolean ZB_RESPONSE_STATUS = true;

    @InjectMocks
    ZeroBounceServiceImpl zerobounceServiceImpl;

    @Mock
    ZeroBounceService.Config config;

    @Mock
    CacheRegistry cacheRegistry;

    private static final String VALID_EMAIL = "valid@example.com";

    private static final String ZEROBOUNCE_RESPONSE = "{\"address\":\"greylisted@example.com\",\"status\":\"unknown\",\"sub_status\":\"greylisted\",\"free_email\":false,\"did_you_mean\":null,\"account\":null," +
            "\"domain\":null,\"domain_age_days\":\"9692\",\"smtp_provider\":\"example\",\"mx_found\":\"true\",\"mx_record\":\"mx.example.com\",\"firstname\":\"zero\",\"lastname\":\"bounce\",\"gender\":\"male\"," +
            "\"country\":null,\"region\":null,\"city\":null,\"zipcode\":null,\"processed_at\":\"2020-08-16 07:20:10.382\"}";

    @BeforeEach
    public void setupMock() throws IOException {
        MockitoAnnotations.initMocks(this);
        lenient().when(config.zerobounceEndpoint()).thenReturn(ZB_ENPOINT);
        lenient().when(config.zerobounceAPIKey()).thenReturn(ZB_API_KEY);
        lenient().when(config.serviceTimeout()).thenReturn(ZB_TIMEOUT);
    }

    private ServiceWorker getServiceWorker() throws InterruptedException, ExecutionException, TimeoutException {
        Map<String, String> params = ImmutableMap.of(
                 EMAIL, VALID_EMAIL,
                "api_key", config.zerobounceAPIKey(),
                "ip_address", ""
        );
        ServiceWorker serviceWorker = new ServiceWorker(VALID_EMAIL, ServiceRequestType.ZEROBOUNCE, HttpClientUtils.getPayload(config.zerobounceEndpoint(),
                params, HttpGet.METHOD_NAME));
        HttpRequestFutureTask<String> mockTask = Mockito.mock(HttpRequestFutureTask.class);
        lenient().when(mockTask.get(anyLong(), same(TimeUnit.SECONDS))).thenReturn(ZEROBOUNCE_RESPONSE);
        serviceWorker.setServiceTask(mockTask);
        return serviceWorker;
    }

    private void initializeCache() throws CacheException {
        ServiceCache serviceCache = mock(ServiceCache.class);
        when(serviceCache.isEnabled()).thenReturn(true);
        when(serviceCache.isInitialized()).thenReturn(true);
        when(serviceCache.getData(new ServiceWorker(VALID_EMAIL))).thenReturn(true);
        when(cacheRegistry.getCache(same(CACHE_ZEROBOUNCE_RESPONSE))).thenReturn(serviceCache);
    }


    @Test
    void testGetServiceData() throws Exception {
        ServiceWorker serviceWorker = getServiceWorker();
        Object responsestring = zerobounceServiceImpl.getServiceData(serviceWorker);
        assertEquals(ZB_RESPONSE_STATUS, responsestring);
    }

    @Test
    void testGetServiceResponse() throws Exception {
        initializeCache();
        ServiceWorker serviceWorker = getServiceWorker();
        Object responsestring = zerobounceServiceImpl.getCacheData(serviceWorker);
        assertEquals(ZB_RESPONSE_STATUS, responsestring);
    }

    @Test
    void testServiceWorker() {
        ServiceWorker serviceWorker = zerobounceServiceImpl.getServiceWorker(Collections.singletonMap(EMAIL, VALID_EMAIL));
        assertAll("Validate zerobounce service worker",
                () -> assertEquals(ServiceRequestType.ZEROBOUNCE, serviceWorker.getServiceRequest()),
                () -> assertEquals(VALID_EMAIL, serviceWorker.getId())
        );
    }
}