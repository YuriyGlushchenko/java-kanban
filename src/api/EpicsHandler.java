package api;

import com.sun.net.httpserver.HttpExchange;
import exeptions.TimeIntersectionException;
import model.Epic;
import model.SubTask;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler {


    public EpicsHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        EndpointData endpointData = getEndpoint("epics", exchange.getRequestURI().getPath(), exchange.getRequestMethod());
        System.out.println("\nзапрос на: " + exchange.getRequestURI().getPath() + ", метод: " + exchange.getRequestMethod());
        System.out.println("Endpoint: " + endpointData.endpoint());
        switch (endpointData.endpoint()) {
            case GET_EPIC_BY_ID -> handleGetEpicById(exchange, endpointData.idOptional().orElse(-999));
            case GET_EPICS -> handleGetAllEpics(exchange);
            case CREATE_EPIC -> handleAddEpic(exchange);
            case UPDATE_EPIC -> handleUpdateEpic(exchange, endpointData.idOptional().orElse(-999));
            case DELETE_EPIC -> handleDeleteEpic(exchange, endpointData.idOptional().orElse(-999));
            case GET_EPIC_SUBTASKS -> handleGetEpicSubTasks(exchange, endpointData.idOptional().orElse(-999));
            case UNKNOWN -> sendUnknownEndpoint(exchange);
        }

    }

    private void handleGetEpicById(HttpExchange exchange, int id) throws IOException {
        Optional<Epic> epicOptional = manager.getEpicById(id);
        if (epicOptional.isPresent()) {
            String epicJSON = gson.toJson(epicOptional.get());
            sendText(exchange, epicJSON, 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleAddEpic(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        String bodyJson = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        Epic epic = gson.fromJson(bodyJson, Epic.class);

        try {
            manager.addNewEpic(epic);
            sendText(exchange, "", 201);
        } catch (TimeIntersectionException e) {
            sendHasOverlaps(exchange);
        }
    }

    private void handleGetAllEpics(HttpExchange exchange) throws IOException {
        List<Epic> epicList = manager.getAllEpics();
        String epicListJson = gson.toJson(epicList);
        sendText(exchange, epicListJson, 200);
    }

    private void handleUpdateEpic(HttpExchange exchange, int id) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        String bodyJson = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        Epic epic = gson.fromJson(bodyJson, Epic.class);

        try {
            manager.updateEpic(epic);
            sendText(exchange, "", 200);
        } catch (TimeIntersectionException e) {
            sendHasOverlaps(exchange);
        }
    }

    private void handleDeleteEpic(HttpExchange exchange, int id) throws IOException {
        manager.deleteEpic(id);
        sendText(exchange, "", 200);
    }

    private void handleGetEpicSubTasks(HttpExchange exchange, int id) throws IOException {
        try {
            List<SubTask> subTaskList = manager.getEpicSubTasks(id);
            String epicListJson = gson.toJson(subTaskList);
            sendText(exchange, epicListJson, 200);
        } catch (NoSuchElementException e) {
            sendNotFound(exchange);
        }
    }
}
