package org.example.tool_tax_code;

public class RequestPayload {
    private String action;
    private String index;
    private String taxCode;
    private String config_aws;
    private String config_send_email;
    private String path_file_tax_code;

    // Getter và Setter
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getConfig_aws() {
        return config_aws;
    }

    public void setConfig_aws(String config_aws) {
        this.config_aws = config_aws;
    }

    public String getConfig_send_email() {
        return config_send_email;
    }

    public void setConfig_send_email(String config_send_email) {
        this.config_send_email = config_send_email;
    }

    public String getPath_file_tax_code() {
        return path_file_tax_code;
    }

    public void setPath_file_tax_code(String path_file_tax_code) {
        this.path_file_tax_code = path_file_tax_code;
    }
}
