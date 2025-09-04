package service;

import model.*;

public class TaskManagerUtils {

    public static String convertToString(Task task) {
        int epicId = -1;
        Type type = task.getType();
        if (type == Type.SUBTASK) {
            epicId = ((SubTask) task).getParentEpicId();
        }
        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(),
                type,
                task.getTitle(),
                task.getStatus(),
                task.getDescription(),
                type == Type.SUBTASK ? epicId : "");
    }

    public static Task restoreFromString(String value) {
        String[] data = value.trim().split(",");

        int id = Integer.parseInt(data[0]);
        Type type = Type.valueOf(data[1]);
        String title = data[2];
        Status status = Status.valueOf(data[3]);
        String description = data[4];

        return switch (type) {
            case TASK -> {
                Task restoredTask = new Task(title, description, id);
                restoredTask.setStatus(status);
                yield restoredTask;
            }
            case EPIC -> new Epic(title, description, id);
            case SUBTASK -> {
                int parentEpicId = Integer.parseInt(data[5]);
                SubTask restoredSubTask = new SubTask(title, description, parentEpicId, id);
                restoredSubTask.setStatus(status);
                yield restoredSubTask;
            }
        };
    }


}
