import model.Epic;
import model.Status;
import model.SubTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;
import service.TaskManager;

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
        epic1Id = manager.addNewTask(epic1);
        epic2Id = manager.addNewTask(epic2);

        subTask1 = new SubTask("Подзадача 1", "описание подзадачи1", epic1);
        subTask1Id = manager.addNewTask(subTask1);
        subTask2 = new SubTask("Подзадача 2", "описание подзадачи2", epic1);
        subTask2Id = manager.addNewTask(subTask2);
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
        assertEquals(Status.IN_PROGRESS, epic1.getStatus());
    }

    @Test
    public void shouldBeEpicStatusDONEWhenAllSubTaskIsDONE() {
        subTask1.setStatus(Status.DONE);
        subTask2.setStatus(Status.DONE);
        assertEquals(Status.DONE, epic1.getStatus());
    }

    @Test
    public void shouldBeEpicStatusNEWWhenDeleteAllSubTask() {
        epic1.removeFromEpicSubTasks(subTask1Id);
        epic1.removeFromEpicSubTasks(subTask2Id);
        assertEquals(Status.NEW, epic1.getStatus());
    }

    @Test
    public void shouldDeleteSubTaskFromEpic() {
        assertEquals(2, epic1.getEpicSubTasks().size());
        assertTrue(epic1.getEpicSubTasks().contains(subTask1));
        assertTrue(epic1.getEpicSubTasks().contains(subTask2));

        epic1.removeFromEpicSubTasks(subTask1Id);
        epic1.removeFromEpicSubTasks(subTask2Id);

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