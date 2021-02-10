package com.myorg.core.models;

import com.myorg.core.services.GoogleReCaptchaService;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;

@Model(adaptables = { Resource.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class GoogleReCaptchaModel {

    @Inject
    GoogleReCaptchaService googleReCaptchaService;

    @Inject
    @Optional
    @ValueMapValue(name = "siteKey", injectionStrategy = InjectionStrategy.OPTIONAL)
    private String siteKey;

    public String getSiteKey() {

        if (StringUtils.isNotBlank(googleReCaptchaService.getGoogleReCaptchaSiteKey())) {
            siteKey = googleReCaptchaService.getGoogleReCaptchaSiteKey();
        }
        return siteKey;
    }

}
