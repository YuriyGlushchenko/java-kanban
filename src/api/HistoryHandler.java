package api;

import com.sun.net.httpserver.HttpExchange;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestURI().getPath().equals("/history") && exchange.getRequestMethod().equals("GET")) {
            List<Task> history = manager.getHistory();
            String historyJson = gson.toJson(history);
            sendText(exchange, historyJson, 200);
        } else {
            sendUnknownEndpoint(exchange);
        }
    }
}
