package org.example.sample_aws_webapplication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.example.tool_tax_code.Program;
import org.example.tool_tax_code.RequestPayload;
import org.example.tool_tax_code.TaxCodeInfoResp;
import org.example.tool_tax_code.TaxIdentityCommon;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet(name = "TaxCodeServlet", value = "/task")
public class TaxCodeServlet extends HttpServlet {
    private ExecutorService executorService;
    private Future<?> informationRetrievalTask;
    private volatile boolean running;

    private TaxCodeInfoResp taxCodeInfoResp;
    private Logger LOG = Logger.getLogger(TaxCodeServlet.class);
    private String message;

    public void init() throws ServletException {
        super.init();
        executorService = Executors.newFixedThreadPool(1);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
//        request.getRequestDispatcher("index.html").forward(request, response);

        String urlLogin1="https://id-dev.mobile-id.vn/dtis/v2/e-identity/general/token/get";
        String basicToken1="Basic TU9CSUxFLUlEX0RFVjotZGRmTG9obDQ0RTdXREFZbDF6S2JFaTRaampBLVhaZEhvcEJpcE14";
        int timeout1 = 120000;
        String accessKey1="RCJBAKFHQENC91VXHF8C";
        String secretKey1="-ddfLohl44E7WDAYl1zKbEi4ZjjA-XZdHopBipMx";
        String region1="vn-south-1";
        String serviceName1="dtis-20.10.05";
        String xApiKey1="G91W_4tkizGha1pBmSs6YCws4jABAmRUxgtkHB_q";

        TaxIdentityCommon func = new TaxIdentityCommon();
        String jsonResp;
        try {
            jsonResp = func.loginIdentity(urlLogin1, "GET", basicToken1, timeout1, accessKey1, secretKey1, region1, serviceName1, xApiKey1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("A: " + jsonResp);

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
        RequestPayload requestPayload = objectMapper.readValue(payload.toString(), RequestPayload.class);

        String action = requestPayload.getAction();
        String index = requestPayload.getIndex();
        String taxCode = requestPayload.getTaxCode();
        String config_aws = requestPayload.getConfig_aws();
        String configSendEmail = requestPayload.getConfig_send_email();
        String pathFileTaxCodes = requestPayload.getPath_file_tax_code();

        taxCodeInfoResp = new TaxCodeInfoResp();
        taxCodeInfoResp.setIndex(Integer.parseInt(index != null ? index : "-1"));
        taxCodeInfoResp.setTaxCode(taxCode);
        taxCodeInfoResp.setConfig_aws(config_aws);
        taxCodeInfoResp.setConfigSendEmail(configSendEmail);
        taxCodeInfoResp.setPathFileTaxCodes(pathFileTaxCodes);

        System.out.println("config_aws: " + config_aws);
        System.out.println("configSendEmail: " + configSendEmail);
        System.out.println("configSendEmail: " + configSendEmail);
        System.out.println("action: " + action);

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
                            taxCodeInfoResp.setMessage("PARAMETER INVALID");
                            sendResp(req, resp);
                            return;
                        }

                        LOG.debug("RUNNING...START");
                        Program.running(args, req, resp);
                    } catch (Exception e) {
                        running = false;
                        taxCodeInfoResp.setRunning(running);
                        taxCodeInfoResp.setMessage(e.toString());
                        LOG.debug("ERROR: ", e);

                        sendResp(req, resp);
                    } finally {
                        LOG.debug("FINISHED...STOP");
                        running = false;
                        taxCodeInfoResp.setRunning(running);
                        taxCodeInfoResp.setMessage("SUCCESSFULLY ALL TAX CODES");
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
            jsonResponse = objectMapper.writeValueAsString(taxCodeInfoResp);
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
}