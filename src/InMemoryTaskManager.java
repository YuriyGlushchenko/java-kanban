import java.util.*;
import java.util.stream.Stream;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private static int counter = 0;

    private final HistoryManager historyManager = Managers.getDefaultHistory();


    @Override
    public ArrayList<Task> getAllSimpleTask() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());

    }

    @Override
    public ArrayList<SubTask> getAllSubTask() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public ArrayList<Task> getAllTypesTask() {
        ArrayList<Task> allTasks = getAllSimpleTask();
        allTasks.addAll(getAllEpics());
        allTasks.addAll(getAllSubTask());
        return allTasks;
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
        subTasks.clear();
        epics.clear();
        historyManager.clearHistory();
    }

    @Override
    public Task getTaskById(int id) {
        Optional<? extends Task>  requestedTaskOptional = Stream.of(tasks, subTasks, epics)
                .filter(hMap -> hMap.containsKey(id))
                .map(hMap -> hMap.get(id))
                .findFirst();
        if(requestedTaskOptional.isPresent()){
            Task requestedTask = requestedTaskOptional.get();
            addTaskToHistory(requestedTask);
            return requestedTask;
        } else {
            throw new NoSuchElementException("Нет задачи с таким id");
        }
    }

    @Override
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

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
        if (task instanceof SubTask subTask) {
            subTask.getParentEpic().checkEpicStatus();
        }
    }

    @Override
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
        deleteFromHistory(taskToDelete);
    }

    @Override
    public ArrayList<SubTask> getEpicSubTasks(Epic epic) {
        return epic.getEpicSubTasks();
    }

    private void addTaskToHistory(Task task){
        historyManager.add(task);
    }

    public List<Task> getHistory(){
        return historyManager.getHistory();
    }

    private void deleteFromHistory(Task task){
        historyManager.delete(task);
    }

}
