package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class Task implements Comparable<Task> {
    protected Type type;
    private int id = -1;
    private String title;
    private Status status = Status.NEW;
    private String description;
    private Duration duration = Duration.ZERO;
    private LocalDateTime startTime;

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Task(String title, String description, int id) {
        this.title = title;
        this.description = description;
        this.id = id;
    }

    public Task() {
    }

    public Type getType() {
        return Type.TASK;
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

    public void setId(int id) {
        if (this.id == -1) {
            this.id = id;
        } else {
            throw new UnsupportedOperationException("Изменение id задачи запрещено");
        }
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

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                '}';
    }

    public LocalDateTime getEndTime(){
        if (startTime != null){
            return startTime.plus(duration);
        } else {
            return null;
        }
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Optional<LocalDateTime> getStartTime() {
        return Optional.ofNullable(startTime);
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public int compareTo(Task other) {
        if (this.startTime == null && other.startTime == null) {
            return 0;
        }
        if (this.startTime == null) {
            return 1;
        }
        if (other.startTime == null) {
            return -1;
        }

        return this.startTime.compareTo(other.startTime);
    }
}
