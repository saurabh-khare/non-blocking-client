package com.myorg.core.beans;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpRequestFutureTask;


public class ServiceWorker {

    private final String id;

    private ServiceRequestType serviceRequestType;

    private HttpUriRequest requestPayload;

    private HttpRequestFutureTask<String> serviceTask;

    public ServiceWorker(String id, ServiceRequestType serviceRequestType, HttpUriRequest requestPayload) {
        this.id = StringUtils.abbreviate(id, 60);
        this.serviceRequestType = serviceRequestType;
        this.requestPayload = requestPayload;
    }

    public ServiceWorker(String id) {
        this.id = StringUtils.abbreviate(id, 60);
    }

    public ServiceRequestType getServiceRequest() {
        return serviceRequestType;
    }

    public HttpUriRequest getRequestPayload() {
        return requestPayload;
    }

    public HttpRequestFutureTask<String> getServiceTask() {
        return serviceTask;
    }

    public String getId() {
        return id;
    }

    public void setServiceTask(HttpRequestFutureTask<String> serviceTask) {
        this.serviceTask = serviceTask;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ServiceWorker that = (ServiceWorker) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "ServiceWorker{" +
                "id='" + id + '\'' +
                '}';
    }
}
