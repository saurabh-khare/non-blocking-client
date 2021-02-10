package com.myorg.core.beans.leadgeneration;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.myorg.core.constants.FormConstants;
import com.myorg.core.constants.GenericConstants;

import javax.servlet.http.HttpServletRequest;

/**
 * Pojo class to store form data
 */
public class FormSubmissionRequest {

    @SerializedName("FirstName")
    private String firstName;

    @SerializedName("LastName")
    private String lastName;

    @SerializedName("Email")
    private final String email;

    @SerializedName("Description")
    private final String description;

    @SerializedName("Phone")
    private final String phone;

    @SerializedName("Street")
    private final String street;

    @SerializedName("City")
    private final String city;

    @SerializedName("State")
    private final String state;

    @SerializedName("PostalCode")
    private final String postalCode;

    @SerializedName("LeadSource")
    private final String leadSource;

    @SerializedName("Company")
    private final String company;

    public FormSubmissionRequest(HttpServletRequest request, final String company, final String recordTypeId, final String leadSource) {
        this.firstName = request.getParameter(FormConstants.FIRST_NAME);
        this.lastName = request.getParameter(FormConstants.LAST_NAME);
        this.email = request.getParameter(FormConstants.EMAIL);
        this.description = request.getParameter(FormConstants.DESCRIPTION);
        this.phone = request.getParameter(FormConstants.PHONE);
        this.street = request.getParameter(FormConstants.STREET);
        this.city = request.getParameter(FormConstants.CITY);
        this.state = request.getParameter(FormConstants.STATE);
        this.postalCode = request.getParameter(FormConstants.POSTALCODE);
        this.leadSource = leadSource;
        this.company = this.firstName + GenericConstants.SPACE + this.lastName + GenericConstants.SPACE + company;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getDescription() {
        return description;
    }

    public String getPhone() {
        return phone;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getLeadSource() {
        return leadSource;
    }

    public String getCompany() {
        return company;
    }

    public String serialize() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
