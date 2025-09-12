package service;

import exeptions.TimeIntersectionException;
import model.Epic;
import model.SubTask;
import model.Task;
import model.Type;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(task -> task.getStartTime().orElse(LocalDateTime.MAX))
    );
    protected int idCounter = 0;

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.keySet().forEach(historyManager::remove);
        tasks.values().stream().filter(task -> task.getStartTime().isPresent()).forEach(prioritizedTasks::remove);
        tasks.clear();
    }

    @Override
    public void deleteAllSubTasks() {
        epics.values().forEach(Epic::deleteAllEpicSubTasks);
        subTasks.keySet().forEach(historyManager::remove);
        subTasks.values().stream().filter(task -> task.getStartTime().isPresent()).forEach(prioritizedTasks::remove);
        subTasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.keySet().forEach(historyManager::remove);
        epics.clear();
        deleteAllSubTasks();
    }

    @Override
    public Optional<Task> getTaskById(int id) {
        Optional<Task> taskOptional = Optional.ofNullable(tasks.get(id));
        taskOptional.ifPresent(historyManager::add);
        return taskOptional;

    }

    @Override
    public Optional<SubTask> getSubTaskById(int id) {
        Optional<SubTask> requestedSubTaskOptional = Optional.ofNullable(subTasks.get(id));
        requestedSubTaskOptional.ifPresent(historyManager::add);
        return requestedSubTaskOptional;
    }

    @Override
    public Optional<Epic> getEpicById(int id) {
        Optional<Epic> requestedEpicOptional = Optional.ofNullable(epics.get(id));
        requestedEpicOptional.ifPresent(historyManager::add);
        return requestedEpicOptional;
    }

    @Override
    public void updateTask(Task task) {
        if (hasTimeConflict(task)) return;
        if (task.getType() != Type.TASK) return;
        if (task.getStartTime().isPresent()) prioritizedTasks.add(task);
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        if (hasTimeConflict(subTask)) return;
        if (subTask.getStartTime().isPresent()) prioritizedTasks.add(subTask);
        subTasks.put(subTask.getId(), subTask);
        checkEpicStatusIsChanged(subTask.getParentEpicId());
    }

    @Override
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    @Override
    public void deleteTask(int id) {
        historyManager.remove(id);
        Task taskToDelete = tasks.get(id);
        if (taskToDelete != null) {
            prioritizedTasks.remove(tasks.get(id));
            tasks.remove(id);
        }
    }

    @Override
    public void deleteSubTask(int id) {
        historyManager.remove(id);
        SubTask subTaskToDelete = subTasks.get(id);
        if (subTaskToDelete != null) {
            Epic parentEpic = epics.get(subTaskToDelete.getParentEpicId());
            parentEpic.deleteSubTaskFromEpic(subTaskToDelete.getId());
            prioritizedTasks.remove(subTaskToDelete);
            subTasks.remove(id);
            checkEpicStatusIsChanged(parentEpic.getId());
        }
    }

    @Override
    public void deleteEpic(int id) {
        historyManager.remove(id);
        Epic epicToDelete = epics.get(id);
        if (epicToDelete != null) {
            for (SubTask subTask : epicToDelete.getEpicSubTasks()) {
                historyManager.remove(subTask.getId());
                subTasks.remove(subTask.getId());
                prioritizedTasks.remove(subTask);
            }
            epics.remove(id);
        }
    }

    @Override
    public ArrayList<SubTask> getEpicSubTasks(int id) {
        return epics.get(id).getEpicSubTasks();
    }

    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private void setIdToTask(Task task) {
        if (task.getId() == -1) {
            int newItemId = ++idCounter;
            task.setId(newItemId);
        }
    }

    @Override
    public int addNewTask(Task newTask) {
        if (hasTimeConflict(newTask))
            throw new TimeIntersectionException("Задача не добавлена: пересечение по времени");
        setIdToTask(newTask);
        tasks.put(newTask.getId(), newTask);
        if (newTask.getStartTime().isPresent()) {
            prioritizedTasks.add(newTask);
        }
        return newTask.getId();
    }

    @Override
    public int addNewSubTask(SubTask newSubTask) {
        if (hasTimeConflict(newSubTask))
            throw new TimeIntersectionException("Задача не добавлена: пересечение по времени");
        setIdToTask(newSubTask);
        subTasks.put(newSubTask.getId(), newSubTask);
        epics.get(newSubTask.getParentEpicId()).addSubTaskToEpic(newSubTask);
        checkEpicStatusIsChanged(newSubTask.getParentEpicId());
        if (newSubTask.getStartTime().isPresent()) {
            prioritizedTasks.add(newSubTask);
        }
        return newSubTask.getId();
    }

    @Override
    public int addNewEpic(Epic newEpic) {
        setIdToTask(newEpic);
        epics.put(newEpic.getId(), newEpic);
        return newEpic.getId();
    }

    private boolean isOverlap(Task task1, Task task2) {
        if (task1.getStartTime().isEmpty() || task2.getStartTime().isEmpty()) return false;
        LocalDateTime start1 = task1.getStartTime().get();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime().get();
        LocalDateTime end2 = task2.getEndTime();

        return !end1.isBefore(start2) && !end2.isBefore(start1) && !end1.isEqual(start2) && !end2.isEqual(start1);

    }

    private boolean hasTimeConflict(Task task) {
        List<Task> prioritizedTasks = getPrioritizedTasks();
        if (prioritizedTasks.isEmpty()) return false;
        return prioritizedTasks
                .stream()
                .anyMatch(task1 -> isOverlap(task1, task) && !task1.equals(task));
    }

    private void checkEpicStatusIsChanged(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic.getEpicSubTasks().isEmpty()) {
            updateEpicTimes(epic, Duration.ZERO, null, null);
            return;
        }
        epic.checkStatus();

        Duration totalDuration = Duration.ZERO;
        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;
        boolean hasTimeData = false;

        for (SubTask subTask : epic.getEpicSubTasks()) {
            Optional<LocalDateTime> subTaskStartTime = subTask.getStartTime();
            LocalDateTime subTaskEndTime = subTask.getEndTime();

            if (subTaskStartTime.isPresent() && subTaskEndTime != null) {
                hasTimeData = true;


                if (earliestStart == null || subTaskStartTime.get().isBefore(earliestStart)) {
                    earliestStart = subTaskStartTime.get();
                }

                if (latestEnd == null || subTaskEndTime.isAfter(latestEnd)) {
                    latestEnd = subTaskEndTime;
                }

                totalDuration = totalDuration.plus(subTask.getDuration());
            }
        }

        if (hasTimeData) {
            updateEpicTimes(epic, totalDuration, earliestStart, latestEnd);
        } else {
            updateEpicTimes(epic, Duration.ZERO, null, null);
        }

    }

    private void updateEpicTimes(Epic epic, Duration duration, LocalDateTime startTime, LocalDateTime endTime) {
        epic.setDuration(duration);
        epic.setStartTime(startTime);
        epic.setEndTime(endTime);
    }

}
