package service;

import model.Task;
import org.junit.jupiter.api.BeforeEach;


class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    private Task taskA;
    private Task taskB;

    @Override
    protected InMemoryTaskManager getManager() {
        return new InMemoryTaskManager();
    }

    @BeforeEach
    public void beforeEachInMemoryTaskManager() throws NoSuchMethodException {
        taskA = new Task("Task 1", "Description 1");
        taskB = new Task("Task 2", "Description 2");
    }

}