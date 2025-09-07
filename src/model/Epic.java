package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Epic extends Task {
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    LocalDateTime endTime;

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

    public void deleteSubTaskFromEpic(int id) {
        subTasks.remove(id);
    }

    public void deleteAllEpicSubTasks() {
        subTasks.clear();
    }

    @Override
    public void setStatus(Status status) {
        checkStatus();
    }

    public void checkEpicState() {
        checkStatus();
        calculateDuration();
        evaluateStartTime();
        calculateEndTime();
    }

    @Override
    public void setDuration(Duration duration) {
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
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

    private void checkStatus() {
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

    private void calculateDuration() {
        Optional<Duration> actualDurationOptional = subTasks
                .values()
                .stream()
                .map(Task::getDuration)
//                .filter(Objects::nonNull)
                .reduce(Duration::plus);
        actualDurationOptional.ifPresent(super::setDuration);
    }

    private void evaluateStartTime() {
        Optional<LocalDateTime> actualStartTimeOptional = subTasks
                .values()
                .stream()
                .map(Task::getStartTime)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(Comparator.naturalOrder());
        actualStartTimeOptional.ifPresent(super::setStartTime);
    }

    private void calculateEndTime() {
        Optional<LocalDateTime> actualEndTimeOptional = subTasks
                .values()
                .stream()
                .map(Task::getEndTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder());
        actualEndTimeOptional.ifPresent(d -> this.endTime = d);
    }


}
