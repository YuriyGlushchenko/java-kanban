package service;

import model.Epic;
import model.SubTask;
import model.Task;
import model.Type;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File autoSaveFile;

    public static void main(String[] args) {
//        FileBackedTaskManager manager = new FileBackedTaskManager(new File("D:\\\\Java\\\\java-kanban\\\\src\\\\data.csv"));
//        Task task1 = new Task("Простая задача1", "Описание простой задачи 1111");
//        int task1Id = manager.addNewTask(task1);
//        Task task2 = new Task("Простая задача2", "Описание простой задачи 2122");
//        int task2Id = manager.addNewTask(task2);
//        Epic epic1 = new Epic("Важный эпик1", "Описание эпика 1");
//        Epic epic2 = new Epic("Важный эпик2", "Описание эпика 2");
//        int epic1Id = manager.addNewTask(epic1);
//        int epic2Id = manager.addNewTask(epic2);
//
//        SubTask subTask1 = new SubTask("Подзадача 1", "описание подзадачи1", epic1);
//        int subTask1Id = manager.addNewTask(subTask1);
//        SubTask subTask2 = new SubTask("Подзадача 2", "описание подзадачи2", epic1);
//        int subTask2Id = manager.addNewTask(subTask2);

        loadFromFile(new File("D:\\\\Java\\\\java-kanban\\\\src\\\\data.csv"));
    }


    public FileBackedTaskManager(File file) {
        this.autoSaveFile = file;
    }

    private void save() {
        Stream<String> convertedTasksStream = Stream.of(tasks, subTasks, epics)
                .flatMap(map -> map.values().stream())
                .map(FileBackedTaskManager::convertToString);
        try {
            Files.write(autoSaveFile.toPath(), (Iterable<String>) convertedTasksStream::iterator);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
            epicId = subTask.getParentEpic().getId();
        } else if (task instanceof Epic) {
            type = Type.EPIC;
        }
        return String.format("%d,%s,%s,%s,%s,%s"
                , task.getId()
                , type
                , task.getTitle()
                , task.getStatus()
                , task.getDescription()
                , type == Type.SUBTASK ? epicId : ""
        );
    }

    Task restoreFromString(String value) {
        System.out.println(value);
        String[] data = value.split(",");
        int id = Integer.parseInt(data[0]);
        String title = data[2];
        String description = data[3];
        Type type = Type.valueOf(data[1]);


        return switch (type) {
            case TASK -> new Task(title, description, id);
            case EPIC -> new Epic(title, description, id);
            case SUBTASK -> new SubTask(title, description, getEpicById(Integer.parseInt(data[5])), id);
        };
    }

    static FileBackedTaskManager loadFromFile(File file) {
        Path path = file.toPath();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            // Чтение всего содержимого файла в строку
            String content = Files.readString(path);
            System.out.println(content);

            String[] data = content.split("\n");
            System.out.println(data.length);
            Arrays.stream(data)
                    .filter(str -> !str.split(",")[1].equals("SUBTASK"))
                    .map(manager::restoreFromString)
                    .forEach(manager::addNewTask);

            System.out.println();
            Arrays.stream(data).forEach(System.out::println);
            System.out.println();

            Arrays.stream(data)
                    .filter(str -> str.split(",")[1].equals("SUBTASK"))
                    .map(manager::restoreFromString)
                    .forEach(manager::addNewTask);

            manager.getAllTypesTask().forEach(System.out::println);



        } catch (IOException e) {
            e.printStackTrace();
        }
        return manager;
    }


}
