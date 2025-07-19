import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager{
    private final static int HISTORY_LIMIT = 10;
    private final LinkedList<Task> historyList = new LinkedList<>();

    @Override
    public void add(Task task) {
        historyList.addLast(task);
        // В ТЗ не указано, история просмотров? или история изменений, тогда нужен clone:
//        try{
//            historyList.addLast((Task) super.clone());
//        } catch (CloneNotSupportedException e){
//            throw new AssertionError("Класс поддерживает клонирование", e);
//        }

        if(historyList.size() > HISTORY_LIMIT){
            historyList.removeFirst();
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyList;
    }

    @Override
    public void delete(Task task){
        while(historyList.contains(task)){
            historyList.remove(task);
        }
    }

    @Override
    public void clearHistory() {
        historyList.clear();
    }
}
