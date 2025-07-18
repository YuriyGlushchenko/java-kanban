import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private static TaskManager manager;
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
        manager = new InMemoryTaskManager();
        task1 = new Task("Простая задача1", "Описание простой задачи 1");
        task1Id = manager.addNewTask(task1);
        task2 = new Task("Простая задача2", "Описание простой задачи 2");
        task2Id = manager.addNewTask(task2);

        epic1 = new Epic("Важный эпик1", "описние эпика 1");
        epic1Id = manager.addNewTask(epic1);
        subTask1 = new SubTask("Подзадача 1", "описание подзадачи1", epic1);
        subTask1Id = manager.addNewTask(subTask1);
        subTask2 = new SubTask("Подзадача 2", "описание подзадачи2", epic1);
        subTask2Id = manager.addNewTask(subTask2);

    }
    @Test
    public void shouldFindTask2ByTask2Id(){
        assertEquals(task2, manager.getTaskById(task2Id));
    }

    @Test
    public void shouldFindEpic1ByEpic1Id(){
        assertEquals(epic1, manager.getTaskById(epic1Id));
    }

    @Test
    public void shouldFindSubTask1BySubTask1Id(){
        assertEquals(subTask1, manager.getTaskById(subTask1Id));
    }

    @Test
    public void shouldReturnEmptyHistoryListOnNewTasks(){
        System.out.println(manager.getHistory());
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    public void shouldReturnCorrectSizeOfHistoryListAfterTaskView(){
        manager.getTaskById(epic1Id);
        manager.getTaskById(task2Id);
        manager.getTaskById(task1Id);
        assertEquals(3, manager.getHistory().size());
    }

    @Test
    public void shouldReturnTaskInHistoryListAfterTaskView(){
        manager.getTaskById(epic1Id);
        LinkedList<Task> expectedList = new LinkedList<>();
        expectedList.add(epic1);
        assertIterableEquals(expectedList, manager.getHistory());
    }


}