import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    ArrayList<Task> getTasks();

    ArrayList<Epic> getAllEpics();

    ArrayList<SubTask> getAllSubTask();

    ArrayList<Task> getAllTypesTask();

    void deleteAllTasks();

    Task getAnyTypeTaskById(int id);

    int addNewTask(Task newTask);

    void updateTask(Task task);

    void deleteTask(int id);

    ArrayList<SubTask> getEpicSubTasks(int id);

    List<Task> getHistory();

    Task getTaskById(int id);

    Epic getEpicById(int id);

    SubTask getSubTaskById(int id);
}
