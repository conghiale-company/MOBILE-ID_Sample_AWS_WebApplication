package org.example.payload;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class RespPayload {
    private boolean isRunning;

    private String taxCode;
    private String config_aws;
    private String configSendEmail;
    private String pathFileTaxCodes;

    private String startDay;
    private String endDay;
    private String previousTaxCode;
    private String previousStatus;
    private String currentTaxCode;
    private String currentStatus;
    private String message;

    private int index = -1;
    private int numberNotFound = 0;
    private int numberParameterIsInvalid = 0;
    private int numberUnknownException = 0;
    private int numberResponseIsNull = 0;
    private int numberCaptchaInvalid = 0;
    private int numberErrors = 0;
    private int numberSuccessfully = 0;
    private int numberTaxCode = 0;
    private int previousIndex = -1;
    private int currentIndex = -1;

    public RespPayload() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        this.isRunning = running;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
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

    public String getConfigSendEmail() {
        return configSendEmail;
    }

    public void setConfigSendEmail(String configSendEmail) {
        this.configSendEmail = configSendEmail;
    }

    public String getPathFileTaxCodes() {
        return pathFileTaxCodes;
    }

    public void setPathFileTaxCodes(String pathFileTaxCodes) {
        this.pathFileTaxCodes = pathFileTaxCodes;
    }

    public String getStartDay() {
        return startDay;
    }

    public void setStartDay(String startDay) {
        this.startDay = startDay;
    }

    public String getEndDay() {
        return endDay;
    }

    public void setEndDay(String endDay) {
        this.endDay = endDay;
    }

    public String getPreviousTaxCode() {
        return previousTaxCode;
    }

    public void setPreviousTaxCode(String previousTaxCode) {
        this.previousTaxCode = previousTaxCode;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getCurrentTaxCode() {
        return currentTaxCode;
    }

    public void setCurrentTaxCode(String currentTaxCode) {
        this.currentTaxCode = currentTaxCode;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public int getNumberNotFound() {
        return numberNotFound;
    }

    public void setNumberNotFound(int numberNotFound) {
        this.numberNotFound = numberNotFound;
    }

    public int getNumberParameterIsInvalid() {
        return numberParameterIsInvalid;
    }

    public void setNumberParameterIsInvalid(int numberParameterIsInvalid) {
        this.numberParameterIsInvalid = numberParameterIsInvalid;
    }

    public int getNumberUnknownException() {
        return numberUnknownException;
    }

    public void setNumberUnknownException(int numberUnknownException) {
        this.numberUnknownException = numberUnknownException;
    }

    public int getNumberResponseIsNull() {
        return numberResponseIsNull;
    }

    public void setNumberResponseIsNull(int numberResponseIsNull) {
        this.numberResponseIsNull = numberResponseIsNull;
    }

    public int getNumberCaptchaInvalid() {
        return numberCaptchaInvalid;
    }

    public void setNumberCaptchaInvalid(int numberCaptchaInvalid) {
        this.numberCaptchaInvalid = numberCaptchaInvalid;
    }

    public int getNumberErrors() {
        return numberErrors;
    }

    public void setNumberErrors(int numberErrors) {
        this.numberErrors = numberErrors;
    }

    public int getNumberSuccessfully() {
        return numberSuccessfully;
    }

    public void setNumberSuccessfully(int numberSuccessfully) {
        this.numberSuccessfully = numberSuccessfully;
    }

    public int getNumberTaxCode() {
        return numberTaxCode;
    }

    public void setNumberTaxCode(int numberTaxCode) {
        this.numberTaxCode = numberTaxCode;
    }

    public int getPreviousIndex() {
        return previousIndex;
    }

    public void setPreviousIndex(int previousIndex) {
        this.previousIndex = previousIndex;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public JsonObject toJson() {
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

        // Add all fields to the JSON object
        jsonBuilder
                .add("isRunning", isRunning)
                .add("taxCode", taxCode != null ? taxCode : "")
                .add("config_aws", config_aws != null ? config_aws : "")
                .add("configSendEmail", configSendEmail != null ? configSendEmail : "")
                .add("pathFileTaxCodes", pathFileTaxCodes != null ? pathFileTaxCodes : "")
                .add("startDay", startDay != null ? startDay : "")
                .add("endDay", endDay != null ? endDay : "")
                .add("previousTaxCode", previousTaxCode != null ? previousTaxCode : "")
                .add("previousStatus", previousStatus != null ? previousStatus : "")
                .add("currentTaxCode", currentTaxCode != null ? currentTaxCode : "")
                .add("currentStatus", currentStatus != null ? currentStatus : "")
                .add("message", message != null ? message : "")
                .add("index", index)
                .add("numberNotFound", numberNotFound)
                .add("numberParameterIsInvalid", numberParameterIsInvalid)
                .add("numberUnknownException", numberUnknownException)
                .add("numberResponseIsNull", numberResponseIsNull)
                .add("numberCaptchaInvalid", numberCaptchaInvalid)
                .add("numberErrors", numberErrors)
                .add("numberSuccessfully", numberSuccessfully)
                .add("numberTaxCode", numberTaxCode)
                .add("previousIndex", previousIndex)
                .add("currentIndex", currentIndex);

        return jsonBuilder.build();
    }

    @Override
    public String toString() {
        return "TaxCodeInfoResp{" +
                "running=" + isRunning +
                ", index=" + index +
                ", taxCode='" + taxCode + '\'' +
                ", config_aws='" + config_aws + '\'' +
                ", configSendEmail='" + configSendEmail + '\'' +
                ", pathFileTaxCodes='" + pathFileTaxCodes + '\'' +
                ", startDay='" + startDay + '\'' +
                ", endDay='" + endDay + '\'' +
                ", previousTaxCode='" + previousTaxCode + '\'' +
                ", previousStatus='" + previousStatus + '\'' +
                ", currentTaxCode='" + currentTaxCode + '\'' +
                ", currentStatus='" + currentStatus + '\'' +
                ", message='" + message + '\'' +
                ", numberNotFound=" + numberNotFound +
                ", numberParameterIsInvalid=" + numberParameterIsInvalid +
                ", numberUnknownException=" + numberUnknownException +
                ", numberDataResponseIsNull=" + numberResponseIsNull +
                ", numberCaptchaInvalid=" + numberCaptchaInvalid +
                ", numberErrors=" + numberErrors +
                ", numberSuccessfully=" + numberSuccessfully +
                ", numberTaxCode=" + numberTaxCode +
                ", previousIndex=" + previousIndex +
                ", currentIndex=" + currentIndex +
                '}';
    }
}