import java.util.Objects;

public class Task {
    private final int id;
    private String title;
    private Status status;
    private String description;

    {
        status = Status.NEW;
        id = ++Stat.id;
        Stat.counter++;
    }

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Task() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    static class Stat{
        public static int counter = 0;
        public static int id;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                '}';
    }
}
