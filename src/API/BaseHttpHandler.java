package API;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseHttpHandler {
    protected record EndpointData(Endpoint endpoint, Optional<Integer> idOptional) {}

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(200, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected static EndpointData getEndpoint(String root, String requestPath, String requestMethod) {
        String patternString = String.format("^/%s(/(\\d+))?(/subtasks)?$", root);
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(requestPath);

        if (matcher.find()) {
            String id = matcher.group(2);
            boolean epicSubtasks = matcher.group(3) != null;

            switch (root) {
                case "tasks" -> {
                    if (id == null && requestMethod.equals("GET"))
                        return new EndpointData(Endpoint.GET_TASKS, Optional.empty());
                    if (id == null && requestMethod.equals("POST"))
                        return new EndpointData(Endpoint.CREATE_TASK, Optional.empty());
                    if (id != null && requestMethod.equals("GET"))
                        return new EndpointData(Endpoint.GET_TASK_BY_ID, Optional.of(Integer.parseInt(id)));
                    if (id != null && requestMethod.equals("POST"))
                        return new EndpointData(Endpoint.UPDATE_TASK, Optional.of(Integer.parseInt(id)));
                    if (id != null && requestMethod.equals("DELETE"))
                        return new EndpointData(Endpoint.DELETE_TASK, Optional.of(Integer.parseInt(id)));
                }
                case "subtasks" -> {
                    if (id == null && requestMethod.equals("GET"))
                        return new EndpointData(Endpoint.GET_SUBTASKS, Optional.empty());
                    if (id == null && requestMethod.equals("POST"))
                        return new EndpointData(Endpoint.CREATE_SUBTASK, Optional.empty());
                    if (id != null && requestMethod.equals("GET"))
                        return new EndpointData(Endpoint.GET_SUBTASK_BY_ID, Optional.of(Integer.parseInt(id)));
                    if (id != null && requestMethod.equals("POST"))
                        return new EndpointData(Endpoint.UPDATE_SUBTASK, Optional.of(Integer.parseInt(id)));
                    if (id != null && requestMethod.equals("DELETE"))
                        return new EndpointData(Endpoint.DELETE_SUBTASK, Optional.of(Integer.parseInt(id)));
                }
                case "epics" -> {
                    if (id == null && requestMethod.equals("GET"))
                        return new EndpointData(Endpoint.GET_EPICS, Optional.empty());
                    if (id == null && requestMethod.equals("POST"))
                        return new EndpointData(Endpoint.CREATE_EPIC, Optional.empty());
                    if (id != null && requestMethod.equals("GET") && !epicSubtasks)
                        return new EndpointData(Endpoint.GET_EPIC_BY_ID, Optional.of(Integer.parseInt(id)));
                    if (id != null && requestMethod.equals("GET") && epicSubtasks)
                        return new EndpointData(Endpoint.GET_EPIC_SUBTASKS, Optional.of(Integer.parseInt(id)));
                    if (id != null && requestMethod.equals("POST"))
                        return new EndpointData(Endpoint.UPDATE_EPIC, Optional.of(Integer.parseInt(id)));
                    if (id != null && requestMethod.equals("DELETE"))
                        return new EndpointData(Endpoint.DELETE_EPIC, Optional.of(Integer.parseInt(id)));
                }
            }
        }
        return new EndpointData(Endpoint.UNKNOWN, Optional.empty());
    }
}
