package com.myorg.core.beans.leadgeneration;

import com.google.gson.annotations.SerializedName;

public class ZeroBounceResponse {

    private String status;

    @SerializedName("sub_status")
    private String subStatus;

    private String address;

    @SerializedName("free_email")
    private boolean freeEmail;

    private String account;

    private String domain;

    @SerializedName("smtp_provider")
    private String smtpProvider;

    @SerializedName("processed_at")
    private String processedAt;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubStatus() {
        return subStatus;
    }

    public void setSubStatus(String subStatus) {
        this.subStatus = subStatus;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isFreeEmail() {
        return freeEmail;
    }

    public void setFreeEmail(boolean freeEmail) {
        this.freeEmail = freeEmail;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSmtpProvider() {
        return smtpProvider;
    }

    public void setSmtpProvider(String smtpProvider) {
        this.smtpProvider = smtpProvider;
    }

    public String getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(String processedAt) {
        this.processedAt = processedAt;
    }
}
