package com.myorg.core.utils;

import com.myorg.core.beans.ServiceWorker;
import com.myorg.core.exception.ConnectionException;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * This utils class is used to make GET and POST call for different REST API end point.
 */
public class HttpClientUtils {

    private HttpClientUtils() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtils.class);

    /**
     * This is the method used to execute HTTP Get method for REST API end point.
     *
     * @param httpClient Instance of @param lang Document Language
     * @param httpGet    Get Request
     * @param apiName    Name of API
     * @return JSON String Response
     */
    public static String executeHTTPGetCall(final CloseableHttpClient httpClient, final HttpGet httpGet,
                                            final String apiName, final int expectedStatusCode, boolean shouldCloseConnection) {

        String responseStr = null;
        try {
            final CloseableHttpResponse response = getResponseFromHTTPGet(httpClient, httpGet, apiName);
            if (null != response && response.getStatusLine() != null
                    && response.getStatusLine().getStatusCode() == expectedStatusCode) {
                responseStr = EntityUtils.toString(response.getEntity());
            } else {
                return responseStr;
            }
        } catch (final IOException ex) {
            LOGGER.error(":::: {} : executeHTTPGetCall IOException occured {} ", apiName, ex);
        } finally {
            if (shouldCloseConnection) {
                try {
                    httpClient.close();
                } catch (final IOException e) {
                    LOGGER.error(":::: {} : executeHTTPGetCall IOException occured while closing httpclient", apiName, e);
                }
            }
        }
        LOGGER.debug(":::: Inside executeHTTPGetCall for {}", apiName);
        return responseStr;
    }
    /**
     * This method is used to execute HTTP Post method for REST API end point.
     *
     * @param httpClient Instance of CloseableHttpClient
     * @param httpPost   Post Request
     * @param apiName    Name of the client
     * @return JSON String Response
     */
    public static String executeHTTPPostCall(final CloseableHttpClient httpClient, final HttpPost httpPost,
                                             final String apiName, final int expectedStatusCode) {

        LOGGER.debug(":::: Inside executeHTTPPostCall for {} start ", apiName);
        String responseStr = null;

        try {
            final CloseableHttpResponse response = httpClient.execute(httpPost);
            if (null != response && response.getStatusLine() != null
                    && response.getStatusLine().getStatusCode() == expectedStatusCode) {
                responseStr = EntityUtils.toString(response.getEntity());
            } else {
                if ((response != null) && (response.getStatusLine() != null)) {
                    LOGGER.error("API {} returned a status other than expected, Status - {}, Response - {}", apiName,
                            response.getStatusLine().getStatusCode(), responseStr);
                } else {
                    LOGGER.error("Response is null for api {}", apiName);
                }
                return responseStr;
            }
        } catch (final ClientProtocolException ex) {
            LOGGER.error(":::: {} : executeHTTPPostCall ClientProtocolException occured {}  ", apiName, ex);
        } catch (final IOException ex) {
            LOGGER.error(":::: {} : executeHTTPPostCall IOException occured {} ", apiName, ex);
        } finally {
            try {
                httpClient.close();
            } catch (final IOException e) {
                LOGGER.error(":::: {} : executeHTTPPostCall IOException occured while closing httpclient {} ", apiName,
                        e);
            }
            httpPost.releaseConnection();
        }
        LOGGER.debug(":::: Inside executeHTTPPostCall for {} reposnseString {} end ", apiName, responseStr);

        return responseStr;
    }

    /**
     * This is the method will execute HTTP Get for REST API end point and will return the Response as it is.
     */
    public static CloseableHttpResponse getResponseFromHTTPGet(final CloseableHttpClient httpClient,
                                                               final HttpGet httpGet, final String apiName) {
        try {
            return httpClient.execute(httpGet);
        } catch (final ClientProtocolException ex) {
            LOGGER.error(":::: {} : getResponseFromHTTPGet ClientProtocolException occured {}  ", apiName, ex);
        } catch (final IOException ex) {
            LOGGER.error(":::: {} : getResponseFromHTTPGet IOException occured {} ", apiName, ex);
        }
        return null;

    }

    /**
     * @param serviceWorker   Service worker contains future task
     * @param timeout         Max time to get service response
     * @return                Service response
     * @throws ConnectionException In case task fails to get data before timeout
     */
    public static String getServiceResponse(final ServiceWorker serviceWorker, long timeout) throws ConnectionException {
        try {
            return serviceWorker.getServiceTask().get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            serviceWorker.getRequestPayload().abort();
            Thread.currentThread().interrupt();//NOSONAR
            throw new ConnectionException(e.getMessage());
        } catch (ExecutionException | IllegalStateException | TimeoutException e) {
            serviceWorker.getRequestPayload().abort();
            throw new ConnectionException(e.getMessage());
        }
    }

    /**
     * Get HTTP payload to be sent to service endpoint
     * @param endpointUrl   Endpoint URL
     * @param parameterMap  Input parameters
     * @param requestType   Method: GET/POST
     * @return              HTTP Payload
     */
    public static HttpUriRequest getPayload(String endpointUrl, Map<String,String> parameterMap, String requestType){
        List<NameValuePair> parameters = parameterMap.entrySet().stream()
                .map(e -> new BasicNameValuePair(e.getKey(), e.getValue())).collect(Collectors.toList());
        try {
            if (HttpPost.METHOD_NAME.equals(requestType)) {
                HttpPost httpPost = new HttpPost(endpointUrl);
                httpPost.setEntity(new UrlEncodedFormEntity(parameters, StandardCharsets.UTF_8.name()));
                return httpPost;
            } else {
                HttpGet httpGet = new HttpGet(endpointUrl);
                URI uri = new URIBuilder(httpGet.getURI()).addParameters(parameters).build();
                httpGet.setURI(uri);
                return httpGet;
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("An error occurred while encoding post parameters", e);
            return null;
        } catch (URISyntaxException e) {
            LOGGER.error("An error occurred while forming url", e);
            return null;
        }
    }
}
