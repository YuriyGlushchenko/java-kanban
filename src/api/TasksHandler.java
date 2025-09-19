package api;

import com.sun.net.httpserver.HttpExchange;
import exeptions.TimeIntersectionException;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        EndpointData endpointData = getEndpoint("tasks", exchange.getRequestURI().getPath(), exchange.getRequestMethod());
        System.out.println("\nзапрос на: " + exchange.getRequestURI().getPath() + ", метод: " + exchange.getRequestMethod());
        System.out.println("Endpoint: " + endpointData.endpoint());
        switch (endpointData.endpoint()) {
            case GET_TASK_BY_ID -> handleGetTaskById(exchange, endpointData.idOptional().orElse(-999));
            case GET_TASKS -> handleGetAllTasks(exchange);
            case CREATE_TASK -> handleAddTask(exchange);
            case UPDATE_TASK -> handleUpdateTask(exchange, endpointData.idOptional().orElse(-999));
            case DELETE_TASK -> handleDeleteTask(exchange, endpointData.idOptional().orElse(-999));
            case UNKNOWN -> sendUnknownEndpoint(exchange);
        }

    }

    private void handleGetTaskById(HttpExchange exchange, int id) throws IOException {
        Optional<Task> taskOptional = manager.getTaskById(id);
        if (taskOptional.isPresent()) {
            String taskJSON = gson.toJson(taskOptional.get());
            sendText(exchange, taskJSON, 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleAddTask(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        String bodyJson = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        Task task = gson.fromJson(bodyJson, Task.class);

        try {
            int id = manager.addNewTask(task);
            sendText(exchange, "", 201);
        } catch (TimeIntersectionException e) {
            sendHasOverlaps(exchange);
        }
    }

    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        List<Task> taskList = manager.getAllTasks();
        String taskListJson = gson.toJson(taskList);
        sendText(exchange, taskListJson, 200);
    }

    private void handleUpdateTask(HttpExchange exchange, int id) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        String bodyJson = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        Task task = gson.fromJson(bodyJson, Task.class);

        try {
            manager.updateTask(task);
            sendText(exchange, "", 200);
        } catch (TimeIntersectionException e) {
            sendHasOverlaps(exchange);
        }
    }

    private void handleDeleteTask(HttpExchange exchange, int id) throws IOException {
        manager.deleteTask(id);
        sendText(exchange, "", 200);
    }

}

