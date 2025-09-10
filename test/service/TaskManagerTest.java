package service;

import model.Epic;
import model.SubTask;
import model.Task;
import model.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
        task1.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        task1.setDuration(Duration.ofHours(1));
        task1Id = manager.addNewTask(task1);

        task2 = new Task("Простая задача2", "Описание простой задачи 2");
        task2.setStartTime(LocalDateTime.of(2023, 1, 1, 11, 0));
        task2.setDuration(Duration.ofHours(1));
        task2Id = manager.addNewTask(task2);

        epic1 = new Epic("Важный эпик1", "Описание эпика 1");
        epic2 = new Epic("Важный эпик2", "Описание эпика 2");
        epic1Id = manager.addNewEpic(epic1);
        epic2Id = manager.addNewEpic(epic2);

        subTask1 = new SubTask("Подзадача 1", "описание подзадачи1", epic1Id);
        subTask1.setStartTime(LocalDateTime.of(2023, 1, 1, 9, 0));
        subTask1.setDuration(Duration.ofHours(1));
        subTask1Id = manager.addNewSubTask(subTask1);

        subTask2 = new SubTask("Подзадача 2", "описание подзадачи2", epic1Id);
        subTask2.setStartTime(LocalDateTime.of(2023, 1, 1, 13, 0));
        subTask2.setDuration(Duration.ofHours(2));
        subTask2Id = manager.addNewSubTask(subTask2);
    }

    @Test
    public void shouldFindTask2ByTask2Id() {
        assertTrue(manager.getTaskById(task2Id).isPresent());
        assertEquals(task2, manager.getTaskById(task2Id).get());
    }

    @Test
    public void shouldFindEpic1ByEpic1Id() {
        assertTrue(manager.getEpicById(epic1Id).isPresent());
        assertEquals(epic1, manager.getEpicById(epic1Id).get());
    }

    @Test
    public void shouldFindSubTask1BySubTask1Id() {
        assertTrue(manager.getSubTaskById(subTask1Id).isPresent());
        assertEquals(subTask1, manager.getSubTaskById(subTask1Id).get());
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
    public void shouldBeTheSameTaskIfIDsMatch() {
        Optional<Task> savedTaskOptional = manager.getTaskById(task1Id);
        assertTrue(savedTaskOptional.isPresent());
        Task savedTask = savedTaskOptional.get();
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task1, savedTask, "Задачи не совпадают.");
    }

    @Test
    public void shouldBeTheSameEpicIfIDsMatch() {
        assertTrue(manager.getEpicById(epic1Id).isPresent());
        Task savedEpic = manager.getEpicById(epic1Id).get();
        assertNotNull(savedEpic, "Задача не найдена.");
        assertEquals(epic1, savedEpic, "Задачи не совпадают.");
    }

    @Test
    public void shouldBeTheSameSubTaskIfIDsMatch() {
        assertTrue(manager.getSubTaskById(subTask1Id).isPresent());
        Task savedSubTask = manager.getSubTaskById(subTask1Id).get();
        assertNotNull(savedSubTask, "Задача не найдена.");
        assertEquals(subTask1, savedSubTask, "Задачи не совпадают.");
    }

    @Test
    public void taskShouldRemainUnmodifiedAfterAddingToManager() {
        String title = "title";
        String description = "description";
        Task newTask = new Task(title, description);
        int id = manager.addNewTask(newTask);

        Optional<Task> loadedTaskOptional = manager.getTaskById(id);
        assertTrue(loadedTaskOptional.isPresent());
        Task loadedTask = loadedTaskOptional.get();

        assertEquals(title, loadedTask.getTitle());
        assertEquals(description, loadedTask.getDescription());
    }

    @Test
    void getTaskByIdShouldReturnCorrectOptionalTask() {
        Optional<Task> actualTaskOptional = manager.getTaskById(task1Id);
        assertTrue(actualTaskOptional.isPresent());
        Task actualTask = actualTaskOptional.get();

        assertEquals(task1, actualTask, "Метод вернул не ту задачу");
    }

    @Test
    void getTaskById_ShouldReturnEmptyOptionalWhenTaskDoesNotExist() {
        assertTrue(manager.getTaskById(-11).isEmpty(), "Optional должен быть пустым у несуществующей задачи");
    }

    @Test
    void getEpicByIdShouldReturnEpic() {
        assertTrue(manager.getEpicById(epic1Id).isPresent());
        Epic actualEpic = manager.getEpicById(epic1Id).get();
        assertEquals(epic1, actualEpic, "Метод вернул не ту задачу");
    }

    @Test
    void getEpicById_ShouldReturnEmptyOptionalWhenEpicDoesNotExist() {
        assertTrue(manager.getEpicById(-11).isEmpty());
    }

    @Test
    void getSubTaskById_ShouldReturnSubTask() {
        assertTrue(manager.getSubTaskById(subTask1Id).isPresent());
        SubTask actualSubTask = manager.getSubTaskById(subTask1Id).get();
        assertEquals(subTask1, actualSubTask, "Метод вернул не ту задачу");
    }

    @Test
    void getSubTaskById_ShouldReturnEmptyOptionalWhenTaskDoesNotExist() {
        assertTrue(manager.getSubTaskById(-11).isEmpty());
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
        assertEquals(epic1Id, historyAfter.getFirst().getId(), "В истории должен остаться эпик");
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

    @Test
    void deleteAllEpics_shouldRemoveAllSubtasksFromPrioritizedTasks() {
        // Проверяем, что подзадачи добавились в prioritizedTasks
        List<Task> prioritizedBefore = manager.getPrioritizedTasks();
        assertEquals(4, prioritizedBefore.size(), "Изначально должно быть в prioritizedTasks 4 задачи");

        // Удаляем все эпики
        manager.deleteAllEpics();

        // Проверяем, что подзадачи удалились из prioritizedTasks
        List<Task> prioritizedAfter = manager.getPrioritizedTasks();
        assertEquals(2, prioritizedAfter.size(), "После удаления должно остаться 2 задачи");
        assertFalse(prioritizedAfter.stream().anyMatch(task -> task.getType() == Type.SUBTASK),
                "Все подзадачи должны быть удалены из prioritizedTasks");
    }

    @Test
    void GetPrioritizedTasks_ShouldReturnTasksInCorrectOrder() {
        manager.addNewTask(new Task("Пустая задача", "Пустое описание")); // задача без времени дополнительно к 4 уже добавленным

        List<Task> prioritizedTasks = manager.getPrioritizedTasks();

        assertEquals(4, prioritizedTasks.size(), "Должно быть 4 приоритетных задачи");
        assertEquals(subTask1, prioritizedTasks.get(0), "Первая задача должна быть SubTask1 (раньше по времени)");
        assertEquals(task1, prioritizedTasks.get(1), "Вторая задача должна быть task1");
        assertEquals(task2, prioritizedTasks.get(2), "Третяя задача должна быть task2");
        assertEquals(subTask2, prioritizedTasks.get(3), "Четвертая задача должна быть SubTask2");
    }

    @Test
    void GetPrioritizedTasks_ShouldReturnSubTasksInCorrectOrder() {
        List<Task> prioritizedTasks = manager.getPrioritizedTasks();

        assertEquals(4, prioritizedTasks.size(), "Должно быть 2 приоритетных подзадачи");
        assertEquals(subTask1, prioritizedTasks.get(0), "Первая подзадача должна быть subTask1 (раньше по времени)");
        assertEquals(subTask2, prioritizedTasks.get(3), "Вторая подзадача должна быть subTask2");
    }

    @Test
    void GetPrioritizedTasks_ShouldReturnMixedTasksInCorrectOrder() {
        manager.addNewTask(new Task("Пустая задача", "Пустое описание")); // задача без времени
        manager.updateTask(task1);
        manager.updateTask(task2);
        manager.updateSubTask(subTask1);
        manager.updateSubTask(subTask2);

        List<Task> prioritizedTasks = manager.getPrioritizedTasks();

        assertEquals(4, prioritizedTasks.size(), "Должно быть 4 приоритетных задачи");

        assertEquals(subTask1, prioritizedTasks.get(0)); // 9:00
        assertEquals(task1, prioritizedTasks.get(1));    // 10:00
        assertEquals(task2, prioritizedTasks.get(2));    // 11:00
        assertEquals(subTask2, prioritizedTasks.get(3)); // 13:00
    }

    @Test
    void GetPrioritizedTasks_ShouldReturnCorrectTasksWhenDeleteTaskAndSubTask() {
        manager.updateTask(task1);
        manager.updateTask(task2);
        manager.updateSubTask(subTask1);
        manager.updateSubTask(subTask2);

        manager.deleteTask(task1Id);
        manager.deleteSubTask(subTask1Id);

        List<Task> prioritizedTasks = manager.getPrioritizedTasks();

        assertEquals(2, prioritizedTasks.size(), "Должна остаться 3 приоритетная задача");
        assertTrue(prioritizedTasks.contains(task2), "Оставшаяся задача должна быть task2");
        assertTrue(prioritizedTasks.contains(subTask2), "Оставшаяся задача должна быть task2");
    }
}
