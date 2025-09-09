package service;

import exeptions.ManagerSaveException;
import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @TempDir
    Path tempDir;
    private File testFile;

    @Override
    protected FileBackedTaskManager getManager() {

        try {
            testFile = Files.createTempFile(tempDir, "test", ".csv").toFile();
            testFile.deleteOnExit();
            return new FileBackedTaskManager(testFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test file", e);
        }
    }

    @Test
    void testSaveAndLoadEmptyFile() throws IOException {
        testFile = Files.createTempFile(tempDir, "test11", ".csv").toFile();
        manager = new FileBackedTaskManager(testFile);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertTrue(loadedManager.getAllTasks().isEmpty(), "В пустом менеджере не должно быть задач");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "В пустом менеджере не должно быть Эпиков");
        assertTrue(loadedManager.getAllSubTasks().isEmpty(), "В пустом менеджере не должно быть SubTask");
    }

    @Test
    void testSaveAndLoadMultipleTasks() {
        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Проверяем задачи
        List<Task> loadedTasks = loadedManager.getAllTasks();
        assertEquals(2, loadedTasks.size());
        assertEquals("Простая задача1", loadedTasks.get(0).getTitle());
        assertEquals("Простая задача2", loadedTasks.get(1).getTitle());

        // Проверяем эпики
        List<Epic> loadedEpics = loadedManager.getAllEpics();
        assertEquals(2, loadedEpics.size());
        assertEquals("Важный эпик1", loadedEpics.get(0).getTitle());
        assertEquals("Важный эпик2", loadedEpics.get(1).getTitle());

        // Проверяем подзадачи
        List<SubTask> loadedSubTasks = loadedManager.getAllSubTasks();
        assertEquals(2, loadedSubTasks.size());
        assertEquals("Подзадача 1", loadedSubTasks.get(0).getTitle());
        assertEquals("Подзадача 2", loadedSubTasks.get(1).getTitle());

        // Проверяем связь подзадач с эпиками
        assertEquals(epic1Id, loadedSubTasks.get(0).getParentEpicId());
        assertEquals(epic1Id, loadedSubTasks.get(1).getParentEpicId());
    }

    @Test
    void testSaveAndLoadAfterUpdate() {
        // Обновляем задачу
        task1.setTitle("Обновленная задача");
        task1.setDescription("Обновленное описание");
        task1.setStatus(Status.IN_PROGRESS);
        task1.setStartTime(LocalDateTime.of(2025, 9, 9, 16, 0));
        task1.setDuration(Duration.ofMinutes(30));
        manager.updateTask(task1);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Проверяем обновленные данные
        Optional<Task> loadedTaskOptional = loadedManager.getTaskById(task1Id);
        assertTrue(loadedTaskOptional.isPresent());
        Task loadedTask = loadedTaskOptional.get();
        assertEquals("Обновленная задача", loadedTask.getTitle());
        assertEquals("Обновленное описание", loadedTask.getDescription());
        assertEquals(Status.IN_PROGRESS, loadedTask.getStatus());
        assertEquals(Duration.ofMinutes(30), loadedTask.getDuration());
        assertEquals(LocalDateTime.of(2025, 9, 9, 16, 0), loadedTask.getStartTime().orElse(null));
    }

    @Test
    void testLoadFromNonExistentFile() {
        File nonExistentFile = new File("non_existent_file.csv");
        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(nonExistentFile));
    }

    @Test
    void testSaveAndLoadAfterDelete() {
        // проверяем, что изначально 2 task создано
        assertEquals(2, manager.getAllTasks().size());

        // Удаляем одну задачу
        manager.deleteTask(task1Id);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Проверяем, что осталась только одна задача
        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals("Простая задача2", loadedManager.getAllTasks().getFirst().getTitle());
    }

    @Test
    void loadFromFile_shouldSetCorrectCounterWhenFileHasTasks() throws IOException {
//        "id,type,name,status,description,duration,startTime,epic"
        List<String> testData = List.of(
                "id,type,name,status,description,duration,startTime,epic",
                "1,TASK,Task 1,NEW,Description 1,0,07.09.2025 20:50,",
                "5,EPIC,Epic 1,NEW,Epic description 1,10,null,",
                "10,SUBTASK,SubTask 1,NEW,Sub description 1,60,null,5",
                "3,TASK,Task 2,NEW,Description 2,50,null,"
        );
        Files.write(testFile.toPath(), testData);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Проверяем, что counter у loadedManager установлен корректно (максимальный id = 10)
        Task newTask = new Task("New Task", "New Description");
        int newId = loadedManager.addNewTask(newTask);

        assertEquals(11, newId, "Новый ID должен быть на 1 больше максимального из файла");
    }

    @Test
    void loadFromFile_shouldSetCounterToZeroWhenFileEmpty() throws IOException {
        // Файл только с заголовком
        List<String> testData = List.of("id,type,name,status,description,epic");
        Files.write(testFile.toPath(), testData);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        Task newTask = new Task("New Task", "New Description");
        int newId = loadedManager.addNewTask(newTask);

        assertEquals(1, newId, "При пустом файле counter должен начинаться с 1");
    }

    @Test
    void testAutoSaveOnOperations() {
        // Проверяем, что операции автоматически сохраняются
        int initialSize = manager.getAllTasks().size();

        Task newTask = new Task("AutoSave Test", "Description");
        int id = manager.addNewTask(newTask);

        // Загружаем из файла и проверяем, что задача сохранилась
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertEquals(initialSize + 1,
                loadedManager.getAllTasks().size(),
                "Задач должно стать на одну больше");

        assertTrue(loadedManager.getTaskById(id).isPresent());
        assertEquals("AutoSave Test",
                loadedManager.getTaskById(id).get().getTitle(),
                "Добавленная задача должна загрузиться из файла");
    }

    @Test
    void Save_ThrowsManagerSaveExceptionOnIOException() throws IOException {
        // Заведомо нерабочий файл = директория
        File readOnlyFile = tempDir.resolve("testttt.csv").toFile();
        Files.createDirectories(readOnlyFile.toPath());

        FileBackedTaskManager manager = new FileBackedTaskManager(readOnlyFile);

        // внутри вызывается save() -> должно выброситься исключение
        assertThrows(ManagerSaveException.class, () -> {
            manager.addNewTask(new Task("Another Task", "Description"));
        });
    }

}