package service;

import exeptions.TimeIntersectionException;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    private Task taskA;
    private Task taskB;

    @Override
    protected InMemoryTaskManager getManager() {
        return new InMemoryTaskManager();
    }

    @BeforeEach
    public void beforeEachInMemoryTaskManager() {
        taskA = new Task("Task 1", "Description 1");
        taskB = new Task("Task 2", "Description 2");

        task1.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        task1.setDuration(Duration.ofHours(1));

        task2.setStartTime(LocalDateTime.of(2023, 1, 1, 11, 0));
        task2.setDuration(Duration.ofHours(1));

        subTask1.setStartTime(LocalDateTime.of(2023, 1, 1, 9, 0));
        subTask1.setDuration(Duration.ofHours(1));

        subTask2.setStartTime(LocalDateTime.of(2023, 1, 1, 13, 0));
        subTask2.setDuration(Duration.ofHours(2));
    }

    @Test
    void isOverlap_shouldReturnTrueWhenPartialOverlap() {
        taskA.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        taskA.setDuration(Duration.ofMinutes(30));

        taskB.setStartTime(LocalDateTime.of(2023, 1, 1, 9, 45));
        taskB.setDuration(Duration.ofMinutes(30));

        assertTrue(manager.isOverlap(taskA, taskB),
                "Должно быть пересечение при частичном пересечении");
        assertTrue(manager.isOverlap(taskB, taskA),
                "Должно быть пересечение при частичном пересечении");
    }

    @Test
    void isOverlap_shouldReturnFalseWhenEitherTaskHasNoTime() {
        Task taskWithTime = new Task("Task 1", "Description 1");
        taskWithTime.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        taskWithTime.setDuration(Duration.ofMinutes(30));

        assertFalse(manager.isOverlap(taskWithTime, taskA),
                "Не должно быть пересечения, если у второй задачи нет времени");

        assertFalse(manager.isOverlap(taskA, taskWithTime),
                "Не должно быть пересечения, если у первой задачи нет времени");

        assertFalse(manager.isOverlap(taskA, taskB),
                "Не должно быть пересечения, если у обеих задач нет времени");
    }

    @Test
    void isOverlap_shouldReturnTrueWhenTimeExactOverlap() {
        // Обе задачи: 10:00-10:30
        taskA.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        taskA.setDuration(Duration.ofMinutes(30));

        taskB.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        taskB.setDuration(Duration.ofMinutes(30));

        assertTrue(manager.isOverlap(taskA, taskB),
                "Должно быть пересечение при точном совпадении времени");
    }

    @Test
    void isOverlap_shouldReturnTrueWhenTaskCompletelyInsideAnother() {
        taskA.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        taskA.setDuration(Duration.ofMinutes(60));

        taskB.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 10));
        taskB.setDuration(Duration.ofMinutes(30));

        assertTrue(manager.isOverlap(taskA, taskB),
                "Должно быть пересечение когда одна задача полностью внутри другой по времени");
        assertTrue(manager.isOverlap(taskB, taskA),
                "Должно быть пересечение когда одна задача полностью внутри другой по времени");
    }

    @Test
    void isOverlap_shouldReturnTrueWhenWhenTasksAreSeparate() {
        taskA.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        taskA.setDuration(Duration.ofMinutes(60));

        taskB.setStartTime(LocalDateTime.of(2023, 1, 1, 11, 0));
        taskB.setDuration(Duration.ofMinutes(30));

        assertFalse(manager.isOverlap(taskA, taskB),
                "Не должно быть пересечения, когда одна задача идет за другой");
        assertFalse(manager.isOverlap(taskB, taskA),
                "Не должно быть пересечения, когда одна задача идет за другой");
    }

    @Test
    void isOverlap_shouldReturnFalseWhenTasksAreOnDifferentDays() {
        taskA.setStartTime(LocalDateTime.of(2023, 1, 5, 10, 0));
        taskA.setDuration(Duration.ofMinutes(30));

        taskB.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        taskB.setDuration(Duration.ofMinutes(30));

        assertFalse(manager.isOverlap(taskA, taskB),
                "Не должно быть пересечения при задачах в разные дни");
        assertFalse(manager.isOverlap(taskB, taskA),
                "Не должно быть пересечения при задачах в разные дни");
    }

    @Test
    void isOverlap_shouldReturnTrueWhenTasksAreOnDifferentDaysButWithLongDuration() {
        taskA.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        taskA.setDuration(Duration.ofHours(25));

        taskB.setStartTime(LocalDateTime.of(2023, 1, 2, 10, 0));
        taskB.setDuration(Duration.ofMinutes(30));

        assertTrue(manager.isOverlap(taskA, taskB),
                "Должно быть пересечение при задачах в разные дни но с большой длительностью");
        assertTrue(manager.isOverlap(taskB, taskA),
                "Должно быть пересечение при задачах в разные дни но с большой длительностью");
    }

    @Test
    void isOverlap_shouldReturnFalseWhenTasksAreOnSameTimeWithZeroDuration() {
        taskA.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0)); //Duration = 0

        taskB.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0)); //Duration = 0

        assertFalse(manager.isOverlap(taskA, taskB),
                "Не должно быть пересечения при задачах в разные дни");
        assertFalse(manager.isOverlap(taskB, taskA),
                "Не должно быть пересечения при задачах в разные дни");
    }

    @Test
    void GetPrioritizedTasks_ShouldReturnTasksInCorrectOrder() {
        // Добавляем задачи
        manager.addNewTask(taskA); // задача без времени
        manager.updateTask(task1);
        manager.updateTask(task2);

        List<Task> prioritizedTasks = manager.getPrioritizedTasks();

        assertEquals(2, prioritizedTasks.size(), "Должно быть 2 приоритетных задачи");
        assertEquals(task1, prioritizedTasks.get(0), "Первая задача должна быть task1 (раньше по времени)");
        assertEquals(task2, prioritizedTasks.get(1), "Вторая задача должна быть task2");
    }

    @Test
    void GetPrioritizedTasks_ShouldReturnSubTasksInCorrectOrder() {
        manager.updateSubTask(subTask1);
        manager.updateSubTask(subTask2);

        List<Task> prioritizedTasks = manager.getPrioritizedTasks();

        assertEquals(2, prioritizedTasks.size(), "Должно быть 2 приоритетных подзадачи");
        assertEquals(subTask1, prioritizedTasks.get(0), "Первая подзадача должна быть subTask1 (раньше по времени)");
        assertEquals(subTask2, prioritizedTasks.get(1), "Вторая подзадача должна быть subTask2");
    }

    @Test
    void GetPrioritizedTasks_ShouldReturnMixedTasksInCorrectOrder() {
        manager.addNewTask(taskA); // задача без времени
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
    void HasTimeConflict_ShouldReturnTrueWhenTimeWithConflict() {
        manager.updateTask(task1);
        manager.updateTask(task2);
        manager.updateSubTask(subTask1);
        manager.updateSubTask(subTask2);

        // Создаем задачу, которая пересекается по времени
        Task conflictingTask = new Task("Conflict", "Description");
        conflictingTask.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 30)); // Начинается во время выполнения task1
        conflictingTask.setDuration(Duration.ofHours(1));

        assertTrue(manager.hasTimeConflict(conflictingTask), "Должен быть конфликт времени");
    }

    @Test
    void HasTimeConflict_ShouldReturnTrueWhenTimeWithConflict1() {
        manager.updateTask(task1);
        manager.updateTask(task2);
        manager.updateSubTask(subTask1);
        manager.updateSubTask(subTask2);

        // Создаем задачу, которая пересекается по времени
        Task conflictingTask = new Task("Conflict", "Description");
        conflictingTask.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 30)); // Начинается во время выполнения task1
        conflictingTask.setDuration(Duration.ofHours(1));

        assertThrows(TimeIntersectionException.class,
                () -> manager.addNewTask(conflictingTask),
                "Должно выброситься исключение при попытке добавить конфликтную задачу");


    }


}