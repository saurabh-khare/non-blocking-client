package com.myorg.core.beans.leadgeneration;


import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;


/**
 * This class defines all status codes and sub codes and categorize if their combination
 * is valid or invalid
 */
public enum ZerobounceStatus {

    DEFAULT(StringUtils.EMPTY, StringUtils.EMPTY, true),

    VALID01("valid", "alias_address", true),

    VALID02("valid", "leading_period_removed", true),

    INVALID01(Constants.STATUS_INVALID, "mailbox_quota_exceeded", true),

    INVALID02(Constants.STATUS_INVALID, "does_not_accept_mail", false),

    INVALID03(Constants.STATUS_INVALID, "failed_syntax_check", false),

    INVALID04(Constants.STATUS_INVALID, "mailbox_not_found", false),

    INVALID05(Constants.STATUS_INVALID, "no_dns_entries", false),

    INVALID06(Constants.STATUS_INVALID, "possible_typo", false),

    INVALID07(Constants.STATUS_INVALID, "unroutable_ip_address", false),

    CATCH_ALL("catch-all ", StringUtils.EMPTY, true),

    SPAMTRAP("spamtrap", StringUtils.EMPTY, false),

    ABUSE("abuse", StringUtils.EMPTY, true),

    DO_NOT_MAIL01(Constants.STATUS_DO_NOT_MAIL, "global_suppression", true),

    DO_NOT_MAIL02(Constants.STATUS_DO_NOT_MAIL, "role_based_catch_all", true),

    DO_NOT_MAIL03(Constants.STATUS_DO_NOT_MAIL, "role_based", true),

    DO_NOT_MAIL04(Constants.STATUS_DO_NOT_MAIL, "disposable", false),

    DO_NOT_MAIL05(Constants.STATUS_DO_NOT_MAIL, "toxic", false),

    DO_NOT_MAIL06(Constants.STATUS_DO_NOT_MAIL, "possible_trap", false),

    UNKNOWN01(Constants.STATUS_UNKNOWN, "antispam_system", true),

    UNKNOWN02(Constants.STATUS_UNKNOWN, "exception_occurred", true),

    UNKNOWN03(Constants.STATUS_UNKNOWN, "failed_smtp_connection", true),

    UNKNOWN04(Constants.STATUS_UNKNOWN, "forcible_disconnect", true),

    UNKNOWN05(Constants.STATUS_UNKNOWN, "greylisted", true),

    UNKNOWN06(Constants.STATUS_UNKNOWN, "mail_server_did_not_respond", true),

    UNKNOWN07(Constants.STATUS_UNKNOWN, "mail_server_temporary_error", true),

    UNKNOWN08(Constants.STATUS_UNKNOWN, "timeout_exceeded", true);

    private final String status;

    private final String subStatus;

    private final boolean allowed;

    /**
     * Private constructor for enum.
     *
     * @param status
     *            main status
     * @param subStatus
     *            sub status
     */
    private ZerobounceStatus(final String status, final String subStatus, final boolean allowed) {
        this.status = status;
        this.subStatus = subStatus;
        this.allowed = allowed;
    }

    public String getStatus() {
        return status;
    }

    public String getSubStatus() {
        return subStatus;
    }

    public boolean isAllowed() {
        return allowed;
    }

    private static class Constants {
        public static final String STATUS_UNKNOWN = "unknown";
        public static final String STATUS_DO_NOT_MAIL = "do_not_mail";
        public static final String STATUS_INVALID = "invalid";
    }

    /**
     * Based on {@code ZeroBounceResponse} get Enum value for which
     * its status and sub status values match that of supplied zero bounce response.
     * In case if none found then return DEFAULT enum. ENUMS with third parameter
     * as true are considered valid statuses
     * @param response Zero bounce response
     * @return ZerobounceStatus enum value
     */
    public static ZerobounceStatus of(final ZeroBounceResponse response){
        return Arrays.stream(values()).filter(value -> value.getStatus().equals(response.getStatus()))
                .filter(value -> value.getSubStatus().equals(response.getSubStatus())).findFirst().orElse(DEFAULT);
    }
}
