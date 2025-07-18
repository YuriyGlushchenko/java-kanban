import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    ArrayList<Task> getAllSimpleTask();

    ArrayList<Epic> getAllEpics();

    ArrayList<SubTask> getAllSubTask();

    ArrayList<Task> getAllTypesTask();

    void deleteAllTasks();

    Task getTaskById(int id);

    int addNewTask(Task newTask);

    void updateTask(Task task);

    void deleteTask(int id);

    ArrayList<SubTask> getEpicSubTasks(Epic epic);

    List<Task> getHistory();
}
