package org.example.socket;

import org.apache.log4j.Logger;
import org.example.tool_tax_code.ToolManager;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

// su dung javaee-api
@ServerEndpoint("/socket-handle-tax-code")
public class SocketHandleTaxCode {
    private static final Logger LOG = Logger.getLogger(SocketHandleTaxCode.class);
    private static final ConcurrentHashMap<String, Session> clients = new ConcurrentHashMap<>();
    private static String lastMessage = "";

    @OnOpen
    public void onOpen(Session session) {
        LOG.debug("[OPEN] Client connected: " + session.getId());
        clients.put(session.getId(), session);
        if (lastMessage != null && !lastMessage.isEmpty())
            sendMessageToClient(session.getId(), lastMessage);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        LOG.debug("[MESSAGE] Message received from client: " + message);

//        Chuyen message ve da  ng json => lay thong tin duoc gui len
        JsonReader jsonReader = Json.createReader(new StringReader(message));
        JsonObject jsonObject = jsonReader.readObject();

//        Retrieve fields with checks for each key
        String action = jsonObject.containsKey("action") ? jsonObject.getString("action") : null;
        String index = jsonObject.containsKey("index") ? jsonObject.getString("index") : null;
        String taxCode = jsonObject.containsKey("taxCode") ? jsonObject.getString("taxCode") : null;
        String configAws = jsonObject.containsKey("config_aws") ? jsonObject.getString("config_aws") : null;
        String configSendEmail = jsonObject.containsKey("config_send_email") ? jsonObject.getString("config_send_email") : null;
        String pathFileTaxCodes = jsonObject.containsKey("path_file_tax_codes") ? jsonObject.getString("path_file_tax_codes") : null;

//        Logging each field
//        LOG.debug("action: " + action);
//        LOG.debug("index: " + index);
//        LOG.debug("taxCode: " + taxCode);
//        LOG.debug("configAws: " + configAws);
//        LOG.debug("configSendEmail: " + configSendEmail);
//        LOG.debug("pathFileTaxCodes: " + pathFileTaxCodes);

        String[] args;

        if (index != null && !index.equalsIgnoreCase("")) {
            args = new String[] { configAws, configSendEmail, pathFileTaxCodes, index };
        } else if (taxCode != null && !taxCode.equalsIgnoreCase("")) {
            args = new String[] { configAws, configSendEmail, taxCode, pathFileTaxCodes };
        } else {
            args = new String[] { configAws, configSendEmail, pathFileTaxCodes };
        }

        if (action.equals("start")) {
//            LOG.debug("[START] Client connected: " + session.getId());
            ToolManager.startTool(args);
        } else if (action.equals("stop")) {
//            LOG.debug("action: " + action);
            ToolManager.stopTool();
        }

//        String response = "Processed message: " + message;
//        session.getBasicRemote().sendText(response);
    }

    @OnClose
    public void onClose(Session session) {
        LOG.debug("[CLOSE] Client disconnected: " + session.getId());
        clients.remove(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        LOG.debug("[ERROR] Client ID:" + session.getId());
        LOG.debug("[ERROR MESSAGE] error: " + error.getMessage());
    }

//    Phuong thuc gui tin nhan den tat ca client
    public static void sendMessageToAll(String message) {
//        LOG.debug("clients: " + clients.values().size());
        clients.values().forEach(session -> {
            LOG.debug("[SEND TO ALL] Client ID:" + session.getId());
            try {
                session.getBasicRemote().sendText(message);
                lastMessage = message;
                LOG.debug("[SERVER SENT] SERVER SENT MESSAGE TO CLIENT " + session.getId() + ": " + message);
            } catch (IOException e) {
                LOG.debug("[ERROR] [SERVER SENT] SERVER SENT MESSAGE TO CLIENT " + session.getId() + ": " + e);
                throw new RuntimeException(e);
            }
        });
    }

//    Phuong thuc gui tin nhan den mot client cu the du tren sessionID
    public static void sendMessageToClient(String sessionId, String message) {
        Session session = clients.get(sessionId);
        if (session != null) {
            try {
                session.getBasicRemote().sendText(message);
                lastMessage = message;
                LOG.debug("[SERVER SENT] SERVER SENT MESSAGE TO CLIENT " + sessionId + ": " + message);
            } catch (IOException e) {
                LOG.debug("[ERROR] [SERVER SENT] SERVER SENT MESSAGE TO CLIENT " + sessionId + ": " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
}
