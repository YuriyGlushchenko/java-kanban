package service;

import model.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private int idCounter = 0;
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>();

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

    public void deleteAllSubTasks() {
        epics.values().forEach(Epic::deleteAllEpicSubTasks);
        subTasks.keySet().forEach(historyManager::remove);
        subTasks.values().stream().filter(task -> task.getStartTime().isPresent()).forEach(prioritizedTasks::remove);
        subTasks.clear();
    }

    public void deleteAllEpics() {
        subTasks.keySet().forEach(historyManager::remove);
        epics.keySet().forEach(historyManager::remove);
        epics.clear();
        subTasks.clear();
    }

    @Override
    public void deleteAllTasks() {
        tasks.keySet().forEach(historyManager::remove);
        tasks.values().stream().filter(task -> task.getStartTime().isPresent()).forEach(prioritizedTasks::remove);
        tasks.clear();
    }

    @Override
    public Task getAnyTypeTaskById(int id) {
        Optional<? extends Task> requestedTaskOptional = Stream.of(tasks, subTasks, epics)
                .filter(hMap -> hMap.containsKey(id))
                .map(hMap -> hMap.get(id))
                .findFirst();
        if (requestedTaskOptional.isPresent()) {
            Task requestedTask = requestedTaskOptional.get();
            historyManager.add(requestedTask);
            return requestedTask;
        } else {
            throw new NoSuchElementException("Нет задачи с таким id");
        }
    }

    @Override
    public Task getTaskById(int id) {
        Task requestedTask = tasks.get(id);
        if (requestedTask != null) {
            historyManager.add(requestedTask);
            return requestedTask;
        } else {
            throw new NoSuchElementException("Нет задачи с таким id");
        }
    }

    @Override
    public Epic getEpicById(int id) {
        Epic requestedEpic = epics.get(id);
        if (requestedEpic != null) {
            historyManager.add(requestedEpic);
            return requestedEpic;
        } else {
            throw new NoSuchElementException("Нет задачи с таким id");
        }
    }

    @Override
    public SubTask getSubTaskById(int id) {
        SubTask requestedSubTask = subTasks.get(id);
        if (requestedSubTask != null) {
            historyManager.add(requestedSubTask);
            return requestedSubTask;
        } else {
            throw new NoSuchElementException("Нет задачи с таким id");
        }
    }

    @Override
    public int addAnyTypeTask(Task newTask) {
        if (newTask.getId() == -1) {
            int newItemId = ++idCounter;
            newTask.setId(newItemId);
        }

        Type newTaskType = newTask.getType();
        switch (newTaskType) {
            case TASK -> addNewTask(newTask);
            case SUBTASK -> addNewSubTask((SubTask) newTask);
            case EPIC -> addNewEpic((Epic) newTask);
        }
        return newTask.getId();
    }

    @Override
    public void updateTask(Task task) {
        Type taskType = task.getType();
        if (task.getStartTime().isPresent() && task.getType() != Type.EPIC) {
            prioritizedTasks.add(task);
        }
        switch (taskType) {
            case TASK -> tasks.put(task.getId(), task);
            case SUBTASK -> {
                SubTask updatedSubTask = (SubTask) task;
                subTasks.put(updatedSubTask.getId(), updatedSubTask);
                checkEpicStatusIsChanged(updatedSubTask.getParentEpicId());
            }
            case EPIC -> epics.put(task.getId(), (Epic) task);
        }

    }

    @Override
    public void deleteAnyTypeTask(int id) {
        Type taskToDeleteType = getAnyTypeTaskById(id).getType();
        switch (taskToDeleteType) {
            case TASK -> deleteTask(id);
            case SUBTASK -> deleteSubTask(id);
            case EPIC -> deleteEpic(id);
        }
    }

    public void setCounter(int counter) {
        this.idCounter = counter;
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private void deleteTask(int id) {
        historyManager.remove(id);
        prioritizedTasks.remove(tasks.get(id));
        tasks.remove(id);
    }

    private void deleteSubTask(int id) {
        SubTask subTaskToDelete = subTasks.get(id);
        Epic parentEpic = epics.get(subTaskToDelete.getParentEpicId());
        parentEpic.deleteSubTaskFromEpic(subTaskToDelete.getId());
        historyManager.remove(id);
        prioritizedTasks.remove(subTaskToDelete);
        subTasks.remove(id);
        checkEpicStatusIsChanged(parentEpic.getId());
    }

    private void deleteEpic(int id) {
        Epic epicToDelete = epics.get(id);
        for (SubTask subTask : epicToDelete.getEpicSubTasks()) {
            historyManager.remove(subTask.getId());
            subTasks.remove(subTask.getId());
        }
        historyManager.remove(id);
        epics.remove(id);
    }

    @Override
    public ArrayList<SubTask> getEpicSubTasks(int id) {
        return epics.get(id).getEpicSubTasks();
    }

    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void addNewTask(Task newTask) {
        tasks.put(newTask.getId(), newTask);
        if (newTask.getStartTime().isPresent()) {
            prioritizedTasks.add(newTask);
        }
    }

    private void addNewSubTask(SubTask newSubTask) {
        subTasks.put(newSubTask.getId(), newSubTask);
        epics.get(newSubTask.getParentEpicId()).addSubTaskToEpic(newSubTask);
        checkEpicStatusIsChanged(newSubTask.getParentEpicId());
        if (newSubTask.getStartTime().isPresent()) {
            prioritizedTasks.add(newSubTask);
        }

    }

    private void addNewEpic(Epic newEpic) {
        epics.put(newEpic.getId(), newEpic);
    }

    private void checkEpicStatusIsChanged(int epicId) {
        Epic epic = epics.get(epicId);
        epic.checkEpicState();
    }

    private boolean isOverlap(Task task1, Task task2) {
        if (task1.getStartTime().isEmpty() || task2.getStartTime().isEmpty()) return false;
        LocalDateTime start1 = task1.getStartTime().get();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime().get();
        LocalDateTime end2 = task1.getEndTime();

        if (start1.isBefore(start2) && end1.isBefore(start2)) {
            return false;
        } else if (start2.isBefore(start1) && end2.isBefore(start1)) {
            return false;
        } else return true;
    }

    private boolean hasTimeConflict(Task task) {
        List<Task> prioritizedTasks = getPrioritizedTasks();
        if (prioritizedTasks.isEmpty()) return false;
        return prioritizedTasks
                .stream()
                .anyMatch(task1 -> isOverlap(task1, task));
    }

}
