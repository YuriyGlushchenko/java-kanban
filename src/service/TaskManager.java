package service;

import model.Epic;
import model.SubTask;
import model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface TaskManager {
    ArrayList<Task> getAllTasks();

    ArrayList<SubTask> getAllSubTasks();

    ArrayList<Epic> getAllEpics();


    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubTasks();


    void deleteTask(int id);

    void deleteSubTask(int id);

    void deleteEpic(int id);


    Optional<Task> getTaskById(int id);

    Optional<Epic> getEpicById(int id);

    Optional<SubTask> getSubTaskById(int id);


    int addNewTask(Task newTask);

    int addNewSubTask(SubTask newSubTask);

    int addNewEpic(Epic newEpic);


    void updateTask(Task task);

    void updateSubTask(SubTask subTask);

    void updateEpic(Epic epic);


    ArrayList<SubTask> getEpicSubTasks(int id);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
