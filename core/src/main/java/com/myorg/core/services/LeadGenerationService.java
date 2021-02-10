package com.myorg.core.services;

import com.myorg.core.beans.leadgeneration.FormSubmissionResponse;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * 
 * This is a service used to submit lead generation request
 *
 */
public interface LeadGenerationService extends RestClientService<String> {

    @ObjectClassDefinition(
            name = "Form Lead API Service Configuration",
            description = "Form Lead API Service Configuration Details.")
    @interface Config {
        @AttributeDefinition(
                name = "Form Lead API End Point",
                description = "Form Lead API End Point",
                type = AttributeType.STRING)
        String formLeadApiEndPoint();

        @AttributeDefinition(
                name = "Form Lead Client_ID",
                description = "Form Lead Client_ID",
                type = AttributeType.STRING)
        String formLeadClientID();

        @AttributeDefinition(
                name = "Form Lead Client_Secret",
                description = "Form Lead Client_Secret",
                type = AttributeType.STRING)
        String formLeadClientSecret();

        @AttributeDefinition(
                name = "Form Lead OAUTH_URL",
                description = "Form Lead OAUTH_URL",
                type = AttributeType.STRING)
        String oauthUrl();

        @AttributeDefinition(
                name = "Form Lead USER_NAME",
                description = "Form Lead USER_NAME",
                type = AttributeType.STRING)
        String userName();

        @AttributeDefinition(
                name = "Form Lead PASSWORD",
                description = "Form Lead PASSWORD",
                type = AttributeType.STRING)
        String password() default "";

        @AttributeDefinition(
                name = "Form Lead Record Type ID",
                description = "Form Lead Record Type ID",
                type = AttributeType.STRING)
        String recordTypeId() ;

        @AttributeDefinition(
                name = "Form Lead Source",
                description = "Form Lead Source",
                type = AttributeType.STRING)
        String leadSource();

        @AttributeDefinition(
                name = "Form Lead Company",
                description = "Form Lead Company",
                type = AttributeType.STRING)
        String company();

        @AttributeDefinition(
                name = "Service timeout",
                description = "Leadgeneration service timeout",
                type = AttributeType.STRING)
        long serviceTimeout() default 5;

        @AttributeDefinition(
                name = "Cache age",
                description = "token cache age",
                type = AttributeType.STRING)
        long cacheTTL() default 14;

        @AttributeDefinition(
                name = "Max size",
                description = "Maximum objects for token cache",
                type = AttributeType.STRING)
        long cacheMaxSize() default 10;

        @AttributeDefinition(name = "Print cache size",
                description = "Logs the information about token cache size if enabled",
                type = AttributeType.BOOLEAN)
        boolean printCacheSize() default false;
    }

    FormSubmissionResponse submitLeadGeneration(String leadGenerationRequest, String token);

    String getRecordTypeId();

    String getLeadSource();

    String getCompany();

}
