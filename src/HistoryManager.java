import java.util.List;

public interface HistoryManager {
     void add(Task task);

     List<Task> getHistory();

     void clearHistory();

     void remove(int id);
}

