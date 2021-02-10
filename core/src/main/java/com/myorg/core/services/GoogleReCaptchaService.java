package com.myorg.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * 
 * This is a service used to submit Google Re-Captcha request
 *
 */
public interface GoogleReCaptchaService extends RestClientService<Boolean> {

    @ObjectClassDefinition(
            name = "Google Re-Captcha Service Configuration",
            description = "Google Re-Captcha Service Configuration Details.")
    @interface Config {

        /**
         * Attribute for Google Re-Captcha Endpoint.
         *
         * @return googleReCaptchaEndPoint
         */
        @AttributeDefinition(name = "Google Re-Captcha End Point", description = "Google Re-Captcha End Point",
                type = AttributeType.STRING)
        String googleReCaptchaEndPoint() default "";

        /**
         * Attribute for Google Re-Captcha Secrete Key.
         *
         * @return googleReCaptchaSecretKey
         */
        @AttributeDefinition(name = "Google Re-Captcha Secret Key", description = "Google Re-Captcha Secret Key",
                type = AttributeType.STRING)
        String googleReCaptchaSecretKey() default "";

        /**
         * Attribute for Google Re-Captcha Site Key.
         *
         * @return googleReCaptchaSiteKey.
         */
        @AttributeDefinition(name = "Google Re-Captcha Site Key" ,description = "Google Re-Captcha Site Key",
                type = AttributeType.STRING)
        String googleReCaptchaSiteKey() default "";

        @AttributeDefinition(name = "Service timeout", description = "Recaptcha service timeout in seconds",
                type = AttributeType.STRING)
        long serviceTimeout() default 3;

        @AttributeDefinition(name = "Cache age", description = "Recaptcha cache age in minutes",
                type = AttributeType.STRING)
        long cacheTTL() default 2;

        @AttributeDefinition(name = "Max size", description = "Maximum no. of objects to be cached",
                type = AttributeType.STRING)
        long cacheMaxSize() default 5000;

        @AttributeDefinition(name = "Print cache size", description = "Logs the information about Recaptcha cache size if enabled",
                type = AttributeType.BOOLEAN)
        boolean printCacheSize() default false;
    }

    /**
     * Attribute for Google Re-Captcha Site Key.
     *
     * @return googleReCaptchaSiteKey.
     */
    String getGoogleReCaptchaSiteKey();


}
