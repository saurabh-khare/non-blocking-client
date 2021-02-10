package com.myorg.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

public interface ZeroBounceService extends RestClientService<Boolean> {

    @ObjectClassDefinition(
            name = "Zerobounce API Service Configuration",
            description = "Endpoint configuration for Zerobounce service")
    @interface Config {

        @AttributeDefinition(
                name = "Zerobounce Enpoint Url",
                description = "Zeroubounce endpoint url",
                type = AttributeType.STRING)
        String zerobounceEndpoint() default "https://api.zerobounce.net/v2/validate";

        @AttributeDefinition(
                name = "Zerobounce API key",
                description = "Zeroubounce API key",
                type = AttributeType.STRING)
        String zerobounceAPIKey();

        @AttributeDefinition(
                name = "Service timeout in seconds",
                description = "Zeroubounce service timeout",
                type = AttributeType.STRING)
        long serviceTimeout() default 3;

        @AttributeDefinition(
                name = "Cache age",
                description = "Zeroubounce cache age in minutes",
                type = AttributeType.STRING)
        long cacheTTL() default 43200;

        @AttributeDefinition(
                name = "Max size",
                description = "Max no. of objects in cache",
                type = AttributeType.STRING)
        long cacheMaxSize() default 10000;

        @AttributeDefinition(name = "Print cache size",
                description = "Logs the information about Zerobounce cache size if enabled",
                type = AttributeType.BOOLEAN)
        boolean printCacheSize() default false;
    }
}
