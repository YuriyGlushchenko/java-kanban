import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        TaskManager manager = new TaskManager();

        Task task1 = new Task("Простая задача1", "Описание простой задачи 1");
        manager.addNewTask(task1);
        Task task2 = new Task("Простая задача2", "Описание простой задачи 2");
        manager.addNewTask(task2);

        Epic epic1 = new Epic("Важный эпик1", "описние эпика 1");
        manager.addNewTask(epic1);
        SubTask subTask1 = new SubTask("Подзадача 1", "описание подзадачи1", epic1);
        manager.addNewTask(subTask1);
        SubTask subTask2 = new SubTask("Подзадача 2", "описание подзадачи2", epic1);
        manager.addNewTask(subTask2);

        epic1.setStatus(Status.DONE);
        System.out.println("Попытка вручную поменять статус эпика, должен остаться NEW: " + epic1);

        System.out.println("Список всех эпиков: ");
        System.out.println(manager.getAllEpicTask());

        System.out.println("Список всех subtask: ");
        System.out.println(manager.getAllSubTask());

        System.out.println("Список всех простых задач: ");
        System.out.println(manager.getAllSimpleTask());


        Task simpleTask = manager.getTaskById(1);
        simpleTask.setStatus(Status.IN_PROGRESS);

        ArrayList<SubTask> epicSubTasksList = manager.getEpicSubTaskList(epic1);
        SubTask epicSubTask1 = epicSubTasksList.getFirst();
        epicSubTask1.setStatus(Status.DONE);
        System.out.println("Поменяли статусы простой задачи и подзадачи, эпик должен сам: ");
        System.out.println(manager.getAllTypesTask());

        SubTask epicSubTask2 = epicSubTasksList.getLast();
        epicSubTask2.setStatus(Status.DONE);
        System.out.println("Обе подзадачи DONE, статус эпика должен сам поменяться: ");
        System.out.println(manager.getAllTypesTask());

        manager.deleteTask(epicSubTask2.getId());
        epicSubTask1.setStatus(Status.IN_PROGRESS);
        System.out.println("Удалили одну подзадачу, вторую поменяли статус, статус эпика должен сам стать IN_PROGRESS:");
        System.out.println(manager.getAllTypesTask());

        manager.deleteTask(epicSubTask1.getId());
        System.out.println("Удалили вторую подзадачу, статус эпика должен сам пересчитаться на NEW: ");
        System.out.println(manager.getAllTypesTask());

        manager.deleteTask(3);
        System.out.println("Удалили эпик: ");
        System.out.println(manager.getAllTypesTask());

        manager.deleteTask(2);
        System.out.println("Удалили простую задачу: ");
        System.out.println(manager.getAllTypesTask());

        manager.deleteAllTasks();
        System.out.println("Удалили вообще всё: ");
        System.out.println(manager.getAllTypesTask());


    }
}
