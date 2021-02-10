package com.myorg.core.services.impl;

import com.google.common.collect.ImmutableMap;
import com.myorg.core.beans.ServiceRequestType;
import com.myorg.core.beans.ServiceWorker;
import com.myorg.core.cache.CacheRegistry;
import com.myorg.core.cache.ServiceCache;
import com.myorg.core.constants.GenericConstants;
import com.myorg.core.exception.CacheException;
import com.myorg.core.services.GoogleReCaptchaService;
import com.myorg.core.utils.HttpClientUtils;
import org.apache.commons.lang3.StringUtils;
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

import static com.myorg.core.constants.GenericConstants.CACHE_RECAPTCHA_RESPONSE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleReCaptchaServiceImplTest {
    private static final String RECAPTCHA_TOKEN = "03AGdBq24GeYV0FCGe_Ar0nb7D8FDg_b-8JBniditPAwVzGXAA4NxWR8TcWEuo01iSHCjLZw1-Jm41gvSQAYZ7GlTQ4Jsw2rJuoIvu0DYuMdM7AlzdSoe1rJPYWOOUKUv5WwcZmLvT2m-Hv_RcgrK_Ol8FInWw_lU-kZzz15_YGC0fM8gLx4ihQ33dArG8eNx8YzG6aUTEJOykwrjjbWYM6Knf8gavNoMXE8wBVFqAUBriI2S_llDBhStC8IPTtHqWgD0ru0AnG0QelENMTQX_Hi-BQOLy863J4LCrOdV08rrGvFuRU00eakGXlpM2FjdPAgCxNQSGu50BD7BuXmkzqPvs6CvwBUdne6YTctw0l3KVwk0tpYrs11EELjhEceP9FoBbUJGeWbxO";

    private static final String RECAPTCHA_END_POINT = "https://www.google.com/recaptcha/api/siteverify";

    private static final String RECAPTCHA_SECRET_KEY = "6LebDvABCD178787213j3PMg5ABCD1253793-KcQ";

    private static final String RECAPTCHA_RESPONSE = "{\n" +
            "  \"success\": true,\n" +
            "  \"challenge_ts\": \"2020-08-16T06:29:02Z\",\n" +
            "  \"hostname\": \"localhost\"\n" +
            "}";

    private static final String RECAPTCHA_SITE_KEY = "6LebDvABCD123213j3PMg5ABCD1253793-XcQ";

    private static final boolean RECAPTCHA_RESPONSE_STATUS = true;

    @InjectMocks
    GoogleReCaptchaServiceImpl googleReCaptchaServiceImpl;

    @Mock
    GoogleReCaptchaService.Config config;

    @Mock
    CacheRegistry cacheRegistry;

    @BeforeEach
    public void setupMock() throws IOException {
        MockitoAnnotations.initMocks(this);
        lenient().when(config.googleReCaptchaEndPoint()).thenReturn(RECAPTCHA_END_POINT);
        lenient().when(config.googleReCaptchaSecretKey()).thenReturn(RECAPTCHA_SECRET_KEY);
        lenient().when(config.googleReCaptchaSiteKey()).thenReturn(RECAPTCHA_SITE_KEY);
        googleReCaptchaServiceImpl.activate(config);
    }

    private ServiceWorker getServiceWorker() throws InterruptedException, ExecutionException, TimeoutException {
        Map<String, String> params = ImmutableMap.of(
                "secret", config.googleReCaptchaSecretKey(),
                "response", RECAPTCHA_TOKEN
        );
        ServiceWorker serviceWorker = new ServiceWorker(RECAPTCHA_TOKEN, ServiceRequestType.AUTH_TOKEN, HttpClientUtils.getPayload(config.googleReCaptchaEndPoint(),
                params, HttpGet.METHOD_NAME));
        HttpRequestFutureTask<String> mockTask = Mockito.mock(HttpRequestFutureTask.class);
        lenient().when(mockTask.get(anyLong(), same(TimeUnit.SECONDS))).thenReturn(RECAPTCHA_RESPONSE);
        serviceWorker.setServiceTask(mockTask);
        return serviceWorker;
    }

    private void initializeCache() throws CacheException {
        ServiceCache serviceCache = mock(ServiceCache.class);
        when(serviceCache.isEnabled()).thenReturn(true);
        when(serviceCache.isInitialized()).thenReturn(true);
        when(serviceCache.getData(new ServiceWorker(RECAPTCHA_TOKEN))).thenReturn(true);
        when(cacheRegistry.getCache(same(CACHE_RECAPTCHA_RESPONSE))).thenReturn(serviceCache);
    }

    @Test
    void testGetServiceData() throws Exception {
        ServiceWorker serviceWorker = getServiceWorker();
        Object response = googleReCaptchaServiceImpl.getServiceData(serviceWorker);
        assertEquals(RECAPTCHA_RESPONSE_STATUS, response);
    }

    @Test
    void testGetServiceResponse() throws Exception {
        initializeCache();
        ServiceWorker serviceWorker = getServiceWorker();
        Object response = googleReCaptchaServiceImpl.getCacheData(serviceWorker);
        assertEquals(RECAPTCHA_RESPONSE_STATUS, response);
    }

    @Test
    void testServiceWorker() {
        ServiceWorker serviceWorker = googleReCaptchaServiceImpl.getServiceWorker(Collections.singletonMap(GenericConstants.RESPONSE, RECAPTCHA_TOKEN));
        assertAll("Validate Recaptcha service worker",
                () -> assertEquals(ServiceRequestType.RECAPTCHA, serviceWorker.getServiceRequest()),
                () -> assertEquals(StringUtils.abbreviate(RECAPTCHA_TOKEN, 60), serviceWorker.getId())
        );
    }

}
