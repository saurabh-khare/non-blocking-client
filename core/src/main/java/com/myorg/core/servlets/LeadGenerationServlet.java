package com.myorg.core.servlets;

import com.myorg.core.beans.leadgeneration.FormSubmissionRequest;
import com.myorg.core.beans.leadgeneration.FormSubmissionResponse;
import com.myorg.core.beans.ServiceWorker;
import com.myorg.core.constants.FormConstants;
import com.myorg.core.constants.GenericConstants;
import com.myorg.core.services.GoogleReCaptchaService;
import com.myorg.core.services.LeadGenerationService;
import com.myorg.core.services.ZeroBounceService;
import org.apache.http.HttpStatus;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.FutureRequestExecutionService;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.HttpRequestFutureTask;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This endpoint is used to consume the end user form data and submit the details to Lead Generation API.
 */
@Component(
    immediate = true,
    service = Servlet.class,
    property = { "sling.servlet.methods=POST", "sling.servlet.paths=/services/leadgeneration" })
public class LeadGenerationServlet extends SlingAllMethodsServlet {

    @Reference
    private transient LeadGenerationService leadGenerationService;

    @Reference
    private transient GoogleReCaptchaService googleReCaptchaService;

    @Reference
    private transient ZeroBounceService zeroBounceService;

    private static final long serialVersionUID = 8498212471440488124L;

    private static final Logger LOGGER = LoggerFactory.getLogger(LeadGenerationServlet.class);

    /**
     * This method creates the request from data and additional parameters, calls the lead generation service to submit
     * lead generation request.
     * 
     * @param request
     * @param resp
     */
    @Override
    protected final void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse resp) throws IOException {

        List<ServiceWorker> serviceWorkers = new ArrayList<>();

        /*Initialize service worker for Google recaptcha service*/
        ServiceWorker recaptchaServiceWorker =  googleReCaptchaService.getServiceWorker(Collections.singletonMap(GenericConstants.RESPONSE, request.getParameter(FormConstants.CAPTCHA_TOKEN)));
        serviceWorkers.add(recaptchaServiceWorker);

        /*Initialize service worker for Zerobounce email validation service*/
        ServiceWorker zeroBounceServiceWorker = zeroBounceService.getServiceWorker(Collections.singletonMap(GenericConstants.EMAIL, request.getParameter(FormConstants.EMAIL)));
        serviceWorkers.add(zeroBounceServiceWorker);

        /*Initialize service worker to get auth token. Since there is no user input required to for generating token, we send empty parameter map*/
        ServiceWorker sfServiceWorker = leadGenerationService.getServiceWorker(Collections.emptyMap());
        serviceWorkers.add(sfServiceWorker);

        long workerCount = serviceWorkers.stream().filter(serviceWorker -> serviceWorker.getServiceRequest()!=null).count();
        /*Worker pool must be initialized with spare thread to cover disruptions*/
        ExecutorService execService = Executors.newFixedThreadPool(Math.toIntExact(workerCount)+1);
        try (
                CloseableHttpClient httpclient = HttpClients.createDefault();
                FutureRequestExecutionService requestExecService = new FutureRequestExecutionService(httpclient, execService);
        ) {
            /*Eliminate cached service workers to avoid generating HTTP request. Filter:serviceworker.getServiceRequest()*/
            serviceWorkers.stream().filter(serviceWorker -> serviceWorker.getServiceRequest()!=null).forEach(serviceWorker -> {
                HttpRequestFutureTask<String> futureTask = requestExecService.execute(serviceWorker.getRequestPayload(), HttpClientContext.create(),
                        new BasicResponseHandler());
                serviceWorker.setServiceTask(futureTask);
            });
            /*If recaptcha response is invalid, then set error response and return*/
            if (Boolean.FALSE.equals(googleReCaptchaService.getCacheData(recaptchaServiceWorker))) {
                setResponse(resp, new FormSubmissionResponse(false, GenericConstants.ERROR_CODE_INVALID_RECAPTCHA, "Recaptcha is invalid", Collections.emptyList()));
                return;
            }
            /*If zerobounce response is invalid for given email, then set error response and return*/
            if (Boolean.FALSE.equals(zeroBounceService.getCacheData(zeroBounceServiceWorker))) {
                setResponse(resp, new FormSubmissionResponse(false, GenericConstants.ERROR_CODE_INVALID_EMAIL,"Email is invalid", Collections.singletonList(FormConstants.EMAIL)));
                return;
            }
            String sessionId = leadGenerationService.getCacheData(sfServiceWorker);
            /*All validations successful, proceed with lead generation*/
            FormSubmissionRequest formSubmissionRequest = new FormSubmissionRequest(request, leadGenerationService.getCompany(),
                    leadGenerationService.getRecordTypeId(), leadGenerationService.getLeadSource() );
            FormSubmissionResponse formSubmissionResponse = leadGenerationService.submitLeadGeneration(formSubmissionRequest.serialize(), sessionId);
            setResponse(resp, formSubmissionResponse);
        } catch (Exception e) {
            /*We don't want to send any exception back to the client instead send error response*/
            LOGGER.error("error while processing lead generation", e);
            FormSubmissionResponse formSubmissionResponse =  new FormSubmissionResponse(false, "SERVER_ERROR", "Internal Server Error", Collections.emptyList());
            formSubmissionResponse.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            setResponse(resp, formSubmissionResponse);
        } finally {
            execService.shutdown();
        }

    }


    /**
     * Set lead generation response
     * @param response                  Http Response
     * @param formSubmissionResponse    Lead generation response object
     * @throws IOException              Thrown in case of error while writing response
     */
    private void setResponse(SlingHttpServletResponse response, FormSubmissionResponse formSubmissionResponse) throws IOException {
        response.getWriter().println(formSubmissionResponse.serialize());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(GenericConstants.APPLICATION_JSON_CONTENT_TYPE);
        response.setStatus(formSubmissionResponse.getStatusCode());

    }

}
