package service;

import exeptions.ManagerSaveException;
import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;

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
        int task1Id = manager.addAnyTypeTask(task1);
        Task task2 = new Task("Простая задача2", "Описание простой задачи 2122");
        int task2Id = manager.addAnyTypeTask(task2);
        Epic epic1 = new Epic("Важный эпик1", "Описание эпика 1");
        Epic epic2 = new Epic("Важный эпик2", "Описание эпика 2");
        int epic1Id = manager.addAnyTypeTask(epic1);
        int epic2Id = manager.addAnyTypeTask(epic2);

        SubTask subTask1 = new SubTask("Подзадача 1", "описание подзадачи1", epic1Id);
        int subTask1Id = manager.addAnyTypeTask(subTask1);
        SubTask subTask2 = new SubTask("Подзадача 2", "описание подзадачи2", epic1Id);
        int subTask2Id = manager.addAnyTypeTask(subTask2);
        subTask1.setStatus(Status.DONE);
        manager.updateTask(subTask1);

        FileBackedTaskManager manager2 = loadFromFile(new File("src/data.csv"));
        manager2.getAllTypesTask().forEach(System.out::println);
    }

//    @Override
//    public void clearManager() {
//        super.clearManager();
//        save();
//    }

    @Override
    public int addAnyTypeTask(Task newTask) {
        int id = super.addAnyTypeTask(newTask);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteAnyTypeTask(int id) {
        super.deleteAnyTypeTask(id);
        save();
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
            int maxId = maxIdOptional.orElse(-1);
            loadedManager.setCounter(maxId);

            // сначал нужно восстановить все эпики и таски и только потом SubTask, т.к. они содержат ссылки на Epic.
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

    private void save() {
        String header = "id,type,name,status,description,epic";

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
        super.addAnyTypeTask(task);
    }


}
