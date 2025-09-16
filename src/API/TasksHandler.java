package API;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;

    public TasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        EndpointData endpointData = getEndpoint("tasks", exchange.getRequestURI().getPath(), exchange.getRequestMethod());
        switch (endpointData.endpoint()){
            case GET_TASK_BY_ID -> sendTask(exchange, endpointData.idOptional().get());
            case GET_TASKS -> System.out.println("Запрос на таски");
        }

    }

    void fortest(){
        EndpointData data = getEndpoint("epics", "/epics/45", "GET");
        System.out.println(data.endpoint());
        System.out.println(data.idOptional().orElse(-999));
    }

    private void sendTask(HttpExchange exchange, int id) throws IOException {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                // любые другие методы билдера
                .create(); // завершаем построение объекта
        Optional<Task> taskOptional = manager.getTaskById(id);
        if(taskOptional.isPresent()){

            String taskJSON = gson.toJson(taskOptional.get());
            sendText(exchange, taskJSON);
        } else {
            sendText(exchange, gson.toJson("нет такой задачи"));
        }
    }
}
