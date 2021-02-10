package com.myorg.core.beans;


public enum ServiceRequestType {

    RECAPTCHA("Google Recaptcha", "Request for validating recaptcha token"),

    ZEROBOUNCE("Zerobounce", "Request for validating email"),

    AUTH_TOKEN("Auth token", "Request for generating authentication token");


    private final String name;

    private final String description;

    /**
     * Private constructor for enum.
     *
     * @param name
     *            service name
     * @param description
     *            service description
     */
    private ServiceRequestType(final String name, final String description) {
        this.name = name;
        this.description = description;

    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
