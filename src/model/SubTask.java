package model;

public class SubTask extends Task {
    private final int parentEpicId;

    public SubTask(String title, String description, int parentEpicId) {
        super(title, description);
        this.parentEpicId = parentEpicId;
        this.type = Type.SUBTASK;
    }

    public SubTask(String title, String description, int parentEpicId, int id) {
        super(title, description, id);
        this.parentEpicId = parentEpicId;
        this.type = Type.SUBTASK;
    }

    public SubTask(int parentEpicId) {
        this.parentEpicId = parentEpicId;
        this.type = Type.SUBTASK;
    }

    public int getParentEpicId() {
        return parentEpicId;
    }

    @Override
    public void setStatus(Status status) {
        super.setStatus(status);
//        parentEpicId.checkEpicStatus();
    }
}
