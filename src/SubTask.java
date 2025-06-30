public class SubTask extends Task{
    private EpicTask parentTask;

    public SubTask(String title, String description, EpicTask parentTask) {
        super(title, description);
        this.parentTask = parentTask;
    }

    public SubTask(EpicTask parentTask) {
        this.parentTask = parentTask;
    }

    public EpicTask getParentTask() {
        return parentTask;
    }
}
