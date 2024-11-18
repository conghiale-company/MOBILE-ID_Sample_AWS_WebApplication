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
    private static ExecutorService executorService = null;
    private static Future<?> informationRetrievalTask;
    public static volatile boolean isRunning = false;

    public static Program program = null;

    public static void startTool(String[] args) {
        LOG.debug("Starting tool...");
        if (!isRunning) {
            isRunning = true;

            // Reinitialize executorService if it was shut down
            if (executorService == null || executorService.isShutdown()) {
                executorService = Executors.newSingleThreadExecutor();
            }

            informationRetrievalTask = executorService.submit(() -> {
                try {
                    program = new Program();
                    program.running(args);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    public static void stopTool() {
        if (informationRetrievalTask != null) {
            isRunning = false;
            LOG.debug("stopping information retrieval");

//        Tạo phản hồi JSON
            JsonObject jsonResponse = Json.createObjectBuilder()
                    .add("isRunning", isRunning)
//                    .add("status", "[STOP]")
                    .add("message", "TOOL HAS STOPPED")
                    .build();
            SocketHandleTaxCode.sendMessageToAll(jsonResponse.toString());

            informationRetrievalTask.cancel(false); // -> try change to false

            if (program != null) {
                program = null; // Hủy đối tượng Program
            }
        }
        executorService.shutdownNow();
        executorService = null;
        LOG.debug("tool stopped: executorService = " + executorService);
    }

    public static void stopTool(JsonObject jsonResponse) {
        if (informationRetrievalTask != null) {
            isRunning = false;
            LOG.debug("stopping information retrieval 01");
            SocketHandleTaxCode.sendMessageToAll(jsonResponse.toString());

            informationRetrievalTask.cancel(false); // -> try change to false
        }
        executorService.shutdownNow();
        executorService = null;

        if (program != null) {
            program = null; // Hủy đối tượng Program
        }
        LOG.debug("tool stopped 01: executorService = " + executorService);
    }
}
