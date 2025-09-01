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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private static FileBackedTaskManager manager;
    private File testFile;

    private static Task task1;
    private static int task1Id;
    private static Task task2;
    private static int task2Id;

    private static Epic epic1;
    private static Epic epic2;
    private static int epic1Id;
    private static int epic2Id;

    private static SubTask subTask1;
    private static SubTask subTask2;
    private static int subTask1Id;
    private static int subTask2Id;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void beforeEach() throws IOException {
        testFile = Files.createTempFile(tempDir, "test", ".csv").toFile();
        manager = new FileBackedTaskManager(testFile);
        task1 = new Task("Простая задача1", "Описание простой задачи 1");
        task1Id = manager.addNewTask(task1);
        task2 = new Task("Простая задача2", "Описание простой задачи 2");
        task2Id = manager.addNewTask(task2);

        epic1 = new Epic("Важный эпик1", "Описание эпика 1");
        epic2 = new Epic("Важный эпик2", "Описание эпика 2");
        epic1Id = manager.addNewTask(epic1);
        epic2Id = manager.addNewTask(epic2);

        subTask1 = new SubTask("Подзадача 1", "описание подзадачи1", epic1Id);
        subTask1Id = manager.addNewTask(subTask1);
        subTask2 = new SubTask("Подзадача 2", "описание подзадачи2", epic1Id);
        subTask2Id = manager.addNewTask(subTask2);
    }

    @Test
    public void ShouldCorrectlyConverTaskToString() {
        String convertedTask = FileBackedTaskManager.convertToString(task1);
        String expected = String.format("%d,TASK,Простая задача1,NEW,Описание простой задачи 1,", task1Id);
        assertEquals(expected, convertedTask, "Конвертация проходит неправильно");
    }

    @Test
    public void ShouldCorrectlyConverSubTaskToString() {
        String convertedTask = FileBackedTaskManager.convertToString(subTask1);
        String expected = String.format("%d,SUBTASK,Подзадача 1,NEW,описание подзадачи1,%d", subTask1Id, epic1Id);
        assertEquals(expected, convertedTask, "Конвертация проходит неправильно");
    }

    @Test
    void testSaveAndLoadEmptyFile() throws IOException {
        testFile = Files.createTempFile(tempDir, "test11", ".csv").toFile();
        manager = new FileBackedTaskManager(testFile);
        manager.save();

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
        Task updatedTask = new Task("Обновленная задача", "Обновленное описание");
        updatedTask.setId(task1Id);
        updatedTask.setStatus(Status.IN_PROGRESS);
        manager.updateTask(updatedTask);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Проверяем обновленные данные
        Task loadedTask = loadedManager.getTaskById(task1Id);
        assertEquals("Обновленная задача", loadedTask.getTitle());
        assertEquals("Обновленное описание", loadedTask.getDescription());
        assertEquals(Status.IN_PROGRESS, loadedTask.getStatus());
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

}