package org.example.sample_aws_webapplication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.example.tool_tax_code.Program;
import org.example.payload.ReqPayload;
import org.example.payload.RespPayload;
import org.example.tool_tax_code.TaxIdentityCommon;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//@WebServlet(name = "TaxCodeServlet", value = "/run-service")
public class TaxCodeServlet_Backup extends HttpServlet {
    private ExecutorService executorService;
    private Future<?> informationRetrievalTask;
    private volatile boolean running;

    private RespPayload respPayload;
    private Logger LOG = Logger.getLogger(TaxCodeServlet_Backup.class);
    private String message;

    public void init() throws ServletException {
        super.init();
        executorService = Executors.newFixedThreadPool(1);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        request.getRequestDispatcher("index.html").forward(request, response);

        // Đặt kiểu phản hồi là JSON
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Đọc dữ liệu JSON từ request body
        StringBuilder jsonString = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
        }

        // Phân tích chuỗi JSON
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonString.toString()))) {
            JsonObject jsonObject = jsonReader.readObject();

            // Lấy các trường từ JSON
            String action = jsonObject.getString("action");
            String index = jsonObject.getString("index");
            String taxCode = jsonObject.getString("taxCode");
            String configAws = jsonObject.getString("config_aws");
            String configSendEmail = jsonObject.getString("config_send_email");
            String pathFileTaxCodes = jsonObject.getString("path_file_tax_codes");

//            Xử lý dữ liệu ở đây
            System.out.println("action: " + action);
            System.out.println("index: " + index);
            System.out.println("taxCode: " + taxCode);
            System.out.println("configAws: " + configAws);
            System.out.println("configSendEmail: " + configSendEmail);
            System.out.println("pathFileTaxCodes: " + pathFileTaxCodes);

            // Tạo phản hồi JSON
            JsonObject jsonResponse = Json.createObjectBuilder()
                    .add("status", "success")
                    .add("message", "Processed tax code successfully")
                    .build();

            out.print(jsonResponse.toString());
            out.flush();
        } catch (Exception e) {
            JsonObject errorResponse = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Failed to process the request: " + e.getMessage())
                    .build();

            out.print(errorResponse.toString());
            out.flush();
        }

//        try {
//            Program.running(new String[] {"D:\\Data\\MOBILE_ID\\DTIS-V2-Utility\\Sample_AWS_intelij\\Sample_AWS_WebApplication\\src\\main\\resources\\config_aws_dev.cfg",
//                    "D:\\Data\\MOBILE_ID\\DTIS-V2-Utility\\Sample_AWS_intelij\\Sample_AWS_WebApplication\\src\\main\\resources\\config_send_email.cfg",
//                    "D:\\Data\\MOBILE_ID\\DTIS-V2-Utility\\Sample_AWS_intelij\\Sample_AWS_WebApplication\\src\\main\\resources\\Tax_Code_Test.txt"}, request, response);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        StringBuilder payload = new StringBuilder();
        try(BufferedReader reader = req.getReader()){
            String line;
            while ((line = reader.readLine()) != null){
                payload.append(line);
            }
        }

        System.out.println("taxCodeInfoResp: " + payload);

        ObjectMapper objectMapper = new ObjectMapper();
        ReqPayload reqPayload = objectMapper.readValue(payload.toString(), ReqPayload.class);

        String action = reqPayload.getAction();
        String index = reqPayload.getIndex();
        String taxCode = reqPayload.getTaxCode();
        String config_aws = reqPayload.getConfig_aws();
        String configSendEmail = reqPayload.getConfig_send_email();
        String pathFileTaxCodes = reqPayload.getPath_file_tax_codes();

        System.out.println("config_aws: " + config_aws);
        System.out.println("configSendEmail: " + configSendEmail);
        System.out.println("pathFileTaxCodes: " + pathFileTaxCodes);
        System.out.println("action: " + action);
        System.out.println("index: " + index);
        System.out.println("taxCode: " + taxCode);

        respPayload = new RespPayload();
        respPayload.setIndex(Integer.parseInt(index != null ? index : "-1"));
        respPayload.setTaxCode(taxCode);
        respPayload.setConfig_aws(config_aws);
        respPayload.setConfigSendEmail(configSendEmail);
        respPayload.setPathFileTaxCodes(pathFileTaxCodes);

        if (action.equalsIgnoreCase("start")) {
            if (!running) {
                // Khoi dong qua trinh truy xuat thong tin tax code
                informationRetrievalTask = executorService.submit(() -> {
                    running = true;
                    try {
                        String[] args = new String[4];
                        args[0] = config_aws;
                        args[1] = configSendEmail;
                        if (index != null && !index.equalsIgnoreCase("")) {
                            args[2] = pathFileTaxCodes;
                            args[3] = index;
                        } else if (taxCode != null && !taxCode.equalsIgnoreCase("")) {
                            args[2] = taxCode;
                            args[3] = pathFileTaxCodes;
                        } else if (pathFileTaxCodes != null && !pathFileTaxCodes.equalsIgnoreCase("")){
                            args[2] = pathFileTaxCodes;
                        } else {
                            LOG.debug("ERROR: PARAMETER INVALID");
                            respPayload.setMessage("PARAMETER INVALID");
                            sendResp(req, resp);
                            return;
                        }

                        LOG.debug("RUNNING...START");
                        Program.running(args);
                    } catch (Exception e) {
                        running = false;
                        respPayload.setRunning(running);
                        respPayload.setMessage(e.toString());
                        LOG.debug("ERROR: ", e);

                        sendResp(req, resp);
                    } finally {
                        LOG.debug("FINISHED...STOP");
                        running = false;
                        respPayload.setRunning(running);
                        respPayload.setMessage("SUCCESSFULLY ALL TAX CODES");
                        sendResp(req, resp);
                    }

                });
            }
        } else if (action.equalsIgnoreCase("stop")) {
            if (informationRetrievalTask != null && !informationRetrievalTask.isDone()) {
                informationRetrievalTask.cancel(true);
                running = false;
            }
        }
    }

    public void destroy() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    private void sendResp(HttpServletRequest req, HttpServletResponse resp) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = "";
        try {
            jsonResponse = objectMapper.writeValueAsString(respPayload);
        } catch (JsonProcessingException e) {
            LOG.debug("ERROR: " + e);
        }

        if (resp != null) {
            // Đặt kiểu dữ liệu trả về là JSON
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            try {
                resp.getWriter().write(jsonResponse);
            } catch (IOException e) {
                LOG.debug("ERROR: " + e);
            }
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html;charset=UTF-8");

        String urlLogin="https://192.168.2.2:11443/dtis/v2/e-identity/general/token/get";
        String basicToken="Basic TU9CSUxFLUlEX0RFVjotZGRmTG9obDQ0RTdXREFZbDF6S2JFaTRaampBLVhaZEhvcEJpcE14";
        int timeout = 120000;
        String accessKey="RCJBAKFHQENC91VXHF8C";
        String secretKey="-ddfLohl44E7WDAYl1zKbEi4ZjjA-XZdHopBipMx";
        String region="vn-south-1";
        String serviceName="dtis-20.10.05";
        String xApiKey="G91W_4tkizGha1pBmSs6YCws4jABAmRUxgtkHB_q";



        TaxIdentityCommon func = new TaxIdentityCommon();
        String jsonResp;

        try {
            jsonResp = func.loginIdentity(urlLogin, "GET", basicToken, timeout, accessKey, secretKey, region, serviceName, xApiKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("A: " + jsonResp);
    }
}