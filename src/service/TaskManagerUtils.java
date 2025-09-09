package service;

import model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class TaskManagerUtils {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static String convertToString(Task task) {
//        "id,type,name,status,description,duration,startTime,epic"
        int epicId = -1;
        Type type = task.getType();
        if (type == Type.SUBTASK) {
            epicId = ((SubTask) task).getParentEpicId();
        }
        Optional<LocalDateTime> startTimeOptional = task.getStartTime();


        return String.format("%d,%s,%s,%s,%s,%d,%s,%s",
                task.getId(),
                type,
                task.getTitle(),
                task.getStatus(),
                task.getDescription(),
                task.getDuration().toMinutes(),
                startTimeOptional.map(localDateTime -> localDateTime.format(formatter)).orElse("null"),
                type == Type.SUBTASK ? epicId : "");
    }

    public static Task restoreFromString(String value) {
//        "id,type,name,status,description,duration,startTime,epic"
        String[] data = value.trim().split(",");

        int id = Integer.parseInt(data[0]);
        Type type = Type.valueOf(data[1]);
        String title = data[2];
        Status status = Status.valueOf(data[3]);
        String description = data[4];
        Duration duration = Duration.ofMinutes(Integer.parseInt(data[5]));
        LocalDateTime startTime = data[6].equals("null") ? null : LocalDateTime.parse(data[6], formatter);

        return switch (type) {
            case TASK -> {
                Task restoredTask = new Task(title, description, id);
                restoredTask.setStatus(status);
                if (startTime != null) restoredTask.setStartTime(startTime);
                if (duration != Duration.ZERO) restoredTask.setDuration(duration);
                yield restoredTask;
            }
            case EPIC -> new Epic(title, description, id);
            case SUBTASK -> {
                int parentEpicId = Integer.parseInt(data[7]);
                SubTask restoredSubTask = new SubTask(title, description, parentEpicId, id);
                restoredSubTask.setStatus(status);
                if (startTime != null) restoredSubTask.setStartTime(startTime);
                if (duration != Duration.ZERO) restoredSubTask.setDuration(duration);
                yield restoredSubTask;
            }
        };
    }


}
