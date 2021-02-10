package com.myorg.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class,defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class LeadGeneration {

    @ValueMapValue
    private String submitAction;

    @ValueMapValue
    private String link;

    @ValueMapValue
    private String buttonHeadline;

    @ValueMapValue
    private String headline;

    @ValueMapValue
    private String buttonText;

    @ValueMapValue
    private String text;

    @ValueMapValue
    private String buttonLink;
    
    @ValueMapValue
    private String buttonTarget;    

    @ValueMapValue
    private String buttonLabel;

    @ValueMapValue
    private String actionURL;

    @ValueMapValue
    private String formType;
    
    @ValueMapValue
    private String errorTitle;
    
    @ValueMapValue
    private String errorText;

    public String getSubmitAction() {
        return submitAction;
    }

    public String getLink() {
        return link;
    }

    public String getButtonHeadline() {
        return buttonHeadline;
    }

    public String getHeadline() {
        return headline;
    }

    public String getButtonText() {
        return buttonText;
    }

    public String getText() {
        return text;
    }

    public String getButtonLink() {
        return buttonLink;
    }

    public String getButtonLabel() {
        return buttonLabel;
    }

    public String getActionURL() {
        return actionURL;
    }

    public String getFormType() {
        return formType;
    }

    public String getErrorTitle() {
        return errorTitle;
    }

    public String getErrorText() {
        return errorText;
    }
    
    public String getButtonTarget() {
        return buttonTarget;
    }    

}
