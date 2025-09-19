package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static Gson getGson() {

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

        return new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();
    }
}
