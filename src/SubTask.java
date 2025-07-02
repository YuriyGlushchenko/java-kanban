public class SubTask extends Task {
    private Epic parentEpic;

    public SubTask(String title, String description, Epic parentEpic) {
        super(title, description);
        this.parentEpic = parentEpic;
    }

    public SubTask(Epic parentEpic) {
        this.parentEpic = parentEpic;
    }

    public Epic getParentEpic() {
        return parentEpic;
    }

    @Override
    public void setStatus(Status status) {
        super.setStatus(status);
        parentEpic.checkEpicStatus();
    }
}
