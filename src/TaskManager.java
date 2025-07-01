import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class TaskManager {
    private final HashMap<Integer, Task> tasks;

    {
        tasks = new HashMap<>();
    }

    public ArrayList<Task> getAllTask() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<EpicTask> getAllEpicTask(){
        return  tasks.values()
                .stream()
                .filter(task -> task instanceof EpicTask)
                .map(task -> (EpicTask)task)
                .collect(Collectors.toCollection(ArrayList::new));

    }

    public ArrayList<SubTask> getAllSubTask(){
        return  tasks.values()
                .stream()
                .filter(task -> task instanceof SubTask)
                .map(task -> (SubTask)task)
                .collect(Collectors.toCollection(ArrayList::new));

    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public void addNewTask(Task newTask) {
        tasks.put(newTask.getId(), newTask);
        if(newTask instanceof SubTask subTask){
            subTask.getParentEpicTask().addSubTask(subTask);
            subTask.getParentEpicTask().checkEpicStatus();
        }
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
        if(task instanceof SubTask subTask){
            subTask.getParentEpicTask().checkEpicStatus();
        }
    }

    public void deleteTask(int id){
        Task taskToDelete = tasks.get(id);
        if(taskToDelete instanceof EpicTask epicTask){
            for(SubTask subTask: epicTask.getEpicSubTasks()){  // можно переписать через stream
                deleteTask(subTask.getId());
            }
        }
        if(taskToDelete instanceof SubTask subTask){
            subTask.getParentEpicTask().removeFromSubTasks(taskToDelete.getId());
        }
        tasks.remove(id);
        Task.Stat.counter--;
    }

    public int getTaskCount(){
     return Task.Stat.counter;
    }

    public ArrayList<SubTask> getEpicSubTaskList(EpicTask epicTask){
        return epicTask.getEpicSubTasks();
    }
}
