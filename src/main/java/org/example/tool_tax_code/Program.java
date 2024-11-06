package org.example.tool_tax_code;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.example.payload.RespPayload;
import org.example.socket.SocketHandleTaxCode;

import javax.json.Json;
import javax.json.JsonObject;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Program {

    static{
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm"); // "dd-MM-yyyy-hh-mm-ss"
        System.setProperty("current.date", dateFormat.format(new Date()));
//        System.setProperty("current.date", java.time.LocalDate.now().toString());
    }

    private static final Logger LOG = Logger.getLogger(Program.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String PATH_TAX_CODE = "src/main/resources/Tax_Code.txt";
    private static final String PATH_TAX_CODE_02 = "src/main/resources/Tax_Code_02.txt";

    private static final int PAGE = 1000;
    private static final int TIME_WAIT = 300;
    private static final int PROCESS_TIME_PER_TAXCODE_FIRST = 2000; // 2000 ms = 2 seconds
    private static final int PROCESS_TIME_PER_TAXCODE_AFTER = 3000; // 3000 ms = 3 seconds
    private static final int MAX_TIME_FOR_ADDING_QUEUE = 5 * 60 * 1000; // 5 minutes in milliseconds
    private static final int MAX_RETRIES = 60; // Số lần thử tối đa
    private static final Object lock = new Object();

    private static int expiresIn = 0;
    private static int posStartPage = 0;
    private static int posEndPage = 0;
    private static int lineNumber = 0;
    private static int currentIndex = -1;

    private static int sumNumberNotFound = 0;
    private static int sumNumberParameterIsInvalid = 0;
    private static int sumNumberUnknownException = 0;
    private static int sumNumberDataResponseIsNull = 0;
    private static int sumNumberCaptchaInvalid = 0;
    private static int sumNumberErrors = 0;
    private static int sumNumberSuccessfully = 0;
    private static int sumNumberTaxCode = 0;

    private static String FROM_EMAIL; //requires valid gmail id
    private static String PASSWORD; // correct password for gmail id
    private static String TO_EMAIL; // can be any email id
    private static String SMTP_HOST;
    private static String TLS_PORT;
    private static String ENABLE_AUTHENTICATION;
    private static String ENABLE_STARTTLS;

    private static String jsonResp;
    private static String accessToken = null;
    private static String PATH = "";
    private static String PATH_AWS_CONFIG = "";
    private static String PATH_SEND_EMAIL_CONFIG = "";

    private static String startDay = "";
    private static String endDay = "";
    private static String currentTaxCode = "";

    private static int index = -1;
    private static String taxCode;

    private static boolean isLogin = false;
    private static boolean isSendTaxCodeToServer = true;
    private static JsonNode jsonNode;
    private static Function func;

    //    private static List<String> taxCodes;
    private static RespPayload respPayload;
    private static List<String> taxCodeList;
    private static List<TaxCodeInfo> taxCodeInfoList;
    private  static RespPayload payload;

    public static void running(String[] args) throws Exception {//
        if (args.length > 1 && args[0] != null && !args[0].isEmpty() && args[1] != null && !args[1].isEmpty()) {
            PATH_AWS_CONFIG = args[0];
            PATH_SEND_EMAIL_CONFIG = args[1];
        } else {
            LOG.debug("Invalid parameter");
            System.exit(0);
        }

        func = new Function(PATH_AWS_CONFIG);

        login();

        if (isLogin) {
            startDay = getDayTime();
            if (args.length > 2 && args[2] != null && !args[2].isEmpty()) {
                if (args[2].contains(".txt")) {
                    PATH = args[2];

                    if (args.length > 3 && args[3] != null && !args[3].isEmpty())
                        loadData(args[3], false);
                    else
                        loadData(null, false);
                } else {
                    if (args[3] != null && !args[3].isEmpty()) {
                        if (args[3].contains(".txt")) {
                            PATH = args[3];
                            loadData(args[2], true);
                        } else {
                            LOG.debug("The fourth parameter is invalid");
                            System.exit(0);
                        }
                    } else {
                        LOG.debug("Invalid parameter");
                        System.exit(0);
                    }
                }
            } else {
                LOG.debug("Invalid parameter");
                System.exit(0);
            }
        } else {
            JsonObject jsonResponse = Json.createObjectBuilder()
                    .add("status", "[ERROR] LOGIN_FAILED")
                    .add("message", "TOOL HAS LOGIN FAILED")
                    .add("taxCode", taxCode)
                    .add("index", index)
                    .add("PATH_AWS_CONFIG", PATH_AWS_CONFIG)
                    .add("PATH_SEND_EMAIL_CONFIG", PATH_SEND_EMAIL_CONFIG)
                    .add("PATH_SEND_EMAIL_CONFIG", PATH_SEND_EMAIL_CONFIG)
                    .add("startDay", getDayTime())
                    .add("endDay", "")
                    .build();
            SocketHandleTaxCode.sendMessageToAll(jsonResponse.toString());
            System.exit(0);
        }
    }

    private static void login() throws IOException {
        LOG.debug("Logging...");
        isLogin = false;

        try {
            jsonResp = func.login();
        } catch (Exception e) {
            LOG.debug("Login failed: " + e);
//            System.exit(0);
        }

        try {
            jsonNode = objectMapper.readTree(jsonResp);
        } catch (IOException e) {
            LOG.debug("Login failed: " + e);
//            System.exit(0);
        }
        accessToken = jsonNode.get("access_token").asText();
        expiresIn = jsonNode.path("expires_in").asInt();
//        expiresIn = 5;

        if (expiresIn > 0)
            isLogin = Utils.isTokenValid(expiresIn, true);

        if (isLogin) {
            LOG.debug("Login successful");

//            Tao mot ScheduledExecutorService de gui email dinh ky
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

//            Dinh ky gui email moi 2h
            scheduledExecutorService.scheduleAtFixedRate(Program::sendHeartbeatEmail, 2, 2, TimeUnit.HOURS);
            LOG.debug("Heartbeat monitor started...");
        } else {
            LOG.debug("Login failed");
        }
    }

    private static void sendHeartbeatEmail() {
        sendEmail("HEARTBEAT TAX-INFO", "This is a heartbeat email sent every 2 hours to monitor the " +
                "TAX-INFO tool.\nStatus: NORMAL ACTIVITY");
    }

    private static void loadData(String param, boolean isMST) throws Exception  {
        try {
//        Tạo phản hồi JSON
            LOG.debug("loadData...");
            JsonObject jsonResponse = Json.createObjectBuilder()
                    .add("status", "LOGIN_SUCCESS")
                    .add("message", "TOOL HAS STARTED")
                    .add("taxCode", taxCode == null ? "" : taxCode)
                    .add("index", index)
                    .add("config_aws", PATH_AWS_CONFIG)
                    .add("configSendEmail", PATH_SEND_EMAIL_CONFIG)
                    .add("pathFileTaxCodes", PATH)
                    .add("startDay", startDay)
                    .build();
            SocketHandleTaxCode.sendMessageToAll(jsonResponse.toString());

            if (param != null && !param.isEmpty()) {
                if (isMST) {
                    handleLoadData(param);
                    taxCode = param;
                }
                else {
                    posStartPage = Integer.parseInt(param);
                    index = posStartPage;
                    System.out.println();
                    LOG.debug("Loading data at index: " + posStartPage);
                    handleLoadData(null);
                }
            } else
                handleLoadData(null);
        } catch (Exception e) {
            LOG.debug("Loading data failed: " + e);
        }
    }

    private static void handleLoadData(String taxCode) throws Exception
    {
        if (taxCodeList == null || taxCodeList.isEmpty()) {
            taxCodeInfoList = new ArrayList<>();
            taxCodeList = new ArrayList<>();
        } else {
            taxCodeList.clear();
        }

        LOG.debug("Reading file into ram...");
        try (BufferedReader br = new BufferedReader(new FileReader(PATH))) {
            String line;
            lineNumber = 0;
            boolean isLoad = false;

            if (taxCode != null && !taxCode.isEmpty())
                posStartPage = -1;

            while ((line = br.readLine()) != null) {
                if (!isLoad) {
                    if (lineNumber == posStartPage) {
                        isLoad = true;
                    }

                    if (taxCode != null && !taxCode.isEmpty() && line.equals(taxCode)) {
                        LOG.debug("Found Tax_Code " + taxCode + " with index " + lineNumber);
                        posStartPage = lineNumber;
                        isLoad = true;
                    }
                }

                if (isLoad)
                    getTaxCodeToRam(line);

                lineNumber++;
            }

            if (!taxCodeList.isEmpty()) {
                LOG.debug("Loaded " + taxCodeList.size() + " tax code from " + posStartPage);
                getInfoDN();
            }

            if (posStartPage == -1) {
                JsonObject fullResponse = Json.createObjectBuilder()
                        .add("status", "[ERROR] NOT FOUND INDEX OR TAX CODE")
                        .add("message", "REQUESTED INDEX OR TAX CODE NOT FOUND")
                        .build();
                SocketHandleTaxCode.sendMessageToAll(fullResponse.toString());

                System.out.println();
                LOG.debug("Not found tax code: " + taxCode + " with path " + PATH);
                System.exit(0);
            }

//                updateTaxCodeInfoResp("", -1, "SUCCESS: ALL TAX CODES COMPLETED");
            JsonObject fullResponse = Json.createObjectBuilder()
                    .add("status", "[STOP] ALL TAX CODES COMPLETED")
                    .add("message", "ALL TAX CODES COMPLETED")
                    .build();
            SocketHandleTaxCode.sendMessageToAll(fullResponse.toString());

            System.out.println();
            LOG.debug("successfully got all business information with path " + PATH);
            endDay = getDayTime();
            String subject = "ALL TAX CODES COMPLETED";
            String body = "INFORMATION OF ALL TAX CODES RETRIEVED: \n" +
                    "\tStart day: " + startDay + "\n" +
                    "\tEnd day: " + endDay + "\n" +
                    "\tPath File: " + PATH + "\n" +
                    "\tNumber of tax codes read: " + sumNumberTaxCode + "\n";
            sendEmail(subject, body);
            System.exit(0);

        } catch (IOException e) {
            JsonObject fullResponse = Json.createObjectBuilder()
                    .add("status", "[ERROR] TOOL TAX INFO AN ERROR OCCURRED")
                    .add("message", "TOOL TAX INFO AN ERROR OCCURRED: " + e.getMessage())
                    .build();
            SocketHandleTaxCode.sendMessageToAll(fullResponse.toString());
            logInfo();

            String formattedDateTime = getDayTime();
            String subject = "TOOL TAX INFO AN ERROR OCCURRED";
            String body = "ERROR INFORMATION: \n" +
                    "\tCode: NULL\n" +
                    "\tMessage: " + e.getMessage() + "\n" +
                    "\tDay: " + formattedDateTime + "\n" +
                    "\tMST: " + currentTaxCode + "\n" +
                    "\tIndex: " + currentIndex;

            sendEmail(subject, body);
            stopTool(e, true);
        }
    }

    private static void getTaxCodeToRam(String line) throws Exception {
        taxCodeList.add(line);

        if (taxCodeList.size() % PAGE == 0) {
            LOG.debug("Loaded " + taxCodeList.size() + " tax code from " + posStartPage);
            getInfoDN();
            taxCodeList.clear();
        }
    }

    private static void getInfoDN() throws Exception {
        if (taxCodeList != null && !taxCodeList.isEmpty()) {
            posEndPage = posStartPage + taxCodeList.size() - 1;
            LOG.debug("Retrieving business information... ");
            for (int i = 0; i < taxCodeList.size(); i++) {
                if (isLogin && Utils.isTokenValid(expiresIn, false)) {
                    handleActionGetDN(i);
//                    updateTaxCodeInfoResp(taxCodeList.get(i), i, "WAITING");
                }
                else {
                    LOG.debug("The access token has expired");
                    login();
                    if (isLogin) {
                        handleActionGetDN(i);
//                        updateTaxCodeInfoResp(taxCodeList.get(i), i, "WAITING");
                    }
                    else
                        LOG.debug("login failed");
                }

                if (isSendTaxCodeToServer)
                    Utils.wait(PROCESS_TIME_PER_TAXCODE_FIRST);

                Utils.wait(2000);
            }
        }
    }

    private static void handleActionGetDN(int i) throws IOException {
        String formattedDateTime;
        currentTaxCode = taxCodeList.get(i);
        currentIndex = posStartPage + i;
        sumNumberTaxCode++;
        endDay = getDayTime();

//        Begin RespPayload
        payload = new RespPayload();
        payload.setRunning(true);
        payload.setIndex(index);
        payload.setTaxCode((taxCode == null) ? "" : taxCode);
        payload.setConfig_aws(PATH_AWS_CONFIG);
        payload.setConfigSendEmail(PATH_SEND_EMAIL_CONFIG);
        payload.setPathFileTaxCodes(PATH);

        payload.setStartDay(startDay);
        payload.setEndDay("");

        if (!taxCodeInfoList.isEmpty()) {
            payload.setPreviousTaxCode(taxCodeInfoList.get(taxCodeInfoList.size() - 1).getTaxCode());
            payload.setPreviousStatus(taxCodeInfoList.get(taxCodeInfoList.size() - 1).getStatus());
            payload.setPreviousIndex(taxCodeInfoList.get(taxCodeInfoList.size() - 1).getIndex());
        }

        payload.setCurrentIndex(currentIndex);
        payload.setCurrentTaxCode(currentTaxCode);
        payload.setCurrentStatus("PROCESSING...");

        payload.setNumberNotFound(sumNumberNotFound);
        payload.setNumberParameterIsInvalid(sumNumberParameterIsInvalid);
        payload.setNumberUnknownException(sumNumberUnknownException);
        payload.setNumberResponseIsNull(sumNumberDataResponseIsNull);
        payload.setNumberCaptchaInvalid(sumNumberCaptchaInvalid);
        payload.setNumberErrors(sumNumberErrors);
        payload.setNumberSuccessfully(sumNumberSuccessfully);
        payload.setNumberTaxCode(sumNumberTaxCode);
//        End RespPayload

        System.out.println();
        LOG.debug("Retrieving business information with tax code " + currentTaxCode);

        JsonObject fullResponse = Json.createObjectBuilder()
//                .add("status", "[ERROR] Exception")
                .add("RespPayload", payload.toJson()) // Adding the payload JSON as a nested object
                .build();
        SocketHandleTaxCode.sendMessageToAll(fullResponse.toString());

        jsonResp = null;

        if (currentTaxCode.length() < 10) {
            isSendTaxCodeToServer = false;
            sumNumberErrors++;
            sumNumberParameterIsInvalid++;
            LOG.info("BUG: 4000 - PARAMETER IS INVALID" + " -- INDEX = " + currentIndex + " -- MST = " + currentTaxCode);

            payload.setCurrentStatus("INVALID");
            payload.setNumberErrors(sumNumberErrors);
            payload.setNumberParameterIsInvalid(sumNumberParameterIsInvalid);
        }
        else {
            try {
                jsonResp = func.getDN(accessToken, currentTaxCode);
                waitJsonResp(currentTaxCode, i);

                if (jsonResp != null) {
                    isSendTaxCodeToServer = true;
                    try {
                        jsonNode = objectMapper.readTree(jsonResp);
                        int status = jsonNode.path("status").asInt();
                        String mess = jsonNode.path("message").asText();
//                        String dtis_id = jsonNode.path("dtis_id").asText();
                        LOG.debug("posStartPage: " + posStartPage + " --- posEndPage: " + posEndPage + " --- status: " +
                                status + " --- mess: " + mess + " --- index: " + currentIndex ); // + " --- dtis_id: " + dtis_id

                        if (status == 0 || mess.equals("SUCCESSFULLY")) {
                            sumNumberSuccessfully++;
                            payload.setCurrentStatus("SUCCESS");
                            payload.setNumberSuccessfully(sumNumberSuccessfully);

//                            updateTaxCodeInfoResp(currentTaxCode, i, "SUCCESS");
                            taxCodeInfoList.add(new TaxCodeInfo(currentTaxCode, currentIndex, "SUCCESS"));

                            LOG.info("INDEX = " + currentIndex + " -- MST = " + currentTaxCode);
                        } else if (status == 4044 || mess.contains("BUSINESS INFORMATION NOT FOUND")) {
                            sumNumberErrors++;
                            sumNumberNotFound++;
                            payload.setCurrentStatus("NOT FOUND");
                            payload.setNumberErrors(sumNumberErrors);
                            payload.setNumberNotFound(sumNumberNotFound);

//                            updateTaxCodeInfoResp(currentTaxCode, i, "NOT FOUND");
                            taxCodeInfoList.add(new TaxCodeInfo(currentTaxCode, currentIndex, "NOT FOUND"));

                            LOG.info("BUG: " + status + " - " + mess + " -- INDEX = " + currentIndex + " -- MST = " + currentTaxCode);
                        } else if (status == 4001 || mess.contains("UNKNOWN EXCEPTION") || status == 4045 || mess.contains("ERROR CONNECTING TO ENTITY, PLEASE TRY AGAIN LATER")) {
                            boolean isErrorUnknownException = true;

                            for (int j = 0; j < 3; j++) {
                                LOG.debug(status + ": " + mess + " - Retry MST " + currentTaxCode + ": [" + (j + 1) + "]");
                                try {
                                    jsonResp = func.getDN(accessToken, currentTaxCode);
                                    waitJsonResp(currentTaxCode, i);

                                    if (jsonResp != null) {
                                        jsonNode = objectMapper.readTree(jsonResp);
                                        status = jsonNode.path("status").asInt();
                                        mess = jsonNode.path("message").asText();
//                                       dtis_id = jsonNode.path("dtis_id").asText();
                                        LOG.debug("posStartPage: " + posStartPage + " --- posEndPage: " + posEndPage + " --- status: " +
                                                status + " --- mess: " + mess + " --- index: " + currentIndex ); // + " --- dtis_id: " + dtis_id
//                                        LOG.debug("status: " + status + " --- mess: " + mess);

                                        if (status == 0 || mess.contains("SUCCESSFULLY")) {
                                            sumNumberSuccessfully++;
                                            payload.setCurrentStatus("SUCCESS");
                                            payload.setNumberSuccessfully(sumNumberSuccessfully);

                                            taxCodeInfoList.add(new TaxCodeInfo(currentTaxCode, currentIndex, "SUCCESS"));

                                            LOG.info("INDEX = " + currentIndex + " -- MST = " + currentTaxCode);
                                            isErrorUnknownException = false;
                                            break;
                                        } else if (status == 4044 && mess.contains("BUSINESS INFORMATION NOT FOUND")) {
                                            sumNumberErrors++;
                                            sumNumberNotFound++;
                                            payload.setCurrentStatus("NOT FOUND");
                                            payload.setNumberErrors(sumNumberErrors);
                                            payload.setNumberNotFound(sumNumberNotFound);

                                            taxCodeInfoList.add(new TaxCodeInfo(currentTaxCode, currentIndex, "NOT FOUND"));

                                            LOG.info("BUG: " + status + " - " + mess + " -- INDEX = " + currentIndex + " -- MST = " + currentTaxCode);
                                            isErrorUnknownException = false;
                                            break;
                                        }
                                    }
                                } catch (Exception e) {
                                    logInfo();

                                    payload.setRunning(false);
                                    fullResponse = Json.createObjectBuilder()
                                            .add("status", "[ERROR] Exception")
                                            .add("message", e.getMessage())
                                            .add("RespPayload", payload.toJson()) // Adding the payload JSON as a nested object
                                            .build();
                                    SocketHandleTaxCode.sendMessageToAll(fullResponse.toString());

//                                    updateTaxCodeInfoResp(currentTaxCode, currentIndex, "ERROR: " + e);

                                    formattedDateTime = getDayTime();
                                    String subject = "TOOL TAX INFO AN ERROR OCCURRED";
                                    String body = "ERROR INFORMATION: \n" +
                                            "\tCode: NULL\n" +
                                            "\tMessage: " + e.getMessage() + "\n" +
                                            "\tDay: " + formattedDateTime + "\n" +
                                            "\tMST: " + currentTaxCode + "\n" +
                                            "\tIndex: " + currentIndex;

                                    sendEmail(subject, body);
                                    stopTool(e, true);
                                }
                            }

                            if (isErrorUnknownException) {
                                sumNumberErrors++;
                                if (status == 4001) {
                                    sumNumberUnknownException++;
                                    payload.setCurrentStatus("UNKNOWN EXCEPTION");
                                    payload.setNumberUnknownException(sumNumberUnknownException);

                                    taxCodeInfoList.add(new TaxCodeInfo(currentTaxCode, i, "UNKNOWN EXCEPTION"));
                                } else if (status == 4045) {
                                    sumNumberCaptchaInvalid++;
                                    payload.setCurrentStatus("CAPTCHA INVALID");
                                    payload.setNumberCaptchaInvalid(sumNumberCaptchaInvalid);

                                    taxCodeInfoList.add(new TaxCodeInfo(currentTaxCode, i, "CAPTCHA INVALID"));
                                }

                                LOG.debug("BUG: " + status + " - " + mess + " -- TAX CODE = " + currentTaxCode + " -- INDEX = " + currentIndex + " -- [RETRIED 3 TIMES]");
                                LOG.info("BUG: " + status + " - " + mess + " -- INDEX = " + currentIndex + " -- MST = " + currentTaxCode);
                                LOG.warn(currentTaxCode);
                                logInfo();

                                payload.setRunning(false);
                                fullResponse = Json.createObjectBuilder()
//                                            .add("status", "LOGIN_SUCCESS")
                                        .add("RespPayload", payload.toJson()) // Adding the payload JSON as a nested object
                                        .build();
                                SocketHandleTaxCode.sendMessageToAll(fullResponse.toString());

                                formattedDateTime = getDayTime();
                                String subject = "TOOL TAX INFO AN " + mess + " HAS OCCURRED";
                                String body = "ERROR INFORMATION: \n" +
                                        "\tCode: " + status + "\n" +
                                        "\tMessage: " + mess + "\n" +
                                        "\tDay: " + formattedDateTime + "\n" +
                                        "\tMST: " + currentTaxCode + "\n" +
                                        "\tIndex: " + currentIndex;

                                sendEmail(subject, body);
                                stopTool(null, true);
                            }
                        }
                    } catch (Exception e) {
                        logInfo();

                        payload.setRunning(false);
                        fullResponse = Json.createObjectBuilder()
                                .add("status", "[ERROR] Exception")
                                .add("message", e.getMessage())
                                .add("RespPayload", payload.toJson()) // Adding the payload JSON as a nested object
                                .build();
                        SocketHandleTaxCode.sendMessageToAll(fullResponse.toString());

//                        updateTaxCodeInfoResp(currentTaxCode, i, "ERROR: " + e);

                        formattedDateTime = getDayTime();
                        String subject = "TOOL TAX INFO AN ERROR OCCURRED";
                        String body = "ERROR INFORMATION: \n" +
                                "\tCode: NULL\n" +
                                "\tMessage: " + e.getMessage() + "\n" +
                                "\tDay: " + formattedDateTime + "\n" +
                                "\tMST: " + currentTaxCode + "\n" +
                                "\tIndex: " + currentIndex;

                        sendEmail(subject, body);
                        stopTool(e, true);
                    }
                } else {
                    sumNumberErrors++;
                    sumNumberDataResponseIsNull++;

                    payload.setRunning(false);
                    payload.setCurrentStatus("DATA RESPONSE IS NULL");
                    payload.setNumberErrors(sumNumberErrors);
                    payload.setNumberResponseIsNull(sumNumberDataResponseIsNull);

                    taxCodeInfoList.add(new TaxCodeInfo(currentTaxCode, i, "DATA RESPONSE IS NULL"));

                    LOG.info("BUG: DATA RESPONSE IS NULL" + " -- INDEX = " + currentIndex + " -- MST = " + currentTaxCode);
                    logInfo();

                    fullResponse = Json.createObjectBuilder()
                            .add("status", "Exception")
                            .add("message", "DATA RESPONSE IS NULL")
                            .add("RespPayload", payload.toJson()) // Adding the payload JSON as a nested object
                            .build();
                    SocketHandleTaxCode.sendMessageToAll(fullResponse.toString());

                    formattedDateTime = getDayTime();
                    String subject = "TOOL TAX INFO AN REQUEST TIME OUT HAS OCCURRED";
                    String body = "ERROR INFORMATION: \n" +
                            "Code: NULL\n" +
                            "Message: " + "DATA RESPONSE IS NULL" + "\n" +
                            "Day: " + formattedDateTime + "\n" +
                            "MST: " + currentTaxCode + "\n" +
                            "Index: " + currentIndex;

                    sendEmail(subject, body);
                    stopTool(null, true);
                }
            } catch (Exception e) {
                logInfo();

                payload.setRunning(false);
                fullResponse = Json.createObjectBuilder()
                        .add("status", "[ERROR] Exception")
                        .add("message", e.getMessage())
                        .add("RespPayload", payload.toJson()) // Adding the payload JSON as a nested object
                        .build();
                SocketHandleTaxCode.sendMessageToAll(fullResponse.toString());

//                updateTaxCodeInfoResp(currentTaxCode, i, "ERROR: " + e);

                formattedDateTime = getDayTime();
                String subject = "TOOL TAX INFO AN ERROR OCCURRED";
                String body = "ERROR INFORMATION: \n" +
                        "\tCode: NULL\n" +
                        "\tMessage: " + e.getMessage() + "\n" +
                        "\tDay: " + formattedDateTime + "\n" +
                        "\tMST: " + currentTaxCode + "\n" +
                        "\tIndex: " + currentIndex;

                sendEmail(subject, body);
                stopTool(e, true);
            }
        }

//        payload.setNumberTaxCode(sumNumberTaxCode);
//        payload.setNumberSuccessfully(sumNumberSuccessfully);
//        payload.setNumberErrors(sumNumberErrors);
//        payload.setNumberUnknownException(sumNumberUnknownException);
//        payload.setNumberCaptchaInvalid(sumNumberCaptchaInvalid);
//        payload.setNumberNotFound(sumNumberNotFound);
//        payload.setNumberResponseIsNull(sumNumberDataResponseIsNull);
//        payload.setNumberParameterIsInvalid(sumNumberParameterIsInvalid);

        logInfo();

        payload.setMessage("DONE INFORMATION OF A TAX CODE: " + currentTaxCode);
        fullResponse = Json.createObjectBuilder()
//                .add("status", "[ERROR] Exception")
                .add("RespPayload", payload.toJson()) // Adding the payload JSON as a nested object
                .build();
        SocketHandleTaxCode.sendMessageToAll(fullResponse.toString());

        if (i == (taxCodeList.size() - 1)) {
            posStartPage = currentIndex + 1;
        }
    }

    private static void updateTaxCodeInfoResp(String currentTaxCode, int currentIndex, String currentStatus) throws IOException {
        endDay = getDayTime();
        respPayload = new RespPayload();
        respPayload.setStartDay(startDay);
        respPayload.setEndDay(endDay);

        respPayload.setIndex(index);
        respPayload.setTaxCode(taxCode);
        respPayload.setConfig_aws(PATH_AWS_CONFIG);
        respPayload.setConfigSendEmail(PATH_SEND_EMAIL_CONFIG);
        respPayload.setPathFileTaxCodes(PATH);

        int size = taxCodeInfoList.size();
        if (size > 0) {
            respPayload.setPreviousTaxCode(taxCodeInfoList.get(size - 1).getTaxCode());
            respPayload.setPreviousStatus(taxCodeInfoList.get(size - 1).getStatus());
            respPayload.setPreviousIndex(taxCodeInfoList.get(size - 1).getIndex());
        }

        respPayload.setCurrentTaxCode(currentTaxCode);
        respPayload.setCurrentStatus(currentStatus);
        respPayload.setCurrentIndex(currentIndex);

        respPayload.setNumberTaxCode(sumNumberTaxCode);
        respPayload.setNumberSuccessfully(sumNumberSuccessfully);
        respPayload.setNumberErrors(sumNumberErrors);
        respPayload.setNumberUnknownException(sumNumberUnknownException);
        respPayload.setNumberCaptchaInvalid(sumNumberCaptchaInvalid);
        respPayload.setNumberNotFound(sumNumberNotFound);
        respPayload.setNumberResponseIsNull(sumNumberDataResponseIsNull);
        respPayload.setNumberParameterIsInvalid(sumNumberParameterIsInvalid);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(respPayload);

//        send to client
    }

    private static void logInfo() {
//        System.out.println();
        endDay = getDayTime();
        LOG.debug("startDay: " + startDay);
        LOG.debug("endDay: " + endDay);
        LOG.debug("sumNumberTaxCode: " + sumNumberTaxCode);
        LOG.debug("sumNumberSuccessfully: " + sumNumberSuccessfully);
        LOG.debug("sumNumberErrors: " + sumNumberErrors);
        LOG.debug("sumNumberNotFound: " + sumNumberNotFound);
        LOG.debug("sumNumberParameterIsInvalid: " + sumNumberParameterIsInvalid);
//        Khi tool xay ra 3 loi duoi thi tool dung lai luon
        LOG.debug("sumNumberCaptchaInvalid: " + sumNumberCaptchaInvalid);
        LOG.debug("sumNumberUnknownException: " + sumNumberUnknownException);
        LOG.debug("sumNumberDataResponseIsNull: " + sumNumberDataResponseIsNull);
    }

    private static void stopTool(Exception e, boolean isError) {
        if (isError) {
            if (e != null) {
                System.out.println();
                LOG.debug(e);
                System.out.println();
            }
        }

        System.exit(0);
    }

    private static void sendEmail(String subject, String body) {
        getSendEmailConfig(PATH_SEND_EMAIL_CONFIG); // Read file config to send email
        LOG.debug("TLSEmail Start");
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST); //SMTP Host
        props.put("mail.smtp.port", TLS_PORT); //TLS Port
        props.put("mail.smtp.auth", ENABLE_AUTHENTICATION); //enable authentication
        props.put("mail.smtp.starttls.enable", ENABLE_STARTTLS); //enable STARTTLS

        //create Authenticator object to pass in Session.getInstance argument
        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        };
        Session session = Session.getInstance(props, auth);

        String hostAndPort = getHostAndPort();
        String subName = " ";
        if (hostAndPort.contains("192.168.2.2"))
            subName = "DEV";
        else if (hostAndPort.contains("192.168.2.4")) {
            subName = "ISAPP";
        }

        endDay = getDayTime();
        String infoServer = "HOST NAME (POST): " + hostAndPort + "\n" + "SUB-NAME: " + subName;
        String infoTaxCodesRead = "\nstartDay: " + startDay +
                "\nendDay: " + endDay +
                "\nsumNumberTaxCode: " + sumNumberTaxCode +
                "\nsumNumberSuccessfully: " + sumNumberSuccessfully +
                "\nsumNumberErrors: " + sumNumberErrors +
                "\nsumNumberNotFound: " + sumNumberNotFound +
                "\nsumNumberParameterIsInvalid: " + sumNumberParameterIsInvalid +
                "\nsumNumberCaptchaInvalid: " + sumNumberCaptchaInvalid +
                "\nsumNumberUnknownException: " + sumNumberUnknownException +
                "\nsumNumberDataResponseIsNull: " + sumNumberDataResponseIsNull;

        body = infoServer + "\n" + body + "\n" + infoTaxCodesRead;

        Utils.sendEmail(session, TO_EMAIL,subject, body);
    }

    private static String getHostAndPort() {
        String url = Function.getURL();
        String[] parts = url.split("//");
        return parts[1].split("/")[0];
    }

    private static String getDayTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    private static void getSendEmailConfig(String pathConfig) {
        System.out.println();
        TreeMap<String, Object> map = Utils.readSendEmailConfig(pathConfig);
        if (map != null) {
            for (String key : map.keySet()) {
                switch (key) {
                    case "FROM_EMAIL":
                        FROM_EMAIL = String.valueOf(map.get(key));
                        break;

                    case "PASSWORD":
                        PASSWORD = String.valueOf(map.get(key));
                        break;

                    case "TO_EMAIL":
                        TO_EMAIL = String.valueOf(map.get(key));
                        break;

                    case "SMTP_HOST":
                        SMTP_HOST = String.valueOf(map.get(key));
                        break;

                    case "TLS_PORT":
                        TLS_PORT = String.valueOf(map.get(key));
                        break;

                    case "ENABLE_AUTHENTICATION":
                        ENABLE_AUTHENTICATION = String.valueOf(map.get(key));
                        break;

                    case "ENABLE_STARTTLS":
                        ENABLE_STARTTLS = String.valueOf(map.get(key));
                        break;
                }
            }

            if (FROM_EMAIL == null || FROM_EMAIL.isEmpty() || PASSWORD == null || PASSWORD.isEmpty() ||
                    TO_EMAIL == null || TO_EMAIL.isEmpty() || SMTP_HOST == null || SMTP_HOST.isEmpty() ||
                    TLS_PORT == null || TLS_PORT.isEmpty() || ENABLE_AUTHENTICATION == null || ENABLE_AUTHENTICATION.isEmpty() ||
                    ENABLE_STARTTLS == null || ENABLE_STARTTLS.isEmpty()) {
                LOG.debug("Invalid configuration parameter");
                System.exit(0);
            } else
                LOG.debug("Configuration send Email parameters loaded successfully");
        }
    }

    private static void waitJsonResp(String mst, int i) {
        int attempts = 0;
        while (jsonResp == null && attempts < MAX_RETRIES) {
            Utils.wait(TIME_WAIT);
            attempts++;

            // Check for timeout here (neu goi qua nhieu lan)
            if (attempts == MAX_RETRIES) {
                LOG.info("BUG: REQUEST TIME OUT" + " -- INDEX = " + (posStartPage + i) + " -- MST = " + mst);
                String formattedDateTime = getDayTime();

                String subject = "TOOL TAX INFO AN REQUEST TIME OUT HAS OCCURRED";
                String body = "ERROR INFORMATION: \n" +
                        "\tCode: \n" +
                        "\tMessage: " + "REQUEST TIME OUT" + "\n" +
                        "\tDay: " + formattedDateTime + "\n" +
                        "\tMST: " + mst + "\n" +
                        "\tIndex: " + (posStartPage + i);

                sendEmail(subject, body);
                System.exit(0);
            }
        }
    }
}
