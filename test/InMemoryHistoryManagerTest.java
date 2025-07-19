import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    @Test
    public void ShouldAddTaskToHistory(){
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();
        Task task1 = new Task("Простая задача1", "Описание простой задачи 1");

        historyManager.add(task1);
        assertTrue(historyManager.getHistory().contains(task1));
    }

}