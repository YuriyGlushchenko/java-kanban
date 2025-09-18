package api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.SubTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;
import service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class HttpTaskServerSubTaskEndpointsTest {

    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = new InMemoryTaskManager();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = taskServer.getGson();

    public HttpTaskServerSubTaskEndpointsTest() throws IOException {
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
    public void testAddSubTask() throws IOException, InterruptedException {
        // создаём родительский эпик
        Epic epic = new Epic("Test Epic", "Testing epic 2");
        int epicId = manager.addNewEpic(epic);

        // создаем сабтаск
        SubTask subTask = new SubTask("Test Subtask", "Testing subTask", epicId);
        subTask.setDuration(Duration.ofMinutes(5));
        subTask.setStartTime(LocalDateTime.now());

        // конвертируем subTask в JSON
        String subTaskJson = gson.toJson(subTask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subTaskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<SubTask> subTasksFromManager = manager.getAllSubTasks();

        assertNotNull(subTasksFromManager, "subTask не возвращаются");
        assertEquals(1, subTasksFromManager.size(), "Некорректное количество subTask");
        assertEquals("Test Subtask", subTasksFromManager.getFirst().getTitle(), "Некорректное имя задачи");
    }

    @Test
    public void testGetSubTaskById() throws IOException, InterruptedException {
        // создаём родительский эпик
        Epic epic = new Epic("Test Epic", "Testing epic 2");
        int epicId = manager.addNewEpic(epic);

        // создаем сабтаск
        SubTask subTask = new SubTask("Test Subtask", "Testing subTask", epicId);
        subTask.setDuration(Duration.ofMinutes(5));
        subTask.setStartTime(LocalDateTime.now());
        int id = manager.addNewSubTask(subTask);
        String subTaskJson = gson.toJson(manager.getSubTaskById(id).orElse(new SubTask("", "", 999))); // сериализованная задача из менеджера

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        // вызываем рест, отвечающий за получение задачи по id
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "неверный статус-код");
        assertEquals(response.body(), subTaskJson, "Задача из менеджера не совпадает с полученной по сети");
    }

    @Test
    public void testGetSubTasks() throws IOException, InterruptedException {
        // создаём родительский эпик
        Epic epic = new Epic("Test Epic", "Testing epic 2");
        int epicId = manager.addNewEpic(epic);

        // создаем сабтаск
        SubTask subTask = new SubTask("Test Subtask", "Testing subTask", epicId);
        subTask.setDuration(Duration.ofMinutes(5));
        subTask.setStartTime(LocalDateTime.now());
        int id = manager.addNewSubTask(subTask);

        // создаем сабтаск
        SubTask subTask2 = new SubTask("Test Subtask2", "Testing subTask2", epicId);
        subTask2.setDuration(Duration.ofMinutes(5));
        subTask2.setStartTime(LocalDateTime.of(2023, 10, 10, 10, 10));
        int id2 = manager.addNewSubTask(subTask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        // вызываем рест, отвечающий за получение списка subtasks
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        class SubTaskListTypeToken extends TypeToken<List<SubTask>> {
        }
        List<SubTask> responseSubTaskList = gson.fromJson(response.body(), new SubTaskListTypeToken().getType());
        assertEquals(manager.getAllSubTasks(), responseSubTaskList, "Список subTask из менеджера не совпадает с полученным по сети");
    }

    @Test
    public void testUpdateSubTask() throws IOException, InterruptedException {
        // создаём родительский эпик
        Epic epic = new Epic("Test Epic", "Testing epic 2");
        int epicId = manager.addNewEpic(epic);

        // создаем сабтаск
        SubTask subTask = new SubTask("Test Subtask", "Testing subTask", epicId);
        subTask.setDuration(Duration.ofMinutes(5));
        subTask.setStartTime(LocalDateTime.now());
        int id = manager.addNewSubTask(subTask);

        // Создаем обновленную задачу
        SubTask updatedSubTask = new SubTask("Test 2 Update", "Testing subTask 2 Update", epicId);
        updatedSubTask.setDuration(Duration.ofMinutes(15));
        updatedSubTask.setStartTime(LocalDateTime.of(2023, 12, 12, 10, 0));
        updatedSubTask.setId(id); // присваиваем задаче id первой, теперь для менеджера эта та же самая задача, но с новыми данными
        String updatedTaskJson = gson.toJson(updatedSubTask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(updatedTaskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что в менеджере одна задача с корректным именем
        List<SubTask> subTasksFromManager = manager.getAllSubTasks();

        assertNotNull(subTasksFromManager, "Задачи не возвращаются");
        assertEquals(1, subTasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2 Update", subTasksFromManager.getFirst().getTitle(), "Некорректное имя задачи");
        assertEquals(updatedTaskJson, gson.toJson(manager.getSubTaskById(id).orElse(new SubTask("", "", 999))), "Задача не обновилась");
    }

    @Test
    public void testDeleteSubTask() throws IOException, InterruptedException {
        // создаём родительский эпик
        Epic epic = new Epic("Test Epic", "Testing epic 2");
        int epicId = manager.addNewEpic(epic);

        // создаем сабтаск
        SubTask subTask = new SubTask("Test Subtask", "Testing subTask", epicId);
        subTask.setDuration(Duration.ofMinutes(5));
        subTask.setStartTime(LocalDateTime.now());
        int id = manager.addNewSubTask(subTask);

        assertEquals(1, manager.getAllSubTasks().size());
        assertEquals("Test Subtask", manager.getAllSubTasks().getFirst().getTitle());

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertEquals(0, manager.getAllSubTasks().size());
        assertTrue(manager.getSubTaskById(id).isEmpty());
    }

    @Test
    public void shouldReturn406CodeWhenSubTasksOverlaps() throws IOException, InterruptedException {
        // создаём родительский эпик
        Epic epic = new Epic("Test Epic", "Testing epic 2");
        int epicId = manager.addNewEpic(epic);

        // создаем сабтаск
        SubTask subTask = new SubTask("Test Subtask", "Testing subTask", epicId);
        subTask.setDuration(Duration.ofMinutes(5));
        subTask.setStartTime(LocalDateTime.of(2023, 10, 10, 10, 10));
        int id = manager.addNewSubTask(subTask);

        // создаем сабтаск
        SubTask overlapedSubTask = new SubTask("Test Subtask2", "Testing subTask2", epicId);
        overlapedSubTask.setDuration(Duration.ofMinutes(5));
        overlapedSubTask.setStartTime(LocalDateTime.of(2023, 10, 10, 10, 11));
        String overlapedSubTaskJson = gson.toJson(overlapedSubTask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(overlapedSubTaskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonObject());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertEquals("Задача пересекается по времени с существующей.", jsonObject.get("message").getAsString());
    }

    @Test
    public void shouldReturn404CodeWhenSubTaskDoseNotExist() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/9999");
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
        assertEquals("Запрашиваемые данные не найдены", jsonObject.get("message").getAsString());
    }

    @Test
    public void shouldReturn404CodeWhenUnknownEndpoint() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/fwefwfw");
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