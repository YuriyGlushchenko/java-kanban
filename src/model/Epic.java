package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class Epic extends Task {
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    private LocalDateTime endTime;

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

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

//    public void checkEpicState() {
//        if (subTasks.isEmpty()) {
//            updateEpicTimes(Duration.ZERO, null, null);
//            super.setStatus(Status.NEW);
//            return;
//        }
//        checkStatus();
//
//        Duration totalDuration = Duration.ZERO;
//        LocalDateTime earliestStart = null;
//        LocalDateTime latestEnd = null;
//        boolean hasTimeData = false;
//
//        for (SubTask subTask : subTasks.values()) {
//            Optional<LocalDateTime> subTaskStartTime = subTask.getStartTime();
//            LocalDateTime subTaskEndTime = subTask.getEndTime();
//
//            if (subTaskStartTime.isPresent() && subTaskEndTime != null) { // надо ли учитывать задачи без времени начала, но с длительностью?
//                hasTimeData = true;
//
//
//                if (earliestStart == null || subTaskStartTime.get().isBefore(earliestStart)) {
//                    earliestStart = subTaskStartTime.get();
//                }
//
//                if (latestEnd == null || subTaskEndTime.isAfter(latestEnd)) {
//                    latestEnd = subTaskEndTime;
//                }
//
//                totalDuration = totalDuration.plus(subTask.getDuration());
//            }
//        }
//
//        if (hasTimeData) {
//            updateEpicTimes(totalDuration, earliestStart, latestEnd);
//        } else {
//            updateEpicTimes(Duration.ZERO, null, null);
//        }
//
//    }

//    @Override
//    public void setDuration(Duration duration) {
//    }

//    @Override
//    public void setStartTime(LocalDateTime startTime) {
//    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    private boolean isAllDone() {
        return subTasks
                .values()
                .stream()
                .map(Task::getStatus)
                .allMatch(status -> status == Status.DONE);
    }

    private boolean isAllNew() {
        return subTasks
                .values()
                .stream()
                .map(Task::getStatus)
                .allMatch(status -> status == Status.NEW);
    }

    public void checkStatus() {
        Status newStatus = Status.IN_PROGRESS;

        if (isAllDone() && !subTasks.isEmpty()) {
            newStatus = Status.DONE;
        } else if (isAllNew()) {
            newStatus = Status.NEW;
        }

        if (getStatus() != newStatus) super.setStatus(newStatus);
    }

//    public void updateEpicTimes(Duration duration, LocalDateTime startTime, LocalDateTime endTime) {
//        super.setDuration(duration);
//        super.setStartTime(startTime);
//        this.endTime = endTime;
//    }


}
