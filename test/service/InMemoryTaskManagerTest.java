package service;

import exeptions.TimeIntersectionException;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    private Task taskA;
    private Task taskB;
    private Method isOverlapMethod;
    private Method hasTimeConflictMethod;

    @Override
    protected InMemoryTaskManager getManager() {
        return new InMemoryTaskManager();
    }

    @BeforeEach
    public void beforeEachInMemoryTaskManager() throws NoSuchMethodException {
        taskA = new Task("Task 1", "Description 1");
        taskB = new Task("Task 2", "Description 2");

        isOverlapMethod = InMemoryTaskManager.class.getDeclaredMethod("isOverlap", Task.class, Task.class);
        isOverlapMethod.setAccessible(true);

        hasTimeConflictMethod = InMemoryTaskManager.class.getDeclaredMethod("hasTimeConflict", Task.class);
        hasTimeConflictMethod.setAccessible(true);
    }

    private boolean invokeIsOverlap(Task task1, Task task2) throws Exception {
        return (Boolean) isOverlapMethod.invoke(manager, task1, task2);
    }

    @Test
    void isOverlap_shouldReturnTrueWhenPartialOverlap() throws Exception {
        taskA.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        taskA.setDuration(Duration.ofMinutes(30));

        taskB.setStartTime(LocalDateTime.of(2023, 1, 1, 9, 45));
        taskB.setDuration(Duration.ofMinutes(30));

        assertTrue(invokeIsOverlap(taskA, taskB),
                "Должно быть пересечение при частичном пересечении");
        assertTrue(invokeIsOverlap(taskB, taskA),
                "Должно быть пересечение при частичном пересечении");
    }

    @Test
    void isOverlap_shouldReturnFalseWhenEitherTaskHasNoTime() throws Exception {
        Task taskWithTime = new Task("Task 1", "Description 1");
        taskWithTime.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        taskWithTime.setDuration(Duration.ofMinutes(30));

        assertFalse(invokeIsOverlap(taskWithTime, taskA),
                "Не должно быть пересечения, если у второй задачи нет времени");

        assertFalse(invokeIsOverlap(taskA, taskWithTime),
                "Не должно быть пересечения, если у первой задачи нет времени");

        assertFalse(invokeIsOverlap(taskA, taskB),
                "Не должно быть пересечения, если у обеих задач нет времени");
    }

    @Test
    void isOverlap_shouldReturnTrueWhenTimeExactOverlap() throws Exception {
        // Обе задачи: 10:00-10:30
        taskA.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        taskA.setDuration(Duration.ofMinutes(30));

        taskB.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        taskB.setDuration(Duration.ofMinutes(30));

        assertTrue(invokeIsOverlap(taskA, taskB),
                "Должно быть пересечение при точном совпадении времени");
    }

    @Test
    void isOverlap_shouldReturnTrueWhenTaskCompletelyInsideAnother() throws Exception {
        taskA.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        taskA.setDuration(Duration.ofMinutes(60));

        taskB.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 10));
        taskB.setDuration(Duration.ofMinutes(30));

        assertTrue(invokeIsOverlap(taskA, taskB),
                "Должно быть пересечение когда одна задача полностью внутри другой по времени");
        assertTrue(invokeIsOverlap(taskB, taskA),
                "Должно быть пересечение когда одна задача полностью внутри другой по времени");
    }

    @Test
    void isOverlap_shouldReturnTrueWhenWhenTasksAreSeparate() throws Exception {
        taskA.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        taskA.setDuration(Duration.ofMinutes(60));

        taskB.setStartTime(LocalDateTime.of(2023, 1, 1, 11, 0));
        taskB.setDuration(Duration.ofMinutes(30));

        assertFalse(invokeIsOverlap(taskA, taskB),
                "Не должно быть пересечения, когда одна задача идет за другой");
        assertFalse(invokeIsOverlap(taskB, taskA),
                "Не должно быть пересечения, когда одна задача идет за другой");
    }

    @Test
    void isOverlap_shouldReturnFalseWhenTasksAreOnDifferentDays() throws Exception {
        taskA.setStartTime(LocalDateTime.of(2023, 1, 5, 10, 0));
        taskA.setDuration(Duration.ofMinutes(30));

        taskB.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        taskB.setDuration(Duration.ofMinutes(30));

        assertFalse(invokeIsOverlap(taskA, taskB),
                "Не должно быть пересечения при задачах в разные дни");
        assertFalse(invokeIsOverlap(taskB, taskA),
                "Не должно быть пересечения при задачах в разные дни");
    }

    @Test
    void isOverlap_shouldReturnTrueWhenTasksAreOnDifferentDaysButWithLongDuration() throws Exception {
        taskA.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        taskA.setDuration(Duration.ofHours(25));

        taskB.setStartTime(LocalDateTime.of(2023, 1, 2, 10, 0));
        taskB.setDuration(Duration.ofMinutes(30));

        assertTrue(invokeIsOverlap(taskA, taskB),
                "Должно быть пересечение при задачах в разные дни но с большой длительностью");
        assertTrue(invokeIsOverlap(taskB, taskA),
                "Должно быть пересечение при задачах в разные дни но с большой длительностью");
    }

    @Test
    void isOverlap_shouldReturnFalseWhenTasksAreOnSameTimeWithZeroDuration() throws Exception {
        taskA.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0)); //Duration = 0

        taskB.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0)); //Duration = 0

        assertFalse(invokeIsOverlap(taskA, taskB),
                "Не должно быть пересечения при задачах в разные дни");
        assertFalse(invokeIsOverlap(taskB, taskA),
                "Не должно быть пересечения при задачах в разные дни");
    }

    @Test
    void HasTimeConflict_ShouldReturnTrueWhenTimeWithConflict() throws InvocationTargetException, IllegalAccessException {
        // Создаем задачу, которая пересекается по времени
        Task conflictingTask = new Task("Conflict", "Description");
        conflictingTask.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 30)); // Начинается во время выполнения task1
        conflictingTask.setDuration(Duration.ofHours(1));

        assertTrue((boolean) hasTimeConflictMethod.invoke(manager, conflictingTask), "Должен быть конфликт времени");
    }

    @Test
    void HasTimeConflict_ShouldReturnTrueWhenTimeWithConflict1() {
        // Создаем задачу, которая пересекается по времени
        Task conflictingTask = new Task("Conflict", "Description");
        conflictingTask.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 30)); // Начинается во время выполнения task1
        conflictingTask.setDuration(Duration.ofHours(1));

        assertThrows(TimeIntersectionException.class,
                () -> manager.addNewTask(conflictingTask),
                "Должно выброситься исключение при попытке добавить конфликтную задачу");
    }


}