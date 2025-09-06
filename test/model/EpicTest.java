package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;
import service.TaskManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private static TaskManager manager;
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

        // Создаём 2 эпика. У первого будет 2 подзадачи. Второй пустой.
        epic1 = new Epic("Важный эпик1", "описние эпика 1");
        epic2 = new Epic("Важный эпик2", "описние эпика 2");
        epic1Id = manager.addAnyTypeTask(epic1);
        epic2Id = manager.addAnyTypeTask(epic2);

        subTask1 = new SubTask("Подзадача 1", "описание подзадачи1", epic1Id);
        subTask1Id = manager.addAnyTypeTask(subTask1);
        subTask2 = new SubTask("Подзадача 2", "описание подзадачи2", epic1Id);
        subTask2Id = manager.addAnyTypeTask(subTask2);
    }

    @Test
    public void statusOfNewEpicShouldBeNEW() {
        assertEquals(Status.NEW, epic2.getStatus());
    }

    @Test
    public void statusOfNewEpicWithTwoSubTaskShouldBeNEW() {
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
        manager.updateTask(subTask1);
        assertEquals(Status.IN_PROGRESS, epic1.getStatus());
    }

    @Test
    public void shouldBeEpicStatusDONEWhenAllSubTaskIsDONE() {
        subTask1.setStatus(Status.DONE);
        subTask2.setStatus(Status.DONE);
        manager.updateTask(subTask1);
        manager.updateTask(subTask2);
        assertEquals(Status.DONE, epic1.getStatus());
    }

    @Test
    public void shouldBeEpicStatusNEWWhenDeleteAllSubTask() {
        epic1.deleteSubTaskFromEpic(subTask1Id);
        epic1.deleteSubTaskFromEpic(subTask2Id);
        assertEquals(Status.NEW, epic1.getStatus());
    }

    @Test
    public void shouldDeleteSubTaskFromEpic() {
        assertEquals(2, epic1.getEpicSubTasks().size());
        assertTrue(epic1.getEpicSubTasks().contains(subTask1));
        assertTrue(epic1.getEpicSubTasks().contains(subTask2));

        epic1.deleteSubTaskFromEpic(subTask1Id);
        epic1.deleteSubTaskFromEpic(subTask2Id);

        assertEquals(0, epic1.getEpicSubTasks().size());
        assertFalse(epic1.getEpicSubTasks().contains(subTask1));
        assertFalse(epic1.getEpicSubTasks().contains(subTask2));
    }

    @Test
    public void shouldAddSubTaskToEpic() {
        assertFalse(epic2.getEpicSubTasks().contains(subTask1));
        epic2.addSubTaskToEpic(subTask1);
        assertTrue(epic2.getEpicSubTasks().contains(subTask1));
    }

    @Test
    void calculateDuration_shouldReturnZeroWhenNoSubTasks() {
        epic1.checkEpicState(); // тест через публичный метод, внутри вызывается calculateDuration()

        assertNull(epic1.getDuration());
    }

    @Test
    void calculateDuration_shouldReturnSumOfSubTasksDurations() {
        subTask1.setDuration(Duration.ofHours(2));
        subTask2.setDuration(Duration.ofMinutes(90));

        epic1.checkEpicState(); // тест через публичный метод, внутри вызывается calculateDuration()

        assertNotNull(epic1.getDuration());
        assertEquals(Duration.ofMinutes(210), epic1.getDuration());
        assertEquals(3, epic1.getDuration().toHours());
        assertEquals(30, epic1.getDuration().toMinutesPart());
    }

    @Test
    void calculateDuration_shouldHandleNullDurations() {
        subTask1.setDuration(Duration.ofHours(2));
        subTask2.setDuration(Duration.ofMinutes(90));
        SubTask subTask3 = new SubTask("SubTask 3", "Description 3", epic1Id);
        // duration is null

        epic1.addSubTaskToEpic(subTask3);

        // Act
        epic1.checkEpicState(); // тест через публичный метод, внутри вызывается calculateDuration()

        // Assert
        assertNotNull(epic1.getDuration());
        assertEquals(Duration.ofMinutes(210), epic1.getDuration());
    }

    @Test
    void calculateDuration_shouldHandleOnlyNullDurations() {
        epic1.checkEpicState(); // тест через публичный метод, внутри вызывается calculateDuration()

        assertNull(epic1.getDuration());
    }

    @Test
    void evaluateStartTime_shouldReturnNullWhenNoSubTasks() {
        Epic epic = new Epic("Test Epic", "Test Description");
        manager.addAnyTypeTask(epic);
        epic.checkEpicState();  // тест через публичный метод, внутри вызывается evaluateStartTime()

        assertNull(epic.getStartTime());
    }

    @Test
    void evaluateStartTime_shouldReturnEarliestStartTime() {
        SubTask subTask3 = new SubTask("SubTask 3", "Description 3", epic1Id);
        LocalDateTime earlyTime = LocalDateTime.of(2024, 1, 10, 9, 0);
        LocalDateTime middleTime = LocalDateTime.of(2024, 1, 10, 12, 0);
        LocalDateTime lateTime = LocalDateTime.of(2024, 1, 10, 15, 0);

        subTask1.setStartTime(middleTime);
        subTask2.setStartTime(earlyTime);
        subTask3.setStartTime(lateTime);

        epic1.addSubTaskToEpic(subTask3);

        epic1.checkEpicState();  // тест через публичный метод, внутри вызывается evaluateStartTime()

        assertNotNull(epic1.getStartTime());
        assertEquals(earlyTime, epic1.getStartTime());
    }

    @Test
    void evaluateStartTime_shouldReturnNullWhenAllStartTimesAreNull() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
//        epic1.checkEpicState();  // тест через публичный метод, внутри вызывается evaluateStartTime()
        Method calculateDurationMethod = Epic.class.getDeclaredMethod("calculateDuration");
        calculateDurationMethod.setAccessible(true);
        calculateDurationMethod.invoke(epic1);


        assertNull(epic1.getStartTime());
    }


}