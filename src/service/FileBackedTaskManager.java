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
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File autoSaveFile;

    public FileBackedTaskManager(File file) {
        this.autoSaveFile = file;
    }

    public static void main(String[] args) {
        FileBackedTaskManager manager = new FileBackedTaskManager(new File("src/data.csv"));
        Task task1 = new Task("Простая задача1", "Описание простой задачи 1111");
        int task1Id = manager.addNewTask(task1);
        Task task2 = new Task("Простая задача2", "Описание простой задачи 2122");
        int task2Id = manager.addNewTask(task2);
        Epic epic1 = new Epic("Важный эпик1", "Описание эпика 1");
        Epic epic2 = new Epic("Важный эпик2", "Описание эпика 2");
        int epic1Id = manager.addNewEpic(epic1);
        int epic2Id = manager.addNewEpic(epic2);

        SubTask subTask1 = new SubTask("Подзадача 1", "описание подзадачи1", epic1Id);
        int subTask1Id = manager.addNewSubTask(subTask1);
        SubTask subTask2 = new SubTask("Подзадача 2", "описание подзадачи2", epic1Id);
        int subTask2Id = manager.addNewSubTask(subTask2);
        subTask1.setStatus(Status.DONE);
        manager.updateSubTask(subTask1);

        FileBackedTaskManager loadedManager = loadFromFile(new File("src/data.csv"));
        loadedManager.getAllTasks().forEach(System.out::println);
        loadedManager.getAllSubTasks().forEach(System.out::println);
        loadedManager.getAllEpics().forEach(System.out::println);
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        Path path = file.toPath();
        FileBackedTaskManager loadedManager = new FileBackedTaskManager(file);
        try {
            String content = Files.readString(path); // Чтение всего содержимого файла в одну строку
            String[] data = content.strip().split("\n");
            if (data.length == 1) return loadedManager;
            String[] dataWithoutHeader = Arrays.copyOfRange(data, 1, data.length);

            OptionalInt maxIdOptional = Arrays
                    .stream(dataWithoutHeader)
                    .map(line -> line.split(",")[0]) // Берем id
                    .mapToInt(Integer::parseInt)
                    .max();
            //            loadedManager.setCounter(maxId);
            loadedManager.idCounter = maxIdOptional.orElse(-1);

            // Сначала нужно восстановить все эпики и таски и только потом SubTask, т.к. они содержат ссылки на Epic.
            Map<Boolean, List<String>> taskStrings = Arrays
                    .stream(dataWithoutHeader)
                    .collect(Collectors.partitioningBy(str -> !str.split(",")[1].equals("SUBTASK")));

            taskStrings.get(true) // сначала делаем все эпики и таски
                    .stream()
                    .map(TaskManagerUtils::restoreFromString)
                    .forEach(loadedManager::restoreTask);

            taskStrings.get(false) // потом восстанавливаем SubTask
                    .stream()
                    .map(TaskManagerUtils::restoreFromString)
                    .forEach(loadedManager::restoreTask);

        } catch (IOException e) {
            throw new ManagerSaveException("Возникла ошибка чтения из файла");
        }
        return loadedManager;
    }

    @Override
    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteSubTask(int id) {
        super.deleteSubTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public int addNewTask(Task newTask) {
        int newTaskId = super.addNewTask(newTask);
        save();
        return newTaskId;
    }

    @Override
    public int addNewSubTask(SubTask newSubTask) {
        int newSubTaskId = super.addNewSubTask(newSubTask);
        save();
        return newSubTaskId;
    }

    @Override
    public int addNewEpic(Epic newEpic) {
        int newEpicId = super.addNewEpic(newEpic);
        save();
        return newEpicId;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        super.updateSubTask(subTask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    private void save() {
        String header = "id,type,name,status,description,duration,startTime,epic";

        Stream<String> convertedTasksStream = Stream.of(tasks, subTasks, epics)
                .flatMap(map -> map.values().stream())
                .map(TaskManagerUtils::convertToString);
        Stream<String> fullContent = Stream.concat(Stream.of(header), convertedTasksStream);

        try {
            Files.write(autoSaveFile.toPath(), (Iterable<String>) fullContent::iterator);
        } catch (IOException e) {
            throw new ManagerSaveException("Возникла ошибка сохранения в файл");
        }

    }

    private void restoreTask(Task task) {
        Type taskType = task.getType();
        switch (taskType) {
            case TASK -> super.addNewTask(task);
            case SUBTASK -> super.addNewSubTask((SubTask) task);
            case EPIC -> super.addNewEpic((Epic) task);
        }
    }

}
