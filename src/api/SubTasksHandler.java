package api;

import com.sun.net.httpserver.HttpExchange;
import exeptions.ManagerSaveException;
import exeptions.TimeIntersectionException;
import model.SubTask;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class SubTasksHandler extends BaseHttpHandler {

    public SubTasksHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        EndpointData endpointData = getEndpoint("subtasks", exchange.getRequestURI().getPath(), exchange.getRequestMethod());
        System.out.println("\nзапрос на: " + exchange.getRequestURI().getPath() + ", метод: " + exchange.getRequestMethod());
        System.out.println("Endpoint: " + endpointData.endpoint());
        switch (endpointData.endpoint()) {
            case GET_SUBTASK_BY_ID -> handleGetSubTaskById(exchange, endpointData.idOptional().orElse(-999));
            case GET_SUBTASKS -> handleGetAllSubTasks(exchange);
            case CREATE_SUBTASK -> handleAddSubTask(exchange);
            case UPDATE_SUBTASK -> handleUpdateSubTask(exchange, endpointData.idOptional().orElse(-999));
            case DELETE_SUBTASK -> handleDeleteSubTask(exchange, endpointData.idOptional().orElse(-999));
            case UNKNOWN -> sendUnknownEndpoint(exchange);
        }

    }

    private void handleGetSubTaskById(HttpExchange exchange, int id) throws IOException {
        Optional<SubTask> subTaskOptional = manager.getSubTaskById(id);
        if (subTaskOptional.isPresent()) {
            String subTaskJSON = gson.toJson(subTaskOptional.get());
            sendText(exchange, subTaskJSON, 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleAddSubTask(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        String bodyJson = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        SubTask subTask = gson.fromJson(bodyJson, SubTask.class);

        try {
            int id = manager.addNewSubTask(subTask);
            sendText(exchange, "", 201);
        } catch (TimeIntersectionException e) {
            sendHasOverlaps(exchange);
        } catch (ManagerSaveException e) {
            sendText(exchange, "Возможно, не верно указан id эпика", 406);
        }
    }

    private void handleGetAllSubTasks(HttpExchange exchange) throws IOException {
        List<SubTask> subTaskList = manager.getAllSubTasks();
        String subTaskListJson = gson.toJson(subTaskList);
        sendText(exchange, subTaskListJson, 200);
    }

    private void handleUpdateSubTask(HttpExchange exchange, int id) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        String bodyJson = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        SubTask subTask = gson.fromJson(bodyJson, SubTask.class);

        try {
            manager.updateSubTask(subTask);
            sendText(exchange, "", 200);
        } catch (TimeIntersectionException e) {
            sendHasOverlaps(exchange);
        } catch (ManagerSaveException e) {
            sendText(exchange, "Возможно, не верно указан id эпика", 406);
        }
    }

    private void handleDeleteSubTask(HttpExchange exchange, int id) throws IOException {
        manager.deleteSubTask(id);
        sendText(exchange, "", 200);
    }
}
