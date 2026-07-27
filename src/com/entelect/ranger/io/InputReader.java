package com.entelect.ranger.io;

import com.entelect.ranger.json.JsonParser;
import com.entelect.ranger.model.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

public final class InputReader {
    private static final Pattern STATION = Pattern.compile("S\\d+");

    public Problem read(Path file) throws IOException {
        Object root = JsonParser.parse(Files.readString(file));
        if (!(root instanceof Map<?, ?> rawGraph)) throw new IllegalArgumentException("Root JSON value must be an adjacency-list object");
        Map<String, List<Edge>> graph = new HashMap<>();
        Problem.Level level = null;
        for (Map.Entry<?, ?> entry : rawGraph.entrySet()) {
            if (!(entry.getKey() instanceof String node) || !(entry.getValue() instanceof List<?> neighbours)) throw new IllegalArgumentException("Each graph entry must map a node name to an array");
            List<Edge> edges = new ArrayList<>();
            for (Object neighbour : neighbours) {
                if (!(neighbour instanceof Map<?, ?> data) || !(data.get("node") instanceof String target)) throw new IllegalArgumentException("Each neighbour needs a string 'node'");
                boolean one = data.containsKey("weight"), two = data.containsKey("time") && data.containsKey("risk");
                if (one == two) throw new IllegalArgumentException("Each edge must use exactly 'weight' or both 'time' and 'risk'");
                Problem.Level edgeLevel = one ? Problem.Level.ONE : Problem.Level.TWO;
                if (level != null && level != edgeLevel) throw new IllegalArgumentException("Input mixes Level 1 and Level 2 edge schemas");
                level = edgeLevel;
                long cost = one ? number(data.get("weight"), "weight") : Math.addExact(number(data.get("time"), "time"), number(data.get("risk"), "risk"));
                edges.add(new Edge(target, cost));
            }
            graph.put(node, edges);
        }
        if (level == null) throw new IllegalArgumentException("Graph contains no edges; level cannot be detected");
        Graph result = new Graph(graph);
        if (!result.contains("A") || !result.contains("B")) throw new IllegalArgumentException("The graph must contain start node A and destination node B");
        List<String> stops = level == Problem.Level.TWO ? result.nodes().stream().filter(node -> STATION.matcher(node).matches()).sorted().toList() : List.of();
        if (level == Problem.Level.TWO && stops.isEmpty()) throw new IllegalArgumentException("Level 2 requires station nodes named S<number>");
        return new Problem(result, level, "A", "B", stops);
    }

    private long number(Object value, String field) {
        if (!(value instanceof Number number) || Math.rint(number.doubleValue()) != number.doubleValue()) throw new IllegalArgumentException("'" + field + "' must be an integer");
        return number.longValue();
    }
}
