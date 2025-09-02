package service;

import model.Epic;
import model.SubTask;
import model.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    ArrayList<Task> getAllTasks();

    ArrayList<Epic> getAllEpics();

    ArrayList<SubTask> getAllSubTasks();

    ArrayList<Task> getAllTypesTask();

    void deleteAllTasks();

    Task getAnyTypeTaskById(int id);

    int addAnyTypeTask(Task newTask);

    void updateTask(Task task);

    void deleteAnyTypeTask(int id);

    ArrayList<SubTask> getEpicSubTasks(int id);

    List<Task> getHistory();

    Task getTaskById(int id);

    Epic getEpicById(int id);

    SubTask getSubTaskById(int id);
}
