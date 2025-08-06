import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryHistoryManager;

import java.util.List;

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
    public void ShouldAddTaskToHistory() {
        Task task1 = new Task("Простая задача1", "Описание простой задачи 1");
        task1.setId(10);
        historyManager.add(task1);
        assertTrue(historyManager.getHistory().contains(task1));
    }

    @Test
    public void ShouldDeleteTaskFromHistory() {
        assertFalse(historyManager.getHistory().contains(task1));
        historyManager.add(task1);
        historyManager.add(task1);
        historyManager.add(task1);
        assertTrue(historyManager.getHistory().contains(task1));

        historyManager.remove(task1.getId());
        assertFalse(historyManager.getHistory().contains(task1));
    }

    @Test
    void addShouldReplaceDuplicateTask() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1); // Дубликат, должен переместиться в конец

        assertEquals(List.of(task2, task1), historyManager.getHistory());
    }

    @Test
    void addShouldMaintainOrderAfterMultipleAdds() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(epic1);
        historyManager.add(epic2);
        historyManager.add(subTask1);
        historyManager.add(subTask2);

        assertEquals(List.of(task1, task2, epic1, epic2, subTask1, subTask2), historyManager.getHistory());
    }

    @Test
    void removeShouldDeleteTaskFromBeginning() {
        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(1); // Удаляем первую задачу

        assertEquals(List.of(task2), historyManager.getHistory());
    }

    @Test
    void removeShouldDeleteTaskFromMiddle() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(epic1);
        historyManager.add(subTask1);

        historyManager.remove(2); // Удаляем из середины

        assertEquals(List.of(task1, epic1, subTask1), historyManager.getHistory());
    }

    @Test
    void removeShouldDeleteTaskFromEnd() {
        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(2); // Удаляем последнюю

        assertEquals(List.of(task1), historyManager.getHistory());
    }

    @Test
    void removeShouldDoNothingIfTaskNotExists() {
        historyManager.add(task1);

        historyManager.remove(999); // Несуществующий ID

        assertEquals(List.of(task1), historyManager.getHistory());
    }

    @Test
    void clearHistoryShouldRemoveAllTasks() {
        historyManager.add(task1);
        historyManager.add(task2);
        assertEquals(List.of(task1, task2), historyManager.getHistory());

        historyManager.clearHistory();

        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void getHistoryShouldReturnEmptyListIfNoTasks() {
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void getHistory_ShouldReturnTasksInCorrectOrder() {
        historyManager.add(task1);
        historyManager.add(epic1);
        historyManager.add(task2);
        historyManager.add(epic2);
        historyManager.add(subTask1);

        assertEquals(List.of(task1, epic1, task2, epic2, subTask1), historyManager.getHistory());
    }
}