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
    private static Epic epic1;
    private static Epic epic2;

    private static SubTask subTask1;
    private static SubTask subTask2;

    @BeforeEach
    public void beforeEach() {
        // Создаём 2 эпика. У первого будет 2 подзадачи. Второй пустой.
        epic1 = new Epic("Важный эпик1", "описние эпика 1",1);
        epic2 = new Epic("Важный эпик2", "описние эпика 2",2);

        subTask1 = new SubTask("Подзадача 1", "описание подзадачи1", epic1.getId(), 3);
        subTask2 = new SubTask("Подзадача 2", "описание подзадачи2", epic1.getId(),4);

        epic1.addSubTaskToEpic(subTask1);
        epic1.addSubTaskToEpic(subTask2);
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
        epic1.checkEpicState();
        assertEquals(Status.IN_PROGRESS, epic1.getStatus());
    }

    @Test
    public void shouldBeEpicStatusIN_PROGRESSWhenOnlyOneSubTaskIsDone() {
        subTask1.setStatus(Status.DONE);
        epic1.checkEpicState();
        assertEquals(Status.IN_PROGRESS, epic1.getStatus());
    }

    @Test
    public void shouldBeEpicStatusDONEWhenAllSubTaskIsDONE() {
        subTask1.setStatus(Status.DONE);
        subTask2.setStatus(Status.DONE);
        epic1.checkEpicState();
        assertEquals(Status.DONE, epic1.getStatus());
    }

    @Test
    public void shouldBeEpicStatusNEWWhenDeleteAllSubTask() {
        epic1.deleteSubTaskFromEpic(subTask1.getId());
        epic1.deleteSubTaskFromEpic(subTask1.getId());
        epic1.checkEpicState();
        assertEquals(Status.NEW, epic1.getStatus());
    }

    @Test
    public void shouldBeEpicStatusIN_PROGRESSWhenAllSubTaskIsIN_PROGRESS() {
        subTask1.setStatus(Status.IN_PROGRESS);
        subTask2.setStatus(Status.IN_PROGRESS);
        epic1.checkEpicState();
        assertEquals(Status.IN_PROGRESS, epic1.getStatus());
    }

    @Test
    public void shouldDeleteSubTaskFromEpic() {
        assertEquals(2, epic1.getEpicSubTasks().size());
        assertTrue(epic1.getEpicSubTasks().contains(subTask1));
        assertTrue(epic1.getEpicSubTasks().contains(subTask2));

        epic1.deleteSubTaskFromEpic(subTask1.getId());
        epic1.deleteSubTaskFromEpic(subTask2.getId());

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

//    @Test
//    void calculateDuration_shouldReturnZeroDurationWhenNoSubTasks() {
//        epic1.checkEpicState(); // тест через публичный метод, внутри вызывается calculateDuration()
//
//        assertEquals(Duration.ZERO, epic1.getDuration());
//    }
//
//    @Test
//    void calculateDuration_shouldReturnSumOfSubTasksDurations() {
//        subTask1.setDuration(Duration.ofHours(2));
//        subTask2.setDuration(Duration.ofMinutes(90));
//
//        epic1.checkEpicState(); // тест через публичный метод, внутри вызывается calculateDuration()
//
//        assertNotNull(epic1.getDuration());
//        assertEquals(Duration.ofMinutes(210), epic1.getDuration());
//        assertEquals(3, epic1.getDuration().toHours());
//        assertEquals(30, epic1.getDuration().toMinutesPart());
//    }
//
//    @Test
//    void calculateDuration_shouldHandleNullDurations() {
//        subTask1.setDuration(Duration.ofHours(2));
//        subTask2.setDuration(Duration.ofMinutes(90));
//        SubTask subTask3 = new SubTask("SubTask 3", "Description 3", epic1Id);
//        // duration is null
//
//        epic1.addSubTaskToEpic(subTask3);
//
//        epic1.checkEpicState(); // тест через публичный метод, внутри вызывается calculateDuration()
//
//        assertNotNull(epic1.getDuration());
//        assertEquals(Duration.ofMinutes(210), epic1.getDuration());
//    }
//
//    @Test
//    void evaluateStartTime_shouldReturnEmptyOptionalWhenNoSubTasks() {
//        Epic epic = new Epic("Test Epic", "Test Description");
//        manager.addNewEpic(epic);
//        epic.checkEpicState();  // тест через публичный метод, внутри вызывается evaluateStartTime()
//
//        assertTrue(epic.getStartTime().isEmpty());
//    }
//
//    @Test
//    void evaluateStartTime_shouldReturnEarliestStartTime() {
//        SubTask subTask3 = new SubTask("SubTask 3", "Description 3", epic1Id);
//        LocalDateTime earlyTime = LocalDateTime.of(2024, 1, 10, 9, 0);
//        LocalDateTime middleTime = LocalDateTime.of(2024, 1, 10, 12, 0);
//        LocalDateTime lateTime = LocalDateTime.of(2024, 1, 10, 15, 0);
//
//        subTask1.setStartTime(middleTime);
//        subTask2.setStartTime(earlyTime);
//        subTask3.setStartTime(lateTime);
//
//        epic1.addSubTaskToEpic(subTask3);
//
//        epic1.checkEpicState();  // тест через публичный метод, внутри вызывается evaluateStartTime()
//
//        assertTrue(epic1.getStartTime().isPresent());
//        assertEquals(earlyTime, epic1.getStartTime().get());
//    }
//
//    @Test
//    void evaluateStartTime_shouldReturnEmptyOptionalWhenAllStartTimesAreNull() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
//        // тест через рефлексию
//        Method calculateDurationMethod = Epic.class.getDeclaredMethod("calculateDuration");
//        calculateDurationMethod.setAccessible(true);
//        calculateDurationMethod.invoke(epic1);
//
//
//        assertTrue(epic1.getStartTime().isEmpty());
//    }
//
//    @Test
//    void calculateEndTime_shouldSetEndTimeToNullWhenNoSubtasks() {
//        epic2.checkEpicState();
//
//        assertNull(epic2.getEndTime());
//    }
//
//    @Test
//    void calculateEndTime_shouldSetEndTimeToLatestSubtaskEndTime() {
//
//        // Подзадача 1: Начинается рано, но заканчивается не позже всех
//        LocalDateTime start1 = LocalDateTime.of(2024, 6, 1, 9, 0); // 9:00
//        Duration duration1 = Duration.ofHours(1); // Длительность 1 час
//        LocalDateTime end1 = start1.plus(duration1); // Окончание в 10:00
//
//        subTask1.setStartTime(start1);
//        subTask1.setDuration(duration1);
//
//        // Подзадача 2: Начинается позже, но заканчивается ПОЗЖЕ всех (самая поздняя)
//        LocalDateTime start2 = LocalDateTime.of(2024, 6, 1, 11, 0); // 11:00
//        Duration duration2 = Duration.ofHours(3); // Длительность 3 часа
//        LocalDateTime expectedEndTime = start2.plus(duration2); // Окончание в 14:00 <- Ожидаемый endTime эпика
//
//        subTask2.setStartTime(start2);
//        subTask2.setDuration(duration2);
//
//        // Подзадача 3: Начинается позже всех, но короткая
//        LocalDateTime start3 = LocalDateTime.of(2024, 6, 1, 13, 0); // 13:00
//        Duration duration3 = Duration.ofMinutes(30); // Длительность 30 мин
//        LocalDateTime end3 = start3.plus(duration3); // Окончание в 13:30
//        SubTask subTask3 = new SubTask("SubTask 3", "Desc3", epic1Id);
//        subTask3.setStartTime(start3);
//        subTask3.setDuration(duration3);
//        epic1.addSubTaskToEpic(subTask3);
//
//        epic1.checkEpicState();
//
//        assertEquals(expectedEndTime,
//                epic1.getEndTime(),
//                "endTime эпика должен быть равен endTime самой поздней подзадачи (subTask2)");
//    }


}