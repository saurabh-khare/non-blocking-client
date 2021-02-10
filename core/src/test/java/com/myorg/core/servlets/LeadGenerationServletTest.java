package com.myorg.core.servlets;

import com.myorg.core.beans.leadgeneration.FormSubmissionRequest;
import com.myorg.core.beans.leadgeneration.FormSubmissionResponse;
import com.myorg.core.beans.ServiceWorker;
import com.myorg.core.constants.FormConstants;
import com.myorg.core.constants.GenericConstants;
import com.myorg.core.services.GoogleReCaptchaService;
import com.myorg.core.services.LeadGenerationService;
import com.myorg.core.services.ZeroBounceService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadGenerationServletTest {

    private static final String RECAPTCHA_TOKEN = "random-token";

    private static final String SESSION_ID = "random-id";


    @InjectMocks
    LeadGenerationServlet leadGenerationServlet;

    SlingHttpServletRequest req;

    SlingHttpServletResponse res;

    @Mock
    FormSubmissionRequest formSubmissionRequest;

    @Mock
    GoogleReCaptchaService googleReCaptchaService;

    @Mock
    ZeroBounceService zeroBounceService;

    @Mock
    LeadGenerationService leadGenerationService;

    private final AemContext context = new AemContext();

    @BeforeEach
    public void setupMock() {
        req = getRequest();
        res = new MockSlingHttpServletResponse();
    }

    @Test
    void testDoPost() throws Exception {

        MockSlingHttpServletRequest mockSlingHttpServletRequest = new MockSlingHttpServletRequest(context.bundleContext());
        ServiceWorker salesforceWorker = new ServiceWorker(SESSION_ID);
        when(leadGenerationService.getServiceWorker(Collections.emptyMap())).thenReturn(salesforceWorker);
        when(leadGenerationService.getLeadSource()).thenReturn("my website");
        when(leadGenerationService.getCompany()).thenReturn("my company");

        ServiceWorker recaptchaWorker = new ServiceWorker(req.getParameter(FormConstants.CAPTCHA_TOKEN));
        when(googleReCaptchaService.getServiceWorker(Collections.singletonMap(GenericConstants.RESPONSE, req.getParameter(FormConstants.CAPTCHA_TOKEN)))).thenReturn(recaptchaWorker);
        when(googleReCaptchaService.getCacheData(any(ServiceWorker.class))).thenReturn(true);

        ServiceWorker zerobounceWorker = new ServiceWorker(req.getParameter(FormConstants.EMAIL));
        when(zeroBounceService.getServiceWorker(Collections.singletonMap(GenericConstants.EMAIL, req.getParameter(FormConstants.EMAIL)))).thenReturn(zerobounceWorker);
        when(zeroBounceService.getCacheData(any(ServiceWorker.class))).thenReturn(true);

        FormSubmissionResponse formSubmissionResponse =  new FormSubmissionResponse(true, null, null, Collections.emptyList());
        when(leadGenerationService.submitLeadGeneration(anyString(), anyString())).thenReturn(formSubmissionResponse);
        leadGenerationServlet.doPost(req, res);
        String output = ((MockSlingHttpServletResponse)res).getOutputAsString();
        verify(leadGenerationService).getServiceWorker(Collections.emptyMap());
        verify(leadGenerationService).getRecordTypeId();
        verify(leadGenerationService).getLeadSource();
        verify(leadGenerationService).getCompany();
        verify(googleReCaptchaService).getServiceWorker(Collections.singletonMap(GenericConstants.RESPONSE, req.getParameter(FormConstants.CAPTCHA_TOKEN)));
        verify(zeroBounceService).getServiceWorker(Collections.singletonMap(GenericConstants.EMAIL, req.getParameter(FormConstants.EMAIL)));
        assertNotNull(output);
    }

    private SlingHttpServletRequest getRequest() {
        MockSlingHttpServletRequest mockSlingHttpServletRequest = new MockSlingHttpServletRequest(context.bundleContext());
        mockSlingHttpServletRequest.addRequestParameter(FormConstants.CAPTCHA_TOKEN, RECAPTCHA_TOKEN);
        mockSlingHttpServletRequest.addRequestParameter(FormConstants.FIRST_NAME, "First");
        mockSlingHttpServletRequest.addRequestParameter(FormConstants.LAST_NAME, "Last");
        mockSlingHttpServletRequest.addRequestParameter(FormConstants.EMAIL, "first.last@example.com");
        mockSlingHttpServletRequest.addRequestParameter(FormConstants.DESCRIPTION, "Comments entered by consumer");
        mockSlingHttpServletRequest.addRequestParameter(FormConstants.PHONE, "3035551212");
        mockSlingHttpServletRequest.addRequestParameter(FormConstants.STREET, RECAPTCHA_TOKEN);
        mockSlingHttpServletRequest.addRequestParameter(FormConstants.CITY, RECAPTCHA_TOKEN);
        mockSlingHttpServletRequest.addRequestParameter(FormConstants.STATE, RECAPTCHA_TOKEN);
        mockSlingHttpServletRequest.addRequestParameter(FormConstants.POSTALCODE, RECAPTCHA_TOKEN);
        return  mockSlingHttpServletRequest;
    }



}
