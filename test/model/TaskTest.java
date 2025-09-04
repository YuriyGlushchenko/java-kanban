package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    private final String TEST_TITLE = "Test model.Task";
    private final String TEST_DESCRIPTION = "Test Description";

    @Test
    void constructorShouldSetTitleAndDescription() {
        Task task = new Task(TEST_TITLE, TEST_DESCRIPTION);

        assertAll(
                () -> assertEquals(TEST_TITLE, task.getTitle()),
                () -> assertEquals(TEST_DESCRIPTION, task.getDescription()),
                () -> assertEquals(Status.NEW, task.getStatus()),
                () -> assertEquals(-1, task.getId())
        );
    }

    @Test
    void setIdShouldWorkOnce() {
        Task task = new Task();
        task.setId(1);

        assertEquals(1, task.getId());
        assertThrows(UnsupportedOperationException.class, () -> task.setId(2));
    }

    @Test
    void tasksWithTheSameIDShouldBeEqual() {
        Task task1 = new Task("Title 1", "Desc 1");
        task1.setId(1);

        Task task2 = new Task("Title 2", "Desc 2");
        task2.setId(1);

        Task task3 = new Task("Title 1", "Desc 1");
        task3.setId(2);

        assertAll(
                () -> assertEquals(task1, task2),
                () -> assertNotEquals(task1, task3)
        );
    }

    @Test
    void ShouldCreateNewTask() {
        Task task = new Task();
        assertNotEquals(null, task);
    }

}