package service;

import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

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
    public void shouldFindTask2ByTask2Id() {
        assertEquals(task2, manager.getAnyTypeTaskById(task2Id));
    }

    @Test
    public void shouldFindEpic1ByEpic1Id() {
        assertEquals(epic1, manager.getAnyTypeTaskById(epic1Id));
    }

    @Test
    public void shouldFindSubTask1BySubTask1Id() {
        assertEquals(subTask1, manager.getAnyTypeTaskById(subTask1Id));
    }

    @Test
    public void shouldReturnEmptyHistoryListOnNewTasks() {
        System.out.println(manager.getHistory());
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    public void shouldReturnCorrectSizeOfHistoryListAfterTaskView() {
        manager.getAnyTypeTaskById(epic1Id);
        manager.getAnyTypeTaskById(task2Id);
        manager.getAnyTypeTaskById(task1Id);
        assertEquals(3, manager.getHistory().size());
    }

    @Test
    public void shouldReturnTaskInHistoryListAfterTaskView() {
        manager.getAnyTypeTaskById(epic1Id);
        LinkedList<Task> expectedList = new LinkedList<>();
        expectedList.add(epic1);
        assertIterableEquals(expectedList, manager.getHistory());
    }

    @Test
    public void ShouldBeTheSameTaskIfIDsMatch() {
        Task savedTask = manager.getAnyTypeTaskById(task1Id);
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task1, savedTask, "Задачи не совпадают.");
    }

    @Test
    public void ShouldBeTheSameEpicIfIDsMatch() {
        Task savedEpic = manager.getAnyTypeTaskById(epic1Id);
        assertNotNull(savedEpic, "Задача не найдена.");
        assertEquals(epic1, savedEpic, "Задачи не совпадают.");
    }

    @Test
    public void ShouldBeTheSameSubTaskIfIDsMatch() {
        Task savedSubTask = manager.getAnyTypeTaskById(subTask1Id);
        assertNotNull(savedSubTask, "Задача не найдена.");
        assertEquals(subTask1, savedSubTask, "Задачи не совпадают.");
    }

    @Test
    public void askShouldRemainUnmodifiedAfterAddingToManager() {
        String title = "title";
        String description = "description";
        Task newTask = new Task(title, description);
        int id = manager.addNewTask(newTask);

        assertEquals(title, manager.getAnyTypeTaskById(id).getTitle());
        assertEquals(description, manager.getAnyTypeTaskById(id).getDescription());
    }

    @Test
    public void shouldCleanAll() {
        assertEquals(2, manager.getAllTasks().size());
        assertEquals(2, manager.getAllEpics().size());
        assertEquals(2, manager.getAllSubTasks().size());
        assertEquals(6, manager.getAllTypesTask().size());

        manager.deleteAllTasks();

        assertTrue(manager.getAllTasks().isEmpty());
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubTasks().isEmpty());
        assertTrue(manager.getAllTypesTask().isEmpty());
    }


    @Test
    void getTaskByIdShouldReturnTask() {
        Task actualTask = manager.getTaskById(task1Id);
        assertEquals(task1, actualTask, "Метод вернул не ту задачу");
    }

    @Test
    void getTaskByIdShouldThrowWhenTaskDoesNotExist() {
        assertThrows(
                NoSuchElementException.class,
                () -> manager.getTaskById(-11),
                "Метод должен кидать NoSuchElementException, если задача не найдена"
        );
    }

    @Test
    void getEpicByIdShouldReturnEpic() {
        Epic actualEpic = manager.getEpicById(epic1Id);
        assertEquals(epic1, actualEpic, "Метод вернул не ту задачу");
    }

    @Test
    void getEpicByIdShouldThrowWhenTaskDoesNotExist() {
        assertThrows(
                NoSuchElementException.class,
                () -> manager.getEpicById(-11),
                "Метод должен кидать NoSuchElementException, если задача не найдена"
        );
    }

    @Test
    void getSubTaskByIdShouldReturnSubTask() {
        SubTask actualSubTask = manager.getSubTaskById(subTask1Id);
        assertEquals(subTask1, actualSubTask, "Метод вернул не ту задачу");
    }

    @Test
    void getSubTaskByIdShouldThrowWhenTaskDoesNotExist() {
        assertThrows(
                NoSuchElementException.class,
                () -> manager.getSubTaskById(-11),
                "Метод должен кидать NoSuchElementException, если задача не найдена"
        );
    }

    @Test
    void testNoStaleSubTaskIdsInEpicAfterDeletion() {
        assertEquals(List.of(subTask1, subTask2), manager.getEpicSubTasks(epic1Id));
        manager.deleteTask(subTask1Id);
        manager.deleteTask(subTask2Id);

        assertFalse(epic1.getEpicSubTasks().stream() //Внутри эпиков не должно оставаться неактуальных id подзадач.
                .anyMatch(subTask -> (subTask.getId() == subTask1Id) || (subTask.getId() == subTask2Id)));
    }

    @Test
    void shouldBeEmptyHistoryAtTheBeginning() {
        assertTrue(manager.getHistory().isEmpty(), "История должна быть пустой до просмотра задач");
    }


    @Test
    void shouldRemoveTaskFromHistoryWhenTaskDeleted() {
        assertTrue(manager.getHistory().isEmpty(), "История должна быть пустой до просмотра задач");

        manager.getTaskById(task1Id); // просматриваем одну задачу
        assertEquals(List.of(task1), manager.getHistory(), "В истории должна быть одна просмотренная задача");

        manager.deleteTask(task1Id); // удаляем задачу
        assertTrue(manager.getHistory().isEmpty(), "Задача должна удалиться и из истории при удалении задачи");
    }

    @Test
    void shouldRemoveAllSubTasksFromHistoryWhenEpicDeleted() {
        assertTrue(manager.getHistory().isEmpty(), "История должна быть пустой до просмотра задач");

        manager.getEpicById(epic1Id); // просматриваем эпик
        manager.getSubTaskById(subTask1Id); // просматриваем подзадачу эпика
        manager.getSubTaskById(subTask2Id); // просматриваем вторую подзадачу эпика
        manager.getTaskById(task1Id); // просматриваем просто задачу
        assertEquals(
                List.of(epic1, subTask1, subTask2, task1),
                manager.getHistory(),
                "В истории должны быть просмотренные задачи"
        );

        manager.deleteTask(epic1Id); // удаляем эпик

        assertEquals(
                List.of(task1),
                manager.getHistory(),
                "Подзадачи должны удалиться из истории при удалении Epic, другие задачи остаться"
        );
    }

}