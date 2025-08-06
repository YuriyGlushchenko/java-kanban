package service;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final HashMap<Integer, HistoryNode> historyMap = new HashMap<>();
    private HistoryNode head = null;
    private HistoryNode tail = null;

    @Override
    public void add(Task task) {
        HistoryNode newHistoryNode = new HistoryNode(task);
        if (historyMap.containsKey(task.getId())) {
            remove(task.getId()); // можно изменить next и prev вместо удаления, тогда и добавлять не надо
        }
        addNodeToHistory(newHistoryNode);
    }

    private void addNodeToHistory(HistoryNode newNode) {
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        historyMap.put(newNode.data.getId(), newNode);
    }


    @Override
    public List<Task> getHistory() {
        ArrayList<Task> history = new ArrayList<>();
        for(HistoryNode node = head; node != null; node = node.next ){
            history.add(node.data);
        }
        return history;
    }

    @Override
    public void clearHistory() {
        head = null;
        tail = null;
        historyMap.clear();
    }

    @Override
    public void remove(int id) {
        if (!historyMap.containsKey(id)) return;
        HistoryNode targetNode = historyMap.get(id);
        HistoryNode prevNode = targetNode.prev;
        HistoryNode nextNode = targetNode.next;

        if (prevNode == null) {
            head = nextNode;
        } else {
            prevNode.next = nextNode;
        }

        if (nextNode == null) {
            tail = prevNode;
        } else {
            nextNode.prev = prevNode;
        }

        historyMap.remove(id);
    }

    static class HistoryNode {
        private Task data;
        HistoryNode next;
        HistoryNode prev;

        public HistoryNode(Task data) {
            this.data = data;
        }


        public Task getData() {
            return data;
        }

        public void setData(Task data) {
            this.data = data;
        }

    }
}
