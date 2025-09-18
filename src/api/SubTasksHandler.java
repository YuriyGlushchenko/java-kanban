package api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exeptions.ManagerSaveException;
import exeptions.TimeIntersectionException;
import model.SubTask;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class SubTasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public SubTasksHandler(HttpTaskServer httpTaskServer) {
        this.manager = httpTaskServer.getManager();
        this.gson = httpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        EndpointData endpointData = getEndpoint("subtasks", exchange.getRequestURI().getPath(), exchange.getRequestMethod());
        System.out.println("Обработчик SubTasks");
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
            sendSuccessfullyDone(exchange, subTaskJSON, 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleAddSubTask(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        String bodyJson = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        SubTask subTask = gson.fromJson(bodyJson, SubTask.class);
//        if(subTask.getParentEpicId() == 0 || manager.getEpicById(subTask.getParentEpicId()).isEmpty()){
//            String text = "{" +
//                    "\"message\":\"Задача не добавлена. Возможно, не верно указан parentEpicId\"," +
//                    "\"statusCode\":\"406\"," +
//                    "\"success\":\"false\"" +
//                    "}";
//            sendText(exchange, text,406);
//        }

        try {
            int id = manager.addNewSubTask(subTask);
            Optional<SubTask> loadedSubTaskOptional = manager.getSubTaskById(id);
            if (loadedSubTaskOptional.isPresent()) {
                String loadedSubTaskJson = gson.toJson(loadedSubTaskOptional.get());
                sendSuccessfullyDone(exchange, loadedSubTaskJson, 201);
            }
        } catch (TimeIntersectionException e) {
            sendHasOverlaps(exchange);
        } catch (ManagerSaveException e) {
            String text = "{" +
                    "\"message\":\"" + e.getMessage() + ". Задача не добавлена. Возможно, не верно указан parentEpicId\"," +
                    "\"statusCode\":\"406\"," +
                    "\"success\":\"false\"" +
                    "}";
            sendText(exchange, text, 406);
        }
    }

    private void handleGetAllSubTasks(HttpExchange exchange) throws IOException {
        List<SubTask> subTaskList = manager.getAllSubTasks();
        String subTaskListJson = gson.toJson(subTaskList);
        sendSuccessfullyDone(exchange, subTaskListJson, 200);
    }

    private void handleUpdateSubTask(HttpExchange exchange, int id) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        String bodyJson = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        SubTask subTask = gson.fromJson(bodyJson, SubTask.class);

        try {
            manager.updateSubTask(subTask);
            Optional<SubTask> loadedSubTaskOptional = manager.getSubTaskById(id);
            if (loadedSubTaskOptional.isPresent()) {
                String loadedSubTaskJson = gson.toJson(loadedSubTaskOptional.get());
                sendSuccessfullyDone(exchange, loadedSubTaskJson, 200);
            }
        } catch (TimeIntersectionException e) {

            sendHasOverlaps(exchange);
        } catch (ManagerSaveException e) {
            String text = "{" +
                    "\"message\":\"" + e.getMessage() + ". Задача не обновлена. Возможно, не верно указан parentEpicId\"," +
                    "\"statusCode\":\"406\"," +
                    "\"success\":\"false\"" +
                    "}";
            sendText(exchange, text, 406);
        }
    }

    private void handleDeleteSubTask(HttpExchange exchange, int id) throws IOException {
        manager.deleteSubTask(id);
        String text = "{" +
                "\"message\":\"Задача удалена\"," +
                "\"statusCode\":\"200\"," +
                "\"success\":\"true\"" +
                "}";
        sendSuccessfullyDone(exchange, text, 200);
    }
}
