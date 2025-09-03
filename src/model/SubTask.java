package model;

public class SubTask extends Task {
    private final int parentEpicId;

    public SubTask(String title, String description, int parentEpicId) {
        super(title, description);
        this.parentEpicId = parentEpicId;
    }

    public SubTask(String title, String description, int parentEpicId, int id) {
        super(title, description, id);
        this.parentEpicId = parentEpicId;
    }

    public SubTask(int parentEpicId) {
        this.parentEpicId = parentEpicId;
    }

    @Override
    public Type getType() {
        return Type.SUBTASK;
    }

    public int getParentEpicId() {
        return parentEpicId;
    }

    @Override
    public void setStatus(Status status) {
        super.setStatus(status);
    }
}
