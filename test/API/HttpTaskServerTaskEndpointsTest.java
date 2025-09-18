package API;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import model.Task;
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


public class HttpTaskServerTaskEndpointsTest {

    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = new InMemoryTaskManager();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = taskServer.getGson();

    public HttpTaskServerTaskEndpointsTest() throws IOException {
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
    public void testAddTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test 2", "Testing task 2");
        task.setDuration(Duration.ofMinutes(5));
        task.setStartTime(LocalDateTime.now());

        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.getFirst().getTitle(), "Некорректное имя задачи");
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test task", "Testing task");
        task.setDuration(Duration.ofMinutes(5));
        task.setStartTime(LocalDateTime.now());
        int id = manager.addNewTask(task);
        String taskJson = gson.toJson(manager.getTaskById(id).orElse(new Task())); // сериализованная задача из менеджера

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        // вызываем рест, отвечающий за получение задачи по id
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "неверный статус-код");
        assertEquals(response.body(), taskJson, "Задача из менеджера не совпадает с полученной по сети");
    }

    @Test
    public void testGetTasks() throws IOException, InterruptedException {
        Task task = new Task("Test task", "Testing task");
        task.setDuration(Duration.ofMinutes(5));
        task.setStartTime(LocalDateTime.now());
        int id = manager.addNewTask(task);

        Task taskB = new Task("Test taskB", "Testing taskB");
        taskB.setDuration(Duration.ofMinutes(5));
        taskB.setStartTime(LocalDateTime.of(2023, 12, 12, 10, 0));
        int idB = manager.addNewTask(taskB);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        // вызываем рест, отвечающий за получение задачи по id
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        class TaskListTypeToken extends TypeToken<List<Task>> {
        }
        List<Task> responseTaskList = gson.fromJson(response.body(), new TaskListTypeToken().getType());
        assertEquals(manager.getAllTasks(), responseTaskList, "Список задач из менеджера не совпадает с полученным по сети");
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2");
        task.setDuration(Duration.ofMinutes(5));
        task.setStartTime(LocalDateTime.now());
        int id = manager.addNewTask(task);

        // Создаем обновленную задачу
        Task updatedTask = new Task("Test 2 Update", "Testing task 2 Update");
        updatedTask.setDuration(Duration.ofMinutes(15));
        updatedTask.setStartTime(LocalDateTime.of(2023, 12, 12, 10, 0));
        updatedTask.setId(id); // присваиваем задаче id первой, теперь для менеджера эта та же самая задача, но с новыми данными
        String updatedTaskJson = gson.toJson(updatedTask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(updatedTaskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что в менеджере одна задача с корректным именем
        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2 Update", tasksFromManager.getFirst().getTitle(), "Некорректное имя задачи");
        assertEquals(updatedTaskJson, gson.toJson(manager.getTaskById(id).orElse(new Task())), "Задача не обновилась");
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("Test task", "Testing task");
        task.setDuration(Duration.ofMinutes(5));
        task.setStartTime(LocalDateTime.now());
        int id = manager.addNewTask(task);

        assertEquals(1, manager.getAllTasks().size());
        assertEquals("Test task", manager.getAllTasks().getFirst().getTitle());

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertEquals(0, manager.getAllTasks().size());
        assertTrue(manager.getTaskById(id).isEmpty());
    }

    @Test
    public void shouldReturn406CodeWhenTasksOverlaps() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2");
        task.setDuration(Duration.ofMinutes(60));
        task.setStartTime(LocalDateTime.of(2023, 12, 12, 10, 0));
        int id = manager.addNewTask(task);

        // Создаем пересекающуюся задачу
        Task overlapedTask = new Task("Overlaps", "Testing task is overlap");
        overlapedTask.setDuration(Duration.ofMinutes(60));
        overlapedTask.setStartTime(LocalDateTime.of(2023, 12, 12, 10, 1));
        String overlapedTaskJson = gson.toJson(overlapedTask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(overlapedTaskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonObject());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertEquals("Задача пересекается по времени с существующей.", jsonObject.get("message").getAsString());
    }

    @Test
    public void shouldReturn404CodeWhenTaskDoseNotExist() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/9999");
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
        URI url = URI.create("http://localhost:8080/tasks/fwefwfw");
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