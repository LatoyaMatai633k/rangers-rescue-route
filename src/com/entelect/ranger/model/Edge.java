package com.entelect.ranger.model;

/** Directed adjacency-list entry. Cost is already risk-adjusted when applicable. */
public record Edge(String destination, long cost) {
    public Edge {
        if (destination == null || destination.isBlank()) throw new IllegalArgumentException("Edge destination is required");
        if (cost < 0) throw new IllegalArgumentException("Dijkstra requires non-negative costs");
    }
}
