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
import service.Managers;
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


public class HttpTaskServerEpicsEndpoinsTest {

    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = new InMemoryTaskManager();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = Managers.getGson();

    public HttpTaskServerEpicsEndpoinsTest() throws IOException {
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
    public void testAddEpic() throws IOException, InterruptedException {
        // создаём задачу
        Epic epic = new Epic("Test Epic", "Testing epic 2");
        String epicJson = gson.toJson(epic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Epic> epicFromManager = manager.getAllEpics();

        assertNotNull(epicFromManager, "Эпики не возвращаются");
        assertEquals(1, epicFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Test Epic", epicFromManager.getFirst().getTitle(), "Некорректное имя задачи");
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic 2");
        int id = manager.addNewEpic(epic);
        String taskJson = gson.toJson(manager.getEpicById(id).orElse(new Epic("", ""))); // сериализованная задача из менеджера

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        // вызываем рест, отвечающий за получение задачи по id
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "неверный статус-код");
        assertEquals(response.body(), taskJson, "Эпик из менеджера не совпадает с полученным по сети");
    }

    @Test
    public void testGetEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic 2");
        int id = manager.addNewEpic(epic);


        Epic epicB = new Epic("Test EpicB", "Testing epic 2B");
        int idB = manager.addNewEpic(epicB);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        // вызываем рест, отвечающий за получение задачи по id
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        class EpicsListTypeToken extends TypeToken<List<Epic>> {
        }
        List<Epic> responseEpicList = gson.fromJson(response.body(), new EpicsListTypeToken().getType());
        assertEquals(manager.getAllEpics(), responseEpicList, "Список эпиков из менеджера не совпадает с полученным по сети");
    }

    @Test
    public void testUpdateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic 2");
        int id = manager.addNewEpic(epic);

        // Создаем обновленную задачу
        Epic updatedEpic = new Epic("Updated Epic", "Testing updated Epic");
        updatedEpic.setId(id); // присваиваем задаче id первой, теперь для менеджера эта та же самая задача, но с новыми данными
        String updatedEpicJson = gson.toJson(updatedEpic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(updatedEpicJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что в менеджере одна задача с корректным именем
        List<Epic> epicsFromManager = manager.getAllEpics();

        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Updated Epic", epicsFromManager.getFirst().getTitle(), "Некорректное имя эпика");
        assertEquals(updatedEpicJson, gson.toJson(manager.getEpicById(id).orElse(new Epic("", ""))), "Эпик не обновилась");
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic 2");
        int id = manager.addNewEpic(epic);

        assertEquals(1, manager.getAllEpics().size());
        assertEquals("Test Epic", manager.getAllEpics().getFirst().getTitle());

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertEquals(0, manager.getAllEpics().size());
        assertTrue(manager.getEpicById(id).isEmpty());
    }

    @Test
    public void testGetEpicSubTasks() throws IOException, InterruptedException {
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
        URI url = URI.create("http://localhost:8080/epics/" + epicId + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        // вызываем рест, отвечающий за получение задачи по id
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        class SubTaskListTypeToken extends TypeToken<List<SubTask>> {
        }
        List<SubTask> responseSubTaskList = gson.fromJson(response.body(), new SubTaskListTypeToken().getType());
        assertEquals(manager.getEpicSubTasks(epicId), responseSubTaskList, "Список EpicSubTask из менеджера не совпадает с полученным по сети");

    }

    @Test
    public void shouldReturn404CodeWhenEpicDoseNotExist() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/9999");
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
        URI url = URI.create("http://localhost:8080/epics/fwefwfw");
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
    public void shouldReturn404CodeWhenRequestingSubTasksofNotExistingEpic() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/9999/subtasks");
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
}