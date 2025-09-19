package api;

import com.sun.net.httpserver.HttpExchange;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestURI().getPath().equals("/prioritized") && exchange.getRequestMethod().equals("GET")) {
            List<Task> prioritized = manager.getPrioritizedTasks();
            String prioritizedJson = gson.toJson(prioritized);
            sendText(exchange, prioritizedJson, 200);
        } else {
            sendUnknownEndpoint(exchange);
        }

    }
}
