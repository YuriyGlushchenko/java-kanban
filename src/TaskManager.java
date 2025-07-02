import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private static int counter = 0;

    public ArrayList<Task> getAllSimpleTask() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getAllEpicTask() {
        return new ArrayList<>(epics.values());

    }

    public ArrayList<SubTask> getAllSubTask() {
        return new ArrayList<>(subTasks.values());
    }

    public ArrayList<Task> getAllTypesTask() {
        ArrayList<Task> allTasks = getAllSimpleTask();
        allTasks.addAll(getAllEpicTask());
        allTasks.addAll(getAllSubTask());
        return allTasks;
    }

    public void deleteAllTasks() {
        tasks.clear();
        subTasks.clear();
        epics.clear();
    }

    public Task getTaskById(int id) {
        if (tasks.containsKey(id)) {
            return tasks.get(id);
        } else if (subTasks.containsKey(id)) {
            return subTasks.get(id);
        } else if (epics.containsKey(id)) {
            return epics.get(id);
        } else {
            throw new NoSuchElementException("Нет задачи с таким id");
        }
    }

    public int addNewTask(Task newTask) {
        int newItemId = ++counter;
        newTask.setId(newItemId);
        if (newTask instanceof SubTask newSubTask) {
            subTasks.put(newItemId, newSubTask);
            newSubTask.getParentEpic().addSubTaskToEpic(newSubTask);
            newSubTask.getParentEpic().checkEpicStatus();
        } else if (newTask instanceof Epic newEpic) {
            epics.put(newItemId, newEpic);
        } else {
            tasks.put(newItemId, newTask);
        }
        return newItemId;
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
        if (task instanceof SubTask subTask) {
            subTask.getParentEpic().checkEpicStatus();
        }
    }

    public void deleteTask(int id) {
        Task taskToDelete = getTaskById(id);
        if (taskToDelete instanceof Epic epic) {
            for (SubTask subTask : epic.getEpicSubTasks()) {
                deleteTask(subTask.getId());
            }
            epics.remove(id);
        } else if (taskToDelete instanceof SubTask subTask) {
            subTask.getParentEpic().removeFromEpicSubTasks(taskToDelete.getId());
            subTasks.remove(id);
        } else {
            tasks.remove(id);
        }
    }

    public ArrayList<SubTask> getEpicSubTaskList(Epic epic) {
        return epic.getEpicSubTasks();
    }
}
