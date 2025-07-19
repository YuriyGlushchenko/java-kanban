import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubTaskTest {
    private final Epic testEpic = new Epic("Epic Title", "Epic Description");
    private final String TEST_TITLE = "Test SubTask";
    private final String TEST_DESCRIPTION = "Test Description";

    @Test
    void constructorShouldSetTitleAndDescription() {
        SubTask subTask = new SubTask(TEST_TITLE, TEST_DESCRIPTION, testEpic);

        assertAll(
                () -> assertEquals(TEST_TITLE, subTask.getTitle()),
                () -> assertEquals(TEST_DESCRIPTION, subTask.getDescription()),
                () -> assertEquals(testEpic, subTask.getParentEpic()),
                () -> assertEquals(Status.NEW, subTask.getStatus())
        );
    }

}