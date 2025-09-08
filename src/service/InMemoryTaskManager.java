package service;

import exeptions.TimeIntersectionException;
import model.Epic;
import model.SubTask;
import model.Task;
import model.Type;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    protected int idCounter = 0;
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>();

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

//    public ArrayList<Task> getAllTypesTask() {
//        ArrayList<Task> allTasks = getAllTasks();
//        allTasks.addAll(getAllEpics());
//        allTasks.addAll(getAllSubTasks());
//        return allTasks;
//    }

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
        subTasks.keySet().forEach(historyManager::remove);
        epics.keySet().forEach(historyManager::remove);
        epics.clear();
        subTasks.clear();
    }


//    public Task getAnyTypeTaskById(int id) {
//        Optional<? extends Task> requestedTaskOptional = Stream.of(tasks, subTasks, epics)
//                .filter(hMap -> hMap.containsKey(id))
//                .map(hMap -> hMap.get(id))
//                .findFirst();
//        if (requestedTaskOptional.isPresent()) {
//            Task requestedTask = requestedTaskOptional.get();
//            historyManager.add(requestedTask);
//            return requestedTask;
//        } else {
//            throw new NoSuchElementException("Нет задачи с таким id");
//        }
//    }

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
    public Epic getEpicById(int id) {
        Epic requestedEpic = epics.get(id);
        if (requestedEpic != null) {
            historyManager.add(requestedEpic);
            return requestedEpic;
        } else {
            throw new NoSuchElementException("Нет задачи с таким id");
        }
    }


//    public int addAnyTypeTask(Task newTask) {
////        if (newTask.getId() == -1) {
////            int newItemId = ++idCounter;
////            newTask.setId(newItemId);
////        }
//
//        Type newTaskType = newTask.getType();
//        switch (newTaskType) {
//            case TASK -> addNewTask(newTask);
//            case SUBTASK -> addNewSubTask((SubTask) newTask);
//            case EPIC -> addNewEpic((Epic) newTask);
//        }
//        return newTask.getId();
//    }




//    public void updateAnyTypeTask(Task task) {
////        if (hasTimeConflict(task)) return;
//        Type taskType = task.getType();
////        if (task.getStartTime().isPresent() && task.getType() != Type.EPIC) {
////            prioritizedTasks.add(task);
////        }
//        switch (taskType) {
//            case TASK -> updateTask(task);
//            case SUBTASK -> {
//                updateSubTask((SubTask) task);
////                SubTask updatedSubTask = (SubTask) task;
////                subTasks.put(updatedSubTask.getId(), updatedSubTask);
////                checkEpicStatusIsChanged(updatedSubTask.getParentEpicId());
//            }
//            case EPIC -> updateEpic((Epic) task);
//        }
//    }

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
//        SubTask updatedSubTask = (SubTask) task;
        subTasks.put(subTask.getId(), subTask);
        checkEpicStatusIsChanged(subTask.getParentEpicId());
    }

    @Override
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }


//    public void deleteAnyTypeTask(int id) {
//        Type taskToDeleteType = getAnyTypeTaskById(id).getType();
//        switch (taskToDeleteType) {
//            case TASK -> deleteTask(id);
//            case SUBTASK -> deleteSubTask(id);
//            case EPIC -> deleteEpic(id);
//        }
//    }

//    public void setCounter(int counter) {
//        this.idCounter = counter;
//    }

    @Override
    public void deleteTask(int id) {
        historyManager.remove(id);
        Task taskToDelete = tasks.get(id);
        if(taskToDelete != null){
            prioritizedTasks.remove(tasks.get(id));
            tasks.remove(id);
        }
    }

    @Override
    public void deleteSubTask(int id) {
        historyManager.remove(id);
        SubTask subTaskToDelete = subTasks.get(id);
        if(subTaskToDelete != null){
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
        if(epicToDelete != null){
            for (SubTask subTask : epicToDelete.getEpicSubTasks()) {
                historyManager.remove(subTask.getId());
                subTasks.remove(subTask.getId());
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

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private void setIdToTask(Task task){
        if (task.getId() == -1) {
            int newItemId = ++idCounter;
            task.setId(newItemId);
        }
    }

    @Override
    public int addNewTask(Task newTask) {
        if (hasTimeConflict(newTask)) throw new TimeIntersectionException("Задача не добавлена: пересечение по времени");
        setIdToTask(newTask);
        tasks.put(newTask.getId(), newTask);
        if (newTask.getStartTime().isPresent()) {
            prioritizedTasks.add(newTask);
        }
        return newTask.getId();
    }

    @Override
    public int addNewSubTask(SubTask newSubTask) {
        if (hasTimeConflict(newSubTask)) throw new TimeIntersectionException("Задача не добавлена: пересечение по времени");
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
