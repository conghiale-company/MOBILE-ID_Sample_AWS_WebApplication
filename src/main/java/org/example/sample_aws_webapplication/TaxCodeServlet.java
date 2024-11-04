package org.example.sample_aws_webapplication;

import org.apache.log4j.Logger;
import org.example.socket.SocketHandleTaxCode;
import org.example.payload.RespPayload;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet(name = "TaxCodeServlet", value = "")
public class TaxCodeServlet extends HttpServlet {

    private Logger LOG = Logger.getLogger(TaxCodeServlet.class);

    public void init() throws ServletException {
        super.init();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOG.debug("GET REQUEST...");

        request.getRequestDispatcher("index.html").forward(request, response);

//        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//        Đat kieu phan hoi là JSON
        LOG.debug("POST REQUEST...");

//        processRequest(req, resp);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
//        Doc du lieu JSON tu request body
        StringBuilder jsonString = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        Phan tich chuoi JSON
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
            LOG.debug("action: " + action);
            LOG.debug("index: " + index);
            LOG.debug("taxCode: " + taxCode);
            LOG.debug("configAws: " + configAws);
            LOG.debug("configSendEmail: " + configSendEmail);
            LOG.debug("pathFileTaxCodes: " + pathFileTaxCodes);

            // Tạo phản hồi JSON
            JsonObject jsonResponse = Json.createObjectBuilder()
                    .add("status", "success")
                    .add("message", "Processed tax code successfully")
                    .build();

//            Gui du lieu den tat ca cac client dang ket noi qua WebSocket
            SocketHandleTaxCode.sendMessageToAll(jsonResponse.toString());

        } catch (Exception e) {
            JsonObject errorResponse = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Failed to process the request: " + e.getMessage())
                    .build();

//            Gui du lieu den tat ca cac client dang ket noi qua WebSocket
            SocketHandleTaxCode.sendMessageToAll(errorResponse.toString());

        }
    }
}