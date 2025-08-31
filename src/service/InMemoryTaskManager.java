package service;

import model.Epic;
import model.SubTask;
import model.Task;

import java.util.*;
import java.util.stream.Stream;

public class InMemoryTaskManager implements TaskManager {
    final HashMap<Integer, Task> tasks = new HashMap<>();
    final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    final HashMap<Integer, Epic> epics = new HashMap<>();
    private static int idCounter = 0;

    private final HistoryManager historyManager = Managers.getDefaultHistory();


    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());

    }

    @Override
    public ArrayList<SubTask> getAllSubTask() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public ArrayList<Task> getAllTypesTask() {
        ArrayList<Task> allTasks = getTasks();
        allTasks.addAll(getAllEpics());
        allTasks.addAll(getAllSubTask());
        return allTasks;
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
        subTasks.clear();
        epics.clear();
        historyManager.clearHistory();
    }

    @Override
    public Task getAnyTypeTaskById(int id) {
        Optional<? extends Task> requestedTaskOptional = Stream.of(tasks, subTasks, epics)
                .filter(hMap -> hMap.containsKey(id))
                .map(hMap -> hMap.get(id))
                .findFirst();
        if (requestedTaskOptional.isPresent()) {
            Task requestedTask = requestedTaskOptional.get();
            addTaskToHistory(requestedTask);
            return requestedTask;
        } else {
            throw new NoSuchElementException("Нет задачи с таким id");
        }
    }

    @Override
    public Task getTaskById(int id) {
        Task requestedTask = tasks.get(id);
        if (requestedTask != null) {
            addTaskToHistory(requestedTask);
            return requestedTask;
        } else {
            throw new NoSuchElementException("Нет задачи с таким id");
        }
    }

    @Override
    public Epic getEpicById(int id) {
        Epic requestedEpic = epics.get(id);
        if (requestedEpic != null) {
            addTaskToHistory(requestedEpic);
            return requestedEpic;
        } else {
            throw new NoSuchElementException("Нет задачи с таким id");
        }
    }

    @Override
    public SubTask getSubTaskById(int id) {
        SubTask requestedSubTask = subTasks.get(id);
        if (requestedSubTask != null) {
            addTaskToHistory(requestedSubTask);
            return requestedSubTask;
        } else {
            throw new NoSuchElementException("Нет задачи с таким id");
        }
    }

    @Override
    public int addNewTask(Task newTask) {
        if (newTask.getId() == -1) {
            int newItemId = ++idCounter;
            newTask.setId(newItemId);
        }
        if (newTask instanceof SubTask newSubTask) {
            subTasks.put(newSubTask.getId(), newSubTask);
            newSubTask.getParentEpic().addSubTaskToEpic(newSubTask);
            newSubTask.getParentEpic().checkEpicStatus();
        } else if (newTask instanceof Epic newEpic) {
            epics.put(newEpic.getId(), newEpic);
        } else {
            tasks.put(newTask.getId(), newTask);
        }
        return newTask.getId();
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
        if (task instanceof SubTask subTask) {
            subTask.getParentEpic().checkEpicStatus();
        }
    }

    @Override
    public void deleteTask(int id) {
        Task taskToDelete = getAnyTypeTaskById(id);
        if (taskToDelete instanceof Epic epic) {
            for (SubTask subTask : epic.getEpicSubTasks()) {
                deleteTask(subTask.getId());
            }
            epics.remove(id);
        } else if (taskToDelete instanceof SubTask subTask) {
            subTask.getParentEpic().removeFromEpicSubTasks(taskToDelete.getId());
            subTasks.remove(id);
        } else {
            tasks.remove(id);
        }
        deleteFromHistory(taskToDelete);
    }

    @Override
    public ArrayList<SubTask> getEpicSubTasks(int id) {
        return epics.get(id).getEpicSubTasks();
    }

    private void addTaskToHistory(Task task) {
        historyManager.add(task);
    }

    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void deleteFromHistory(Task task) {
        historyManager.remove(task.getId());
    }

}
