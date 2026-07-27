package com.entelect.ranger.model;

import java.util.List;

public record Problem(Graph graph, Level level, String start, String destination, List<String> requiredStops) {
    public enum Level { ONE, TWO }
}
