package API;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public PrioritizedHandler(HttpTaskServer httpTaskServer) {
        this.manager = httpTaskServer.getManager();
        this.gson = httpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestURI().getPath().equals("/prioritized") && exchange.getRequestMethod().equals("GET")) {
            List<Task> prioritized = manager.getPrioritizedTasks();
            String prioritizedJson = gson.toJson(prioritized);
            sendSuccessfullyDone(exchange, prioritizedJson, 200);
        } else {
            sendUnknownEndpoint(exchange);
        }

    }
}
