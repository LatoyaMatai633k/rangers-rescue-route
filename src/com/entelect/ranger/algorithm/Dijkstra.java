package com.entelect.ranger.algorithm;

import com.entelect.ranger.model.*;
import java.util.*;

public final class Dijkstra {
    public PathResult shortestPath(Graph graph, String source, String target) {
        Map<String, Long> distances = new HashMap<>(); Map<String, String> previous = new HashMap<>();
        PriorityQueue<State> queue = new PriorityQueue<>(Comparator.comparingLong(State::distance).thenComparing(State::node));
        distances.put(source, 0L); queue.add(new State(source, 0));
        while (!queue.isEmpty()) {
            State current = queue.remove();
            if (current.distance != distances.get(current.node)) continue;
            if (current.node.equals(target)) break;
            for (Edge edge : graph.neighbours(current.node)) {
                long candidate = Math.addExact(current.distance, edge.cost());
                long known = distances.getOrDefault(edge.destination(), Long.MAX_VALUE);
                if (candidate < known || candidate == known && current.node.compareTo(previous.getOrDefault(edge.destination(), "\uffff")) < 0) {
                    distances.put(edge.destination(), candidate); previous.put(edge.destination(), current.node); queue.add(new State(edge.destination(), candidate));
                }
            }
        }
        Long cost = distances.get(target); if (cost == null) throw new IllegalArgumentException("No route from " + source + " to " + target);
        LinkedList<String> route = new LinkedList<>(); for (String node = target; node != null; node = previous.get(node)) route.addFirst(node);
        return new PathResult(cost, List.copyOf(route));
    }
    private record State(String node, long distance) { }
    public record PathResult(long cost, List<String> route) { }
}
