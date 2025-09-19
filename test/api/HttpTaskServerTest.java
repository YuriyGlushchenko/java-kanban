package api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class HttpTaskServerTest {

    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = new InMemoryTaskManager();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = Managers.getGson();

    public HttpTaskServerTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteAllTasks();
        manager.deleteAllSubTasks();
        manager.deleteAllEpics();
        taskServer.startServer();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stopServer();
    }

    @Test
    public void testGetHistory() throws IOException, InterruptedException {
        // создаём родительский эпик
        Epic epic = new Epic("Test Epic", "Testing epic 2");
        int epicId = manager.addNewEpic(epic);

        // создаем сабтаск
        SubTask subTask = new SubTask("Test Subtask", "Testing subTask", epicId);
        subTask.setDuration(Duration.ofMinutes(5));
        subTask.setStartTime(LocalDateTime.now());
        int subTask1id = manager.addNewSubTask(subTask);

        // создаем сабтаск
        SubTask subTask2 = new SubTask("Test Subtask2", "Testing subTask2", epicId);
        subTask2.setDuration(Duration.ofMinutes(5));
        subTask2.setStartTime(LocalDateTime.of(2023, 10, 10, 10, 10));
        int subTask2id = manager.addNewSubTask(subTask);

        Task task = new Task("Test 2", "Testing task 2");
        task.setDuration(Duration.ofMinutes(5));
        task.setStartTime(LocalDateTime.of(2022, 10, 10, 10, 10));
        int taskId = manager.addNewTask(task);

        // создаем историю
        manager.getTaskById(taskId);
        manager.getEpicById(epicId);
        manager.getSubTaskById(subTask2id);
        manager.getSubTaskById(subTask1id);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        // вызываем рест, отвечающий за получение списка subtasks
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(gson.toJson(manager.getHistory()));
        assertEquals(200, response.statusCode());
        assertEquals(gson.toJson(manager.getHistory()), response.body(), "История из менеджера не совпадает с полученным по сети");
    }

    @Test
    public void testGetPrioritized() throws IOException, InterruptedException {
        // создаём родительский эпик
        Epic epic = new Epic("Test Epic", "Testing epic 2");
        int epicId = manager.addNewEpic(epic);

        // создаем сабтаск
        SubTask subTask = new SubTask("Test Subtask", "Testing subTask", epicId);
        subTask.setDuration(Duration.ofMinutes(5));
        subTask.setStartTime(LocalDateTime.now());
        int subTask1id = manager.addNewSubTask(subTask);

        // создаем сабтаск
        SubTask subTask2 = new SubTask("Test Subtask2", "Testing subTask2", epicId);
        subTask2.setDuration(Duration.ofMinutes(5));
        subTask2.setStartTime(LocalDateTime.of(2023, 10, 10, 10, 10));
        int subTask2id = manager.addNewSubTask(subTask);

        Task task = new Task("Test 2", "Testing task 2");
        task.setDuration(Duration.ofMinutes(5));
        task.setStartTime(LocalDateTime.of(2022, 10, 10, 10, 10));
        int taskId = manager.addNewTask(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        // вызываем рест, отвечающий за получение списка subtasks
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(gson.toJson(manager.getPrioritizedTasks()));
        assertEquals(200, response.statusCode());
        assertEquals(gson.toJson(manager.getPrioritizedTasks()), response.body(), "Приоритетные задачи из менеджера не совпадает с полученным по сети");
    }

    @Test
    public void shouldReturn404CodeWhenUnknownhistoryEndpoint() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history/fwefwfw");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        // вызываем рест, отвечающий за получение задачи по id
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "неверный статус-код");
        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonObject());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertEquals("Такого эндпоинта не существует", jsonObject.get("message").getAsString());
    }

    @Test
    public void shouldReturn404CodeWhenUnknownPrioritizedEndpoint() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized/fwefwfw");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        // вызываем рест, отвечающий за получение задачи по id
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "неверный статус-код");
        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonObject());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertEquals("Такого эндпоинта не существует", jsonObject.get("message").getAsString());
    }
}