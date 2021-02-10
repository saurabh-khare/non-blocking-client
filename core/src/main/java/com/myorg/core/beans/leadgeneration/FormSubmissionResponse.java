package com.myorg.core.beans.leadgeneration;

import com.google.gson.Gson;
import org.apache.http.HttpStatus;

import java.util.Collections;
import java.util.List;

public class FormSubmissionResponse {

    private transient String id;

    private boolean success;

    private transient int statusCode = HttpStatus.SC_OK;

    private List<Error> errors;

    public FormSubmissionResponse(){}

    public FormSubmissionResponse(boolean success, String errorCode, String errorMessage, List<String> errorFields){
        this.success = success;
        FormSubmissionResponse.Error error = this.new Error(errorCode, errorMessage, errorFields);
        this.errors = Collections.singletonList(error);
        if (!success) {
            this.statusCode = HttpStatus.SC_BAD_REQUEST;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Error> getErrors() {
        return getImmutableList(errors);
    }

    public void setErrors(List<Error> errors) {
        this.errors = getImmutableList(errors);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    private static <T> List<T> getImmutableList(List<? extends T> mutableList){
        if (mutableList == null) {
            return null;
        }
        return Collections.unmodifiableList(mutableList);
    }

    public class Error {

        public Error(){}

        public Error(String errorCode, String message, List<String> fields) {
            this.errorCode = errorCode;
            this.message = message;
            this.fields = getImmutableList(fields);
        }

        private String message;

        private String errorCode;

        private List<String> fields;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public List<String> getFields() {
            return getImmutableList(fields);
        }

        public void setFields(List<String> fields) {
            this.fields = getImmutableList(fields);
        }

    }

    public String serialize() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
