import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private static InMemoryHistoryManager historyManager;
    private static Task task1;
    private static Task task2;
    private static Epic epic1;
    private static Epic epic2;
    private static SubTask subTask1;
    private static SubTask subTask2;

    @BeforeEach
    public void beforeEach() {
        historyManager = new InMemoryHistoryManager();

        task1 = new Task("Простая задача1", "Описание простой задачи 1");
        task1.setId(1);
        task2 = new Task("Простая задача2", "Описание простой задачи 2");
        task2.setId(2);
        epic1 = new Epic("Важный эпик1", "описние эпика 1");
        epic1.setId(3);
        epic2 = new Epic("Важный эпик2", "описние эпика 2");
        epic2.setId(4);
        subTask1 = new SubTask("Подзадача 1", "описание подзадачи1", epic1);
        subTask1.setId(5);
        subTask2 = new SubTask("Подзадача 2", "описание подзадачи2", epic1);
        subTask2.setId(6);
    }

    @Test
    public void ShouldAddTaskToHistory(){
        Task task1 = new Task("Простая задача1", "Описание простой задачи 1");
        task1.setId(10);
        historyManager.add(task1);
        assertTrue(historyManager.getHistory().contains(task1));
    }

    @Test
    public void ShouldDeleteTaskFromHistory(){
        assertFalse(historyManager.getHistory().contains(task1));
        historyManager.add(task1);
        historyManager.add(task1);
        historyManager.add(task1);
        assertTrue(historyManager.getHistory().contains(task1));

        historyManager.delete(task1);
        assertFalse(historyManager.getHistory().contains(task1));
    }



}