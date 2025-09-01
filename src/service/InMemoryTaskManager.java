package service;

import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;

import java.util.*;
import java.util.stream.Stream;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private int idCounter = 0;

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());

    }

    @Override
    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public ArrayList<Task> getAllTypesTask() {
        ArrayList<Task> allTasks = getAllTasks();
        allTasks.addAll(getAllEpics());
        allTasks.addAll(getAllSubTasks());
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
            epics.get(newSubTask.getParentEpicId()).addSubTaskToEpic(newSubTask);
            checkEpicStatusIsChanged(newSubTask.getParentEpicId());

        } else if (newTask instanceof Epic newEpic) {
            epics.put(newEpic.getId(), newEpic);
        } else {
            tasks.put(newTask.getId(), newTask);
        }
        return newTask.getId();
    }

    @Override
    public void updateTask(Task task) {
        if (task instanceof SubTask subTask) {
            subTasks.put(subTask.getId(), subTask);
            checkEpicStatusIsChanged(subTask.getParentEpicId());
        } else if (task instanceof Epic epic) {
            epics.put(epic.getId(), epic);
        } else {
            tasks.put(task.getId(), task);
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
            Epic parentEpic = epics.get(subTask.getParentEpicId());
            parentEpic.removeFromEpicSubTasks(taskToDelete.getId());
            subTasks.remove(id);
            checkEpicStatusIsChanged(parentEpic.getId());
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

    private void checkEpicStatusIsChanged(int epicId) {
        Epic epic = epics.get(epicId);
        epic.setStatus(Status.NEW);  // Можно указать любой статус, он просто запустит проверку статуса Эпика
    }

}
