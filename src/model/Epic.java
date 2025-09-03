package model;

import java.util.ArrayList;
import java.util.HashMap;

public class Epic extends Task {
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();

    public Epic(String title, String description) {
        super(title, description);
    }

    public Epic(String title, String description, int id) {
        super(title, description, id);
    }

    @Override
    public Type getType() {
        return Type.EPIC;
    }

    public void addSubTaskToEpic(SubTask subTask) {
        subTasks.put(subTask.getId(), subTask);
    }

    public ArrayList<SubTask> getEpicSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    public void removeFromEpicSubTasks(int id) {
        subTasks.remove(id);
    }

    public void deleteAllEpicSubTasks(){
        subTasks.clear();
    }

    @Override
    public void setStatus(Status status) {
        boolean allDone = isAllDone();
        boolean inProgress = isInProgress();

        Status newStatus = Status.NEW;

        if (allDone && !subTasks.isEmpty()) {
            newStatus = Status.DONE;
        } else if (!allDone && inProgress) {
            newStatus = Status.IN_PROGRESS;
        }

        if (getStatus() != newStatus) super.setStatus(newStatus);
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
