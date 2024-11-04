package org.example.tool_tax_code;

import org.apache.log4j.Logger;
import org.example.socket.SocketHandleTaxCode;
import sun.rmi.runtime.Log;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ToolManager {
    private static final Logger LOG = Logger.getLogger(ToolManager.class);
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static Future<?> informationRetrievalTask;
    public static volatile boolean isRunning = false;

    public static void startTool(String[] args) {
        if (!isRunning) {
            isRunning = true;
            informationRetrievalTask = executorService.submit(() -> {
                try {
                    Program.running(args);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    public static void stopTool() {
        if (informationRetrievalTask != null) {
            informationRetrievalTask.cancel(true);
            isRunning = false;

//        Tạo phản hồi JSON
            JsonObject jsonResponse = Json.createObjectBuilder()
                    .add("isRunning", isRunning)
//                    .add("status", "[STOP]")
                    .add("message", "TOOL HAS STOPPED")
                    .build();
            SocketHandleTaxCode.sendMessageToAll(jsonResponse.toString());
        }
//        executorService.shutdownNow();

    }


}
