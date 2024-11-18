package org.example.socket;

import org.apache.log4j.Logger;
import org.example.tool_tax_code.Program;
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
    public static String lastMessage = "";

    @OnOpen
    public void onOpen(Session session) {
        LOG.debug("[OPEN] Client connected: " + session.getId());
        clients.put(session.getId(), session);
        if (lastMessage != null && !lastMessage.isEmpty())
            sendMessageToClient(session.getId(), lastMessage);

//        if (ToolManager.program != null && ToolManager.isRunning) {
//            if (lastMessage != null && !lastMessage.isEmpty())
//                sendMessageToClient(session.getId(), lastMessage);
//        }
//        else {
//            JsonObject jsonResponse = Json.createObjectBuilder()
//                    .add("isRunning", false)
////                    .add("status", "[STOP]")
//                    .add("message", "TOOL HAS STOPPED")
//                    .build();
//            sendMessageToClient(session.getId(), jsonResponse.toString());
//        }
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

        String[] args;

        if (index != null && !index.equalsIgnoreCase("")) {
            args = new String[] { configAws, configSendEmail, pathFileTaxCodes, index };
        } else if (taxCode != null && !taxCode.equalsIgnoreCase("")) {
            args = new String[] { configAws, configSendEmail, taxCode, pathFileTaxCodes };
        } else {
            args = new String[] { configAws, configSendEmail, pathFileTaxCodes };
        }

        LOG.debug("action: " + action);

        if (action != null && action.equals("start")) {
            ToolManager.startTool(args);
        } else if (action != null && action.equals("stop")) {
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
        lastMessage = message;
        clients.values().forEach(session -> {
            LOG.debug("[SEND TO ALL] Client ID:" + session.getId());
            try {
                session.getBasicRemote().sendText(message);
                LOG.debug("[SERVER SENT] SERVER SENT MESSAGE TO CLIENT " + session.getId() + ": " + message);
            } catch (IOException e) {
                LOG.debug("[ERROR] [SERVER SENT] SERVER SENT MESSAGE TO CLIENT " + session.getId() + ": " + e);
                throw new RuntimeException(e);
            }
        });
    }

//    Phuong thuc gui tin nhan den mot client cu the du tren sessionID
    public static void sendMessageToClient(String sessionId, String message) {
        lastMessage = message;
        Session session = clients.get(sessionId);
        if (session != null) {
            try {
                session.getBasicRemote().sendText(message);
                LOG.debug("[SERVER SENT] SERVER SENT MESSAGE TO CLIENT " + sessionId + ": " + message);
            } catch (IOException e) {
                LOG.debug("[ERROR] [SERVER SENT] SERVER SENT MESSAGE TO CLIENT " + sessionId + ": " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
}
