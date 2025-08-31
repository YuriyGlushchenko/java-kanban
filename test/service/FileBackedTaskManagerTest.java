package service;

import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
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


    @BeforeEach
    public void beforeEach() {
        manager = new FileBackedTaskManager(new File("D:\\Java\\java-kanban\\src\\data.csv"));
        task1 = new Task("Простая задача1", "Описание простой задачи 1");
        task1Id = manager.addNewTask(task1);
        task2 = new Task("Простая задача2", "Описание простой задачи 2");
        task2Id = manager.addNewTask(task2);

        epic1 = new Epic("Важный эпик1", "Описание эпика 1");
        epic2 = new Epic("Важный эпик2", "Описание эпика 2");
        epic1Id = manager.addNewTask(epic1);
        epic2Id = manager.addNewTask(epic2);

        subTask1 = new SubTask("Подзадача 1", "описание подзадачи1", epic1);
        subTask1Id = manager.addNewTask(subTask1);
        subTask2 = new SubTask("Подзадача 2", "описание подзадачи2", epic1);
        subTask2Id = manager.addNewTask(subTask2);
    }

    @Test
    public void ShouldCorrectlyConverTaskToString() {
        String convertedTask = manager.convertToString(task1);
        String expected = String.format("%d,TASK,Простая задача1,NEW,Описание простой задачи 1,", task1Id);
        assertEquals(expected, convertedTask, "Конвертация проходит неправильно");
    }

    @Test
    public void ShouldCorrectlyConverSubTaskToString() {
        String convertedTask = manager.convertToString(subTask1);
        String expected = String.format("%d,SUBTASK,Подзадача 1,NEW,описание подзадачи1,%d", subTask1Id, epic1Id);
        assertEquals(expected, convertedTask, "Конвертация проходит неправильно");
    }

}