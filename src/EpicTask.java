import java.util.ArrayList;

public class EpicTask extends Task{
    private ArrayList<SubTask> subTasks;

    {
        subTasks = new ArrayList<>();
    }

    public EpicTask(String title, String description) {
        super(title, description);
    }

    public void addSubTask(SubTask subTask){
        subTasks.add(subTask);
    }
}
