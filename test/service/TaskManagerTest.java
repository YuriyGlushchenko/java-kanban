package service;

import exeptions.TimeIntersectionException;
import model.*;
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
        task1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        task1.setDuration(Duration.ofHours(1));
        task1Id = manager.addNewTask(task1);

        task2 = new Task("Простая задача2", "Описание простой задачи 2");
        task2.setStartTime(LocalDateTime.of(2025, 1, 1, 11, 0));
        task2.setDuration(Duration.ofHours(1));
        task2Id = manager.addNewTask(task2);

        epic1 = new Epic("Важный эпик1", "Описание эпика 1");
        epic2 = new Epic("Важный эпик2", "Описание эпика 2");
        epic1Id = manager.addNewEpic(epic1);
        epic2Id = manager.addNewEpic(epic2);

        subTask1 = new SubTask("Подзадача 1", "описание подзадачи1", epic1Id);
        subTask1.setStartTime(LocalDateTime.of(2025, 1, 1, 9, 0));
        subTask1.setDuration(Duration.ofHours(1));
        subTask1Id = manager.addNewSubTask(subTask1);

        subTask2 = new SubTask("Подзадача 2", "описание подзадачи2", epic1Id);
        subTask2.setStartTime(LocalDateTime.of(2025, 1, 1, 13, 0));
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

    @Test
    void EpicDurationShouldBeSetToZeroDurationWhenNoSubTasks() {
        assertEquals(Duration.ZERO, epic2.getDuration());
    }

    @Test
    void EpicDurationShouldBeSetToSumOfSubTasksDurations() {
        assertNotNull(epic1.getDuration());
        assertEquals(0, epic1.getDuration().toMinutesPart(), "у Эпик1 длительность должна быть 3 часа 0 минут");
        assertEquals(3, epic1.getDuration().toHours(), "у Эпик1 длительность должна быть 3 часа 0 минут");
    }

    @Test
    void epicDurationShouldBeCorrectlySetWhenSubTaskHasNullDurations() {
        SubTask subTask3 = new SubTask("SubTask 3", "Description 3", epic1.getId()); // duration == null
        epic1.addSubTaskToEpic(subTask3);

        assertNotNull(epic1.getDuration());
        assertEquals(Duration.ofMinutes(180), epic1.getDuration());
    }

    @Test
    void epicStartTimeShouldBeSetToEmptyOptionalWhenNoSubTasks() {
        assertTrue(epic2.getStartTime().isEmpty(), "У эпика без SubTask время начала должно быть null (EmptyOptional)");
    }

    @Test
    void epicStartTimeShouldBeSetToEarliestStartTime() {
        LocalDateTime earlyTime = LocalDateTime.of(2024, 1, 10, 9, 0);

        SubTask subTask3 = new SubTask("SubTask 3", "Description 3", epic1.getId());
        subTask3.setStartTime(earlyTime);
        manager.addNewSubTask(subTask3);

        assertTrue(epic1.getStartTime().isPresent());
        assertEquals(earlyTime, epic1.getStartTime().get());
    }

    @Test
    void epicStartTimeShouldBeSetToEmptyOptionalWhenAllStartTimesAreNull() {
        SubTask subTask3 = new SubTask("SubTask 3", "Description 3", epic2.getId());

        manager.addNewSubTask(subTask3);

        SubTask subTask4 = new SubTask("SubTask 4", "Description 4", epic2.getId());
        manager.addNewSubTask(subTask4);

        assertTrue(epic2.getStartTime().isEmpty());
    }

    @Test
    void epicEndTimeShouldBeSetToToNullWhenNoSubTasks() {
        assertNull(epic2.getEndTime());
    }

    @Test
    void epicEndTimeShouldBeSetToLatestSubtaskEndTime() {
        // Подзадача 3: Заканчивается первой
        SubTask subTask3 = new SubTask("SubTask 3", "Description 3", epic2.getId());
        LocalDateTime start3 = LocalDateTime.of(2023, 6, 1, 9, 0); // 9:00
        Duration duration3 = Duration.ofHours(1); // Длительность 1 час
        subTask3.setStartTime(start3);
        subTask3.setDuration(duration3);
        manager.addNewSubTask(subTask3);

        // Подзадача 4: Заканчивается последней
        SubTask subTask4 = new SubTask("SubTask 4", "Description 4", epic2.getId());
        LocalDateTime start4 = LocalDateTime.of(2023, 6, 3, 11, 0); // 11:00
        Duration duration4 = Duration.ofHours(3); // Длительность 3 часа
        LocalDateTime expectedEndTime = start4.plus(duration4); // Окончание в 14:00 <- Ожидаемый endTime эпика
        subTask4.setStartTime(start4);
        subTask4.setDuration(duration4);
        manager.addNewSubTask(subTask4);

        // Подзадача 5: Заканчивается посередине
        SubTask subTask5 = new SubTask("SubTask 5", "Description 5", epic2.getId());
        LocalDateTime start5 = LocalDateTime.of(2023, 6, 2, 13, 0); // 13:00
        Duration duration5 = Duration.ofMinutes(30); // Длительность 30 мин
        subTask5.setStartTime(start5);
        subTask5.setDuration(duration5);
        manager.addNewSubTask(subTask5);

        assertEquals(expectedEndTime,
                epic2.getEndTime(),
                "endTime эпика должен быть равен endTime самой поздней подзадачи (subTask4)");
    }

    @Test
    public void statusOfNewEpicShouldBeNEW() {
        assertEquals(Status.NEW, epic2.getStatus());
    }

    @Test
    public void statusOfNewEpicWithTwoNewSubTaskShouldBeNEW() {
        assertEquals(Status.NEW, epic1.getStatus());
    }

    @Test
    public void shouldNotChangeEpicStatusManually() {
        epic1.setStatus(Status.IN_PROGRESS);
        assertEquals(Status.NEW, epic1.getStatus());
    }

    @Test
    public void shouldBeEpicStatusIN_PROGRESSWhenSubTaskIsIN_PROGRESS() {
        subTask1.setStatus(Status.IN_PROGRESS);
        manager.updateSubTask(subTask1);
        assertEquals(Status.IN_PROGRESS, epic1.getStatus());
    }

    @Test
    public void shouldBeEpicStatusIN_PROGRESSWhenOnlyOneSubTaskIsDone() {
        subTask1.setStatus(Status.DONE);
        manager.updateSubTask(subTask1);
        assertEquals(Status.IN_PROGRESS, epic1.getStatus());
    }

    @Test
    public void shouldBeEpicStatusDONEWhenAllSubTaskIsDONE() {
        subTask1.setStatus(Status.DONE);
        subTask2.setStatus(Status.DONE);
        manager.updateSubTask(subTask1);
        manager.updateSubTask(subTask2);
        assertEquals(Status.DONE, epic1.getStatus());
    }

    @Test
    public void shouldBeEpicStatusNEWWhenDeleteAllSubTasksFromEpic() {
        epic1.deleteSubTaskFromEpic(subTask1.getId());
        epic1.deleteSubTaskFromEpic(subTask1.getId());
        assertEquals(Status.NEW, epic1.getStatus());
    }

    @Test
    public void shouldBeEpicStatusIN_PROGRESSWhenAllSubTaskIsIN_PROGRESS() {
        subTask1.setStatus(Status.IN_PROGRESS);
        subTask2.setStatus(Status.IN_PROGRESS);
        manager.updateSubTask(subTask1);
        manager.updateSubTask(subTask2);
        assertEquals(Status.IN_PROGRESS, epic1.getStatus());
    }

    @Test
    public void shouldBeEpicStatusNewWhenDeleteOneSubTaskIN_PROGRESSAndOneSubTaskNewRemain() {
        subTask1.setStatus(Status.IN_PROGRESS);
        manager.updateSubTask(subTask1);
        assertEquals(Status.IN_PROGRESS, epic1.getStatus());

        manager.deleteSubTask(subTask1.getId());

        assertEquals(Status.NEW, epic1.getStatus());
    }

    @Test
    public void shouldBeEpicStatusIN_PROGRESSWhenStatusWasDONEAndThenAddNewSubTask() {
        subTask1.setStatus(Status.DONE);
        subTask2.setStatus(Status.DONE);
        manager.updateSubTask(subTask1);
        manager.updateSubTask(subTask2);
        assertEquals(Status.DONE, epic1.getStatus());

        SubTask subTask3 = new SubTask("Подзадача 3", "описание подзадачи3", epic1.getId());
        subTask3.setStartTime(LocalDateTime.of(2020, 1, 1, 13, 0));
        subTask3.setDuration(Duration.ofHours(2));
        manager.addNewSubTask(subTask3);

        assertEquals(Status.IN_PROGRESS, epic1.getStatus());
    }

    @Test
    void shouldBeTimeConflictWhenAddTaskWithPartialOverlap() {
        Task taskA = new Task("Task 1", "Description 1");
        taskA.setStartTime(LocalDateTime.of(2025, 1, 1, 14, 0));
        taskA.setDuration(Duration.ofMinutes(180));

        assertThrows(TimeIntersectionException.class, () -> {
            manager.addNewTask(taskA);
        });
    }

    @Test
    void shouldNotBeConflictWhenAddTaskWithNoTime() {
        Task taskWithoutTime = new Task("Task 1", "Description 1");

        assertDoesNotThrow(() -> manager.addNewTask(taskWithoutTime),
                "Не должно быть пересечения, если у  задачи нет времени");
    }

    @Test
    void shouldBeTimeConflictBetweenTaskAndSubTaskWhenAddTaskWithExactOverlap() {
        Task taskA = new Task("Task 1", "Description 1");
        taskA.setStartTime(LocalDateTime.of(2025, 1, 1, 13, 0));
        taskA.setDuration(Duration.ofHours(2));

        assertThrows(TimeIntersectionException.class, () -> {
                    manager.addNewTask(taskA);
                },
                "Должен быть конфликт времени между task и subTask когда они точно совадают по времени");
    }

    @Test
    void shouldBeTimeConflictWhenTaskCompletelyInsideAnother() {
        Task taskA = new Task("Task 1", "Description 1");
        taskA.setStartTime(LocalDateTime.of(2025, 1, 1, 13, 30));
        taskA.setDuration(Duration.ofHours(1));

        assertThrows(TimeIntersectionException.class, () -> {
                    manager.addNewTask(taskA);
                },
                "Должен быть конфликт времени между task и subTask когда один внутри другого по времени");
    }

    @Test
    void shouldNotBeTimeConflictWhenTasksAreSeparate() {
        Task taskA = new Task("Task 1", "Description 1");
        taskA.setStartTime(LocalDateTime.of(2025, 1, 1, 20, 30));
        taskA.setDuration(Duration.ofHours(1));

        assertDoesNotThrow(() -> manager.addNewTask(taskA), "Не должно быть конфликтов у непересекающихся задач");
    }

    @Test
    void shouldNotBeTimeConflictWhenTasksAreOnDifferentDays() {
        Task taskA = new Task("Task 1", "Description 1");
        taskA.setStartTime(LocalDateTime.of(2025, 1, 2, 13, 0));
        taskA.setDuration(Duration.ofHours(2));

        assertDoesNotThrow(() -> manager.addNewTask(taskA), "Не должно быть конфликтов у задач в разные дни");
    }

    @Test
    void shouldNotBeTimeConflictWhenTasksAreOnDifferentDaysWithLongDuration() {
        Task taskA = new Task("Task 1", "Description 1");
        taskA.setStartTime(LocalDateTime.of(2024, 12, 31, 8, 0));
        taskA.setDuration(Duration.ofHours(25));

        assertDoesNotThrow(() -> manager.addNewTask(taskA),
                "Не должно быть конфликтов у задач в разные дни даже при большой длительности");
    }

    @Test
    void shouldNotBeTimeConflictWhenTasksAreOnSameTimeWithZeroDuration() {
        Task taskA = new Task("Task A", "Description A");
        taskA.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0)); //Duration = 0

        Task taskB = new Task("Task B", "Description B");
        taskB.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0)); //Duration = 0

        assertDoesNotThrow(() -> manager.addNewTask(taskA),
                "Не должно быть конфликтов у задач в одно время, но c нулевой длительностью");
        assertDoesNotThrow(() -> manager.addNewTask(taskB),
                "Не должно быть конфликтов у задач в одно время, но c нулевой длительностью");
    }

    @Test
    void shouldNotBeTimeConflictWhenDeleteTaskAndThenAddNewTaskWithTimeConflictWithDeletedTask() {
        Task taskA = new Task("Task A", "Description A");
        taskA.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        taskA.setDuration(Duration.ofHours(1));
        // изначально конфликт есть с task1
        assertThrows(TimeIntersectionException.class, () -> {
            manager.addNewTask(taskA);
        });

        manager.deleteTask(task1.getId()); // убираем одну из конфликтующих задач

        assertDoesNotThrow(() -> manager.addNewTask(taskA),
                "Не должно быть конфликтов у задачи после удаления конфликтной задачи");
    }

    @Test
    void shouldNotThrowWhenDeleteNotExistingTask() {
        assertDoesNotThrow(() -> manager.deleteTask(999),
                "Не должно быть ошибок, если пытаться удалять несуществующую задачу");
    }
}
