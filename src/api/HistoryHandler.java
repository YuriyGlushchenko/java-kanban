package api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public HistoryHandler(HttpTaskServer httpTaskServer) {
        this.manager = httpTaskServer.getManager();
        this.gson = httpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestURI().getPath().equals("/history") && exchange.getRequestMethod().equals("GET")) {
            List<Task> history = manager.getHistory();
            String historyJson = gson.toJson(history);
            sendSuccessfullyDone(exchange, historyJson, 200);
        } else {
            sendUnknownEndpoint(exchange);
        }
    }
}
