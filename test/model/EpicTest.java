package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private static Epic epic1;
    private static Epic epic2;

    private static SubTask subTask1;
    private static SubTask subTask2;

    @BeforeEach
    public void beforeEach() {
        // Создаём 2 эпика. У первого будет 2 подзадачи. Второй пустой.
        epic1 = new Epic("Важный эпик1", "описние эпика 1", 1);
        epic2 = new Epic("Важный эпик2", "описние эпика 2", 2);

        subTask1 = new SubTask("Подзадача 1", "описание подзадачи1", epic1.getId(), 3);
        subTask2 = new SubTask("Подзадача 2", "описание подзадачи2", epic1.getId(), 4);

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
        epic1.checkStatus();
        assertEquals(Status.IN_PROGRESS, epic1.getStatus());
    }

    @Test
    public void shouldBeEpicStatusIN_PROGRESSWhenOnlyOneSubTaskIsDone() {
        subTask1.setStatus(Status.DONE);
        epic1.checkStatus();
        assertEquals(Status.IN_PROGRESS, epic1.getStatus());
    }

    @Test
    public void shouldBeEpicStatusDONEWhenAllSubTaskIsDONE() {
        subTask1.setStatus(Status.DONE);
        subTask2.setStatus(Status.DONE);
        epic1.checkStatus();
        assertEquals(Status.DONE, epic1.getStatus());
    }

    @Test
    public void shouldBeEpicStatusNEWWhenDeleteAllSubTask() {
        epic1.deleteSubTaskFromEpic(subTask1.getId());
        epic1.deleteSubTaskFromEpic(subTask1.getId());
        epic1.checkStatus();
        assertEquals(Status.NEW, epic1.getStatus());
    }

    @Test
    public void shouldBeEpicStatusIN_PROGRESSWhenAllSubTaskIsIN_PROGRESS() {
        subTask1.setStatus(Status.IN_PROGRESS);
        subTask2.setStatus(Status.IN_PROGRESS);
        epic1.checkStatus();
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

}