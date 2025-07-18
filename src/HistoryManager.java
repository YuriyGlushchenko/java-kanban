import java.util.List;

public interface HistoryManager {
    public void add(Task task);

    public List<Task> getHistory();

    public void delete(Task task);  // можно вообще добавлять свои методы в интерфейс кроме двух обязательных по условию?

    public void clearHistory();
}

