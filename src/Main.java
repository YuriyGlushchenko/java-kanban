import model.Epic;
import model.SubTask;
import model.Task;
import service.Managers;
import service.TaskManager;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        TaskManager manager = Managers.getDefault();

        Task task1 = new Task("Простая задача1", "Описание простой задачи 1");
        manager.addAnyTypeTask(task1);
        Task task2 = new Task("Простая задача2", "Описание простой задачи 2");
        manager.addAnyTypeTask(task2);

        Epic epic1 = new Epic("Важный эпик1", "описние эпика 1");
        manager.addAnyTypeTask(epic1);
        SubTask subTask1 = new SubTask("Подзадача 1", "описание подзадачи1", epic1.getId());
        manager.addAnyTypeTask(subTask1);
        SubTask subTask2 = new SubTask("Подзадача 2", "описание подзадачи2", epic1.getId());
        manager.addAnyTypeTask(subTask2);
        SubTask subTask3 = new SubTask("Подзадача 3", "описание подзадачи3", epic1.getId());
        manager.addAnyTypeTask(subTask3);

        Epic epic2 = new Epic("Важный эпик2", "описние эпика 2");
        manager.addAnyTypeTask(epic2);


        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);

            for (Task task : manager.getEpicSubTasks(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getAllSubTasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }

        manager.getAnyTypeTaskById(1);
        manager.getAnyTypeTaskById(2);
        manager.getAnyTypeTaskById(3);
        manager.getAnyTypeTaskById(3);
        manager.getAnyTypeTaskById(4);
        manager.getAnyTypeTaskById(3);

        System.out.println("История после вызова getTaskById (вызов в порядке 1,2,3,3,4,3):");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}
