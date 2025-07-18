import java.util.List;

public interface HistoryManager {
     void add(Task task);

     List<Task> getHistory();

     void delete(Task task);  // можно вообще добавлять свои методы в интерфейс кроме двух обязательных по условию?

     void clearHistory();
}

