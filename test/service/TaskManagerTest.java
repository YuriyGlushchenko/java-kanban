package service;

import model.Epic;
import model.SubTask;
import model.Task;
import model.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;
    protected Task task1;
    protected int task1Id;
    protected Task task2;
    protected int task2Id;

    protected Epic epic1;
    protected Epic epic2;
    protected int epic1Id;
    protected int epic2Id;

    protected SubTask subTask1;
    protected SubTask subTask2;
    protected int subTask1Id;
    protected int subTask2Id;

    protected abstract T getManager();


    @BeforeEach
    public void beforeEach() {
        manager = getManager();
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
    public void shouldFindTask2ByTask2Id() {
        assertEquals(task2, manager.getTaskById(task2Id));
    }

    @Test
    public void shouldFindEpic1ByEpic1Id() {
        assertEquals(epic1, manager.getEpicById(epic1Id));
    }

    @Test
    public void shouldFindSubTask1BySubTask1Id() {
        assertEquals(subTask1, manager.getSubTaskById(subTask1Id));
    }

    @Test
    public void shouldReturnEmptyHistoryListOnNewTasks() {
        System.out.println(manager.getHistory());
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    public void shouldReturnCorrectSizeOfHistoryListAfterTaskView() {
        manager.getEpicById(epic1Id);
        manager.getTaskById(task2Id);
        manager.getTaskById(task1Id);
        assertEquals(3, manager.getHistory().size());
    }

    @Test
    public void shouldReturnTaskInHistoryListAfterTaskView() {
        manager.getEpicById(epic1Id);
        LinkedList<Task> expectedList = new LinkedList<>();
        expectedList.add(epic1);
        assertIterableEquals(expectedList, manager.getHistory());
    }

    @Test
    public void ShouldBeTheSameTaskIfIDsMatch() {
        Task savedTask = manager.getTaskById(task1Id);
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task1, savedTask, "Задачи не совпадают.");
    }

    @Test
    public void ShouldBeTheSameEpicIfIDsMatch() {
        Task savedEpic = manager.getEpicById(epic1Id);
        assertNotNull(savedEpic, "Задача не найдена.");
        assertEquals(epic1, savedEpic, "Задачи не совпадают.");
    }

    @Test
    public void ShouldBeTheSameSubTaskIfIDsMatch() {
        Task savedSubTask = manager.getSubTaskById(subTask1Id);
        assertNotNull(savedSubTask, "Задача не найдена.");
        assertEquals(subTask1, savedSubTask, "Задачи не совпадают.");
    }

    @Test
    public void TaskShouldRemainUnmodifiedAfterAddingToManager() {
        String title = "title";
        String description = "description";
        Task newTask = new Task(title, description);
        int id = manager.addNewTask(newTask);

        assertEquals(title, manager.getTaskById(id).getTitle());
        assertEquals(description, manager.getTaskById(id).getDescription());
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
        manager.deleteSubTask(subTask1Id);
        manager.deleteSubTask(subTask2Id);

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

        manager.deleteEpic(epic1Id); // удаляем эпик

        assertEquals(
                List.of(task1),
                manager.getHistory(),
                "Подзадачи должны удалиться из истории при удалении Epic, другие задачи остаться"
        );
    }

    @Test
    void deleteAllSubTasks_shouldRemoveAllSubTasksAndUpdateEpics() {
        SubTask subTask3 = new SubTask("SubTask 3", "Description", epic2Id);
        manager.addNewSubTask(subTask3);

        manager.deleteAllSubTasks();

        assertTrue(manager.getAllSubTasks().isEmpty());
        assertTrue(manager.getEpicSubTasks(epic1Id).isEmpty());
        assertTrue(manager.getEpicSubTasks(epic2Id).isEmpty());
    }

    @Test
    void deleteAllEpics_shouldRemoveAllEpicsAndTheirSubTasks() {
        manager.deleteAllEpics();

        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubTasks().isEmpty());
    }

    @Test
    void deleteAllTasks_shouldRemoveAllTasks() {
        assertEquals(2, manager.getAllTasks().size());
        manager.deleteAllTasks();

        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    void deleteAllTasks_shouldNotAffectEpicsAndSubTasks() {
        manager.deleteAllTasks();

        assertTrue(manager.getAllTasks().isEmpty());
        assertEquals(2, manager.getAllEpics().size());
        assertEquals(2, manager.getAllSubTasks().size());
    }

    @Test
    void deleteNonExistentTask_shouldThrowException() {
        assertDoesNotThrow(() -> manager.deleteTask(999));
    }

    @Test
    void deleteNonExistentSubTask_shouldThrowException() {
        assertDoesNotThrow(() -> manager.deleteSubTask(999));
    }

    @Test
    void deleteNonExistentEpic_shouldThrowException() {
        assertDoesNotThrow(() -> manager.deleteEpic(999));
    }

    @Test
    void deleteAllSubTasks_shouldRemoveSubTasksFromHistory() {
        manager.getSubTaskById(subTask1Id);
        manager.getSubTaskById(subTask2Id);
        manager.getEpicById(epic1Id); // Эпик тоже в истории

        // Проверяем, что подзадачи в истории
        List<Task> historyBefore = manager.getHistory();
        assertEquals(3, historyBefore.size(), "В истории должно быть 3 задачи перед удалением");

        // Удаляем все подзадачи
        manager.deleteAllSubTasks();

        // Проверяем, что подзадачи удалены из истории, но эпик остался
        List<Task> historyAfter = manager.getHistory();
        assertEquals(1, historyAfter.size(), "В истории должен остаться только эпик");
        assertEquals(epic1Id, historyAfter.get(0).getId(), "В истории должен остаться эпик");
    }

    @Test
    void deleteAllEpics_shouldRemoveEpicsAndSubTasksFromHistory() {
        // Добавляем все задачи в историю
        manager.getEpicById(epic1Id);
        manager.getEpicById(epic2Id);
        manager.getSubTaskById(subTask2Id);
        manager.getSubTaskById(subTask1Id);
        manager.getTaskById(task1Id);
        manager.getTaskById(task2Id);

        // Проверяем, что все задачи в истории
        List<Task> historyBefore = manager.getHistory();
        assertEquals(6, historyBefore.size(), "В истории должно быть 6 задач перед удалением");

        // Удаляем все эпики
        manager.deleteAllEpics();

        // Проверяем, что эпики и подзадачи удалены из истории
        List<Task> historyAfter = manager.getHistory();
        assertEquals(2, historyAfter.size(), "В истории должно быть 2 задач после удаления");
        assertTrue(historyAfter.stream().allMatch(t -> t.getType() == Type.TASK), "Задача должна остаться в истории");
    }

    @Test
    void testSubTaskHasValidEpic() {
        // Проверяем, что у подзадачи есть валидный родительский эпик
        assertDoesNotThrow(() -> manager.getEpicById(subTask1.getParentEpicId()));
        assertEquals(epic1.getId(), subTask1.getParentEpicId());
    }

    @Test
    void testAddSubTaskToNonExistentEpic() {
        // Попытка добавить подзадачу к несуществующему эпику
        SubTask invalidSubTask = new SubTask("Invalid SubTask", "Description", 999);

        assertThrows(Exception.class, () -> manager.addNewSubTask(invalidSubTask));
    }
}
