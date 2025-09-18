package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpServer;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final TaskManager manager;
    private final HttpServer httpServer;
    private Gson gson;


    public HttpTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        this.httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        initGson();
        serverInitPaths();

    }

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer taskServer = new HttpTaskServer();
        taskServer.startServer();
    }

    public Gson getGson() {
        return gson;
    }

    public TaskManager getManager() {
        return manager;
    }

    private void serverInitPaths() {
        httpServer.createContext("/tasks", new TasksHandler(this));
        httpServer.createContext("/subtasks", new SubTasksHandler(this));
        httpServer.createContext("/epics", new EpicsHandler(this));
        httpServer.createContext("/history", new HistoryHandler(this));
        httpServer.createContext("/prioritized", new PrioritizedHandler(this));
    }

    public void startServer() {
        httpServer.start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }

    public void stopServer() {
        httpServer.stop(0);
    }

    private void initGson() {
        class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
            private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            @Override
            public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime) throws IOException {
                if (localDateTime == null) {
                    jsonWriter.nullValue();
                } else {
                    jsonWriter.value(localDateTime.format(formatter));
                }
            }

            @Override
            public LocalDateTime read(final JsonReader jsonReader) throws IOException {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.nextNull();
                    return null;
                }
                return LocalDateTime.parse(jsonReader.nextString(), formatter);
            }
        }

        class DurationTypeAdapter extends TypeAdapter<Duration> {

            @Override
            public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
                if (duration == null) {
                    jsonWriter.nullValue();
                } else {
                    jsonWriter.value(duration.toString());
                }
            }

            @Override
            public Duration read(JsonReader jsonReader) throws IOException {
                if (jsonReader.peek() == null) {
                    return null;
                }
                String durationString = jsonReader.nextString();
                return Duration.parse(durationString);
            }
        }

        gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();
    }


}
