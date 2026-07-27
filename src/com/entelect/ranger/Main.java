package com.entelect.ranger;

import com.entelect.ranger.algorithm.RouteOptimizer;
import com.entelect.ranger.io.*;
import com.entelect.ranger.model.Problem;
import java.nio.file.*;
import java.util.*;

public final class Main {
    public static void main(String[] args) throws Exception {
        Path input = args.length == 1 ? Path.of(args[0]) : discoverInput(Path.of("input"));
        if (args.length > 1) throw new IllegalArgumentException("Usage: java ...Main [input-file]");
        Problem problem = new InputReader().read(input);
        List<String> route = new RouteOptimizer().solve(problem);
        Path answer = Path.of("output", "answers.txt"); new AnswerWriter().write(answer, route);
        System.out.printf("Solved Level %d from %s. Wrote %s (%d nodes).%n", problem.level() == Problem.Level.ONE ? 1 : 2, input, answer, route.size());
    }
    private static Path discoverInput(Path directory) throws Exception {
        if (!Files.isDirectory(directory)) throw new IllegalArgumentException("Input directory not found: " + directory.toAbsolutePath());
        try (var files = Files.list(directory)) {
            List<Path> candidates = files.filter(Files::isRegularFile).sorted().toList();
            if (candidates.size() != 1) throw new IllegalArgumentException("input/ must contain exactly one input JSON file; found " + candidates.size());
            return candidates.get(0);
        }
    }
}
