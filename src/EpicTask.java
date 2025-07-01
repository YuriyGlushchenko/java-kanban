import java.util.ArrayList;
import java.util.HashMap;

public class EpicTask extends Task {
    private final HashMap<Integer, SubTask> subTasks;
    private boolean allIsDone;
    private boolean inProgress;

    {
        subTasks = new HashMap<>();
        allIsDone = false;
        inProgress = false;
    }

    public EpicTask(String title, String description) {
        super(title, description);
    }

    public void addSubTask(SubTask subTask) {
        subTasks.put(subTask.getId(), subTask);
    }

    public ArrayList<SubTask> getEpicSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    public void removeFromSubTasks(int id) {
        subTasks.remove(id);
        checkEpicStatus();
    }

    @Override
    public void setStatus(Status status) {
//        throw new UnsupportedOperationException("Прямая смена статуса в Epic запрещена");
        if (allIsDone && status == Status.DONE) { // можно переписать на switch или DRY
            super.setStatus(status);
        } else if (inProgress && status == Status.IN_PROGRESS) {
            super.setStatus(status);
        } else if (!allIsDone && !inProgress && status == Status.NEW) {
            super.setStatus(status);
        }
    }

    public void checkEpicStatus() {
        if (isAllDone() && !subTasks.isEmpty()) {
            allIsDone = true;
            setStatus(Status.DONE);
        } else if (isInProgress()) {
            allIsDone = false;
            inProgress = true;
            setStatus(Status.IN_PROGRESS);
        } else {
            allIsDone = false;
            inProgress = false;
            setStatus(Status.NEW);
        }
    }

    private boolean isAllDone() {
        return subTasks
                .values()
                .stream()
                .map(Task::getStatus)
                .allMatch(status -> status == Status.DONE);
    }

    private boolean isInProgress() {
        return subTasks
                .values()
                .stream()
                .map(Task::getStatus)
                .anyMatch(status -> status == Status.DONE || status == Status.IN_PROGRESS); // для пустого потока False
    }


}
