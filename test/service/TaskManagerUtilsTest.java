package service;

import model.Epic;
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

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerUtilsTest {
    private static FileBackedTaskManager manager;
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
    private File testFile;

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
        epic1Id = manager.addNewEpic(epic1);
        epic2Id = manager.addNewEpic(epic2);

        subTask1 = new SubTask("Подзадача 1", "описание подзадачи1", epic1Id);
        subTask1Id = manager.addNewSubTask(subTask1);
        subTask2 = new SubTask("Подзадача 2", "описание подзадачи2", epic1Id);
        subTask2Id = manager.addNewSubTask(subTask2);
    }

    @Test
    public void ShouldCorrectlyConverTaskToString() {
        task1.setDuration(Duration.ofMinutes(50));
        task1.setStartTime(LocalDateTime.of(2025,9,7,20,0));
        String convertedTask = TaskManagerUtils.convertToString(task1);
        String expected = String.format("%d,TASK,Простая задача1,NEW,Описание простой задачи 1,50,07.09.2025 20:00,", task1Id);
        assertEquals(expected, convertedTask, "Конвертация проходит неправильно");
    }

    @Test
    public void ShouldCorrectlyConverSubTaskToString() {
        String convertedTask = TaskManagerUtils.convertToString(subTask1);
        String expected = String.format("%d,SUBTASK,Подзадача 1,NEW,описание подзадачи1,0,null,%d", subTask1Id, epic1Id);
        assertEquals(expected, convertedTask, "Конвертация проходит неправильно");
    }

}