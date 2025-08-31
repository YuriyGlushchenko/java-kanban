package service;

import model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    public void managersShouldReturnNonNullInstanceOfTaskManager() {
        assertNotNull(Managers.getDefault());
    }

    @Test
    public void managersShouldReturnNonNullInstanceOfHistoryManager() {
        assertNotNull(Managers.getDefaultHistory());
    }

    @Test
    public void managersShouldReturnInitializedInstanceOfTaskManager() {
        TaskManager manager = Managers.getDefault();
        Task task1 = new Task("Простая задача1", "Описание простой задачи 1");
        assertDoesNotThrow(() -> manager.addNewTask(task1));

        Task task2 = new Task("Простая задача1", "Описание простой задачи 1");
        Object expectedTask2id = manager.addNewTask(task2);
        assertInstanceOf(Integer.class, expectedTask2id, "Должен возвращаться ID типа int");

    }
}