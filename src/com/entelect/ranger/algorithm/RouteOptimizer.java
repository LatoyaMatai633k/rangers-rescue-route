package com.entelect.ranger.algorithm;

import com.entelect.ranger.model.Problem;
import java.util.*;

/** Exact optimiser: Dijkstra metric closure plus Held-Karp dynamic programming. */
public final class RouteOptimizer {
    private final Dijkstra dijkstra = new Dijkstra();

    public List<String> solve(Problem problem) {
        List<String> terminals = new ArrayList<>(); terminals.add(problem.start()); terminals.addAll(problem.requiredStops()); terminals.add(problem.destination());
        Dijkstra.PathResult[][] paths = new Dijkstra.PathResult[terminals.size()][terminals.size()];
        for (int i = 0; i < terminals.size(); i++) for (int j = 0; j < terminals.size(); j++) if (i != j) paths[i][j] = dijkstra.shortestPath(problem.graph(), terminals.get(i), terminals.get(j));
        int stops = problem.requiredStops().size();
        if (stops == 0) return paths[0][1].route();
        int states = 1 << stops; long[][] cost = new long[states][stops]; int[][] parent = new int[states][stops];
        for (long[] row : cost) Arrays.fill(row, Long.MAX_VALUE); for (int[] row : parent) Arrays.fill(row, -1);
        for (int last = 0; last < stops; last++) cost[1 << last][last] = paths[0][last + 1].cost();
        for (int mask = 1; mask < states; mask++) for (int last = 0; last < stops; last++) if ((mask & (1 << last)) != 0 && cost[mask][last] != Long.MAX_VALUE)
            for (int next = 0; next < stops; next++) if ((mask & (1 << next)) == 0) {
                int nextMask = mask | (1 << next); long candidate = Math.addExact(cost[mask][last], paths[last + 1][next + 1].cost());
                if (candidate < cost[nextMask][next]) { cost[nextMask][next] = candidate; parent[nextMask][next] = last; }
            }
        int full = states - 1, last = -1; long best = Long.MAX_VALUE;
        for (int candidate = 0; candidate < stops; candidate++) { long total = Math.addExact(cost[full][candidate], paths[candidate + 1][stops + 1].cost()); if (total < best) { best = total; last = candidate; } }
        LinkedList<Integer> order = new LinkedList<>(); for (int mask = full; last != -1;) { order.addFirst(last); int before = parent[mask][last]; mask ^= 1 << last; last = before; }
        List<Integer> indexes = new ArrayList<>(); indexes.add(0); order.forEach(i -> indexes.add(i + 1)); indexes.add(stops + 1);
        List<String> route = new ArrayList<>(); for (int i = 1; i < indexes.size(); i++) { List<String> leg = paths[indexes.get(i - 1)][indexes.get(i)].route(); route.addAll(route.isEmpty() ? leg : leg.subList(1, leg.size())); }
        return List.copyOf(route);
    }
}
