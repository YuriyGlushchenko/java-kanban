package model;

import java.util.ArrayList;
import java.util.HashMap;

public class Epic extends Task {
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    private boolean allIsDone = false;
    private boolean inProgress = false;

    public Epic(String title, String description) {
        super(title, description);
    }

    public Epic(String title, String description, int id) {
        super(title, description, id);
    }

    public void addSubTaskToEpic(SubTask subTask) {
        subTasks.put(subTask.getId(), subTask);
    }

    public ArrayList<SubTask> getEpicSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    public void removeFromEpicSubTasks(int id) {
        subTasks.remove(id);
        checkEpicStatus();
    }

    @Override
    public void setStatus(Status status) {
        if ((allIsDone && status == Status.DONE)
                || (inProgress && status == Status.IN_PROGRESS)
                || (!allIsDone && !inProgress && status == Status.NEW)) {
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
