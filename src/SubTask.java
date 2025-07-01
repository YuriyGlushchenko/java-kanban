public class SubTask extends Task{
    private EpicTask parentEpicTask;

    public SubTask(String title, String description, EpicTask parentEpicTask) {
        super(title, description);
        this.parentEpicTask = parentEpicTask;
    }

    public SubTask(EpicTask parentEpicTask) {
        this.parentEpicTask = parentEpicTask;
    }

    public EpicTask getParentEpicTask() {
        return parentEpicTask;
    }

    @Override
    public void setStatus(Status status) {
        super.setStatus(status);
        parentEpicTask.checkEpicStatus();
    }
}
