package service;

import exeptions.ManagerSaveException;
import model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File autoSaveFile;

    public static void main(String[] args) {
        FileBackedTaskManager manager = new FileBackedTaskManager(new File("src/data.csv"));
        Task task1 = new Task("Простая задача1", "Описание простой задачи 1111");
        int task1Id = manager.addNewTask(task1);
        Task task2 = new Task("Простая задача2", "Описание простой задачи 2122");
        int task2Id = manager.addNewTask(task2);
        Epic epic1 = new Epic("Важный эпик1", "Описание эпика 1");
        Epic epic2 = new Epic("Важный эпик2", "Описание эпика 2");
        int epic1Id = manager.addNewTask(epic1);
        int epic2Id = manager.addNewTask(epic2);

        SubTask subTask1 = new SubTask("Подзадача 1", "описание подзадачи1", epic1Id);
        int subTask1Id = manager.addNewTask(subTask1);
        SubTask subTask2 = new SubTask("Подзадача 2", "описание подзадачи2", epic1Id);
        int subTask2Id = manager.addNewTask(subTask2);
        subTask1.setStatus(Status.DONE);
        manager.updateTask(subTask1);

        FileBackedTaskManager manager2 = loadFromFile(new File("src/data.csv"));
        manager2.getAllTypesTask().forEach(System.out::println);
    }


    public FileBackedTaskManager(File file) {
        this.autoSaveFile = file;
    }

    void save() {
        Stream<String> convertedTasksStream = Stream.of(tasks, subTasks, epics)
                .flatMap(map -> map.values().stream())
                .map(FileBackedTaskManager::convertToString);
        try {
            Files.write(autoSaveFile.toPath(), (Iterable<String>) convertedTasksStream::iterator);
        } catch (IOException e) {
            throw new ManagerSaveException("Возникла ошибка сохранения в файл");
        }

    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public int addNewTask(Task newTask) {
        int id = super.addNewTask(newTask);
        save();
        return id;
    }

    private void restoreTask(Task task) {
        super.addNewTask(task);
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    static String convertToString(Task task) {
        int epicId = -1;
        Type type = Type.TASK;
        if (task instanceof SubTask subTask) {
            type = Type.SUBTASK;
            epicId = subTask.getParentEpicId();
        } else if (task instanceof Epic) {
            type = Type.EPIC;
        }
        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(),
                type,
                task.getTitle(),
                task.getStatus(),
                task.getDescription(),
                type == Type.SUBTASK ? epicId : "");
    }

    Task restoreFromString(String value) {
        String[] data = value.trim().split(",");
        int id = Integer.parseInt(data[0]);
        String title = data[2];
        String description = data[4];
        Type type = Type.valueOf(data[1]);
        Status status = Status.valueOf(data[3]);


        return switch (type) {
            case TASK -> {
                Task restoredTask = new Task(title, description, id);
                restoredTask.setStatus(status);
                yield restoredTask;
            }
            case EPIC -> new Epic(title, description, id);
            case SUBTASK -> {
                SubTask restoredSubTask = new SubTask(title, description, Integer.parseInt(data[5]), id);
                restoredSubTask.setStatus(status);
                yield restoredSubTask;
            }
        };
    }

    static FileBackedTaskManager loadFromFile(File file) {
        Path path = file.toPath();
        FileBackedTaskManager manager2 = new FileBackedTaskManager(file);
        try {
            String content = Files.readString(path); // Чтение всего содержимого файла в одну строку
            if (content.strip().isBlank()) return manager2;
            String[] data = content.split("\n");

            // сначал нужно восстановить все эпики и таски и только потом SubTask, т.к. они содержат ссылки на Epic.
            Map<Boolean, List<String>> taskStrings = Arrays.stream(data).collect(Collectors.partitioningBy(str -> !str.split(",")[1].equals("SUBTASK")));

            taskStrings.get(true) // сначал делаем все эпики и таски
                    .stream().map(manager2::restoreFromString).forEach(manager2::restoreTask);

            taskStrings.get(false) // пото восстанавливаем SubTask
                    .stream().map(manager2::restoreFromString).forEach(manager2::restoreTask);

        } catch (IOException e) {
            throw new ManagerSaveException("Возникла ошибка чтения из файла");
        }
        return manager2;
    }


}
