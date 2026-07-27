# Ranger's Rescue Route Solver

A dependency-free Java application for the Entelect Ranger's Rescue Route challenge. It reads one JSON adjacency-list input, detects the challenge level from the edge schema, and writes the required JSON route to `output/answers.txt`.

## Requirements

- Java 17 or newer (the project uses only the Java standard library)
- An input graph containing `A` (start) and `B` (destination)

## Run

Place exactly one JSON input file in `input/`, then run from the project root:

```powershell
javac -d build (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object FullName)
java -cp build com.entelect.ranger.Main
```

To use an explicit file instead of input discovery:

```powershell
java -cp build com.entelect.ranger.Main path\to\input.json
```

The generated submission file is `output/answers.txt` and has this required form:

```json
{
  "route": ["A", "D", "E", "B"]
}
```

## Input detection and assumptions

The official specification's JSON examples are bare adjacency-list objects, with no `level` property. Therefore the solver detects:

- Level 1: every edge has `node` and integer `weight`.
- Level 2: every edge has `node`, integer `time`, and integer `risk`; its cost is `time + risk`.

For Level 2, all required stations are node names matching `S<number>` (such as `S1` through `S4`). This implements the official station naming convention while avoiding a hard-coded count. The specification describes undirected, connected graphs; the parser preserves the supplied adjacency list so valid directed input is also handled correctly.

## Algorithms

- **Dijkstra:** Computes each shortest path with non-negative edge costs. Complexity per run: `O((V + E) log V)` time and `O(V)` memory.
- **Held-Karp subset DP (Level 2):** Builds a shortest-path metric between `A`, all stations, and `B`, then exactly finds the best station order. For `k` stations: `O(2^k k^2)` time and `O(2^k k)` memory, in addition to Dijkstra runs. With the four official stations, this is tiny and exact.

The paths for the selected legs are concatenated without repeated junction nodes. No graph or route data is hard-coded.

## Structure

```text
src/com/entelect/ranger/
  Main.java                 application entry point and input discovery
  json/JsonParser.java      dependency-free JSON parser
  io/InputReader.java       validation, level detection, graph construction
  io/AnswerWriter.java      submission-format JSON writer
  model/                    immutable graph and problem model
  algorithm/Dijkstra.java   single-leg shortest paths
  algorithm/RouteOptimizer.java  exact Level 1/Level 2 route selection
input/1.txt                 supplied Level 1 example
output/answers.txt          generated submission (after running)
```

## Verification strategy

- Unit-test Dijkstra on disconnected, one-edge, equal-cost, and large-weight graphs.
- Test the input parser with malformed JSON, mixed Level 1/Level 2 schemas, missing `A`/`B`, missing station names, and negative costs.
- For Level 2, compare the DP result against exhaustive permutations for small random graphs.
- For performance, generate sparse and dense connected graphs and measure memory/runtime separately from parsing.

The included `input/1.txt` should produce `A -> D -> E -> B`, total cost 9.

## Leaderboard optimisation opportunities

1. **Highest impact:** Keep exact Dijkstra + exact station-order DP. It reaches the true optimum and therefore the maximum score on the specified two levels.
2. **If future inputs have many stations:** use an A* or branch-and-bound ordering search with an admissible lower bound; it can reduce explored states but must preserve exactness.
3. **For very large sparse graphs:** run Dijkstra only from required terminals (already done), use primitive-array node IDs, and reuse allocation-heavy buffers.
4. **For dense graphs with many terminal queries:** consider all-pairs preprocessing only when its measured cost beats repeated Dijkstra; it is not preferable for the official sparse examples.
