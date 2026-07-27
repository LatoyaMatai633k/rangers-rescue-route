package com.entelect.ranger.model;

import java.util.*;

/** Immutable directed view of the supplied adjacency list. */
public final class Graph {
    private final Map<String, List<Edge>> adjacency;

    public Graph(Map<String, List<Edge>> adjacency) {
        Map<String, List<Edge>> copy = new TreeMap<>();
        adjacency.forEach((node, edges) -> copy.put(node, List.copyOf(edges)));
        this.adjacency = Collections.unmodifiableMap(copy);
    }

    public List<Edge> neighbours(String node) { return adjacency.getOrDefault(node, List.of()); }
    public boolean contains(String node) { return adjacency.containsKey(node); }
    public Set<String> nodes() { return adjacency.keySet(); }
}
