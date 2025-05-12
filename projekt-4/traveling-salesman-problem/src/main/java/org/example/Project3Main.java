package org.example;

import org.example.algorithms.LocalSearch;
import org.example.algorithms.RandomHeuristic;
import org.example.algorithms.WeightedRegretHeuristicAlgorithm;
import org.example.interfaces.Neighborhood;
import org.example.swaps.BetweenCycleSwapNeighborhood;
import org.example.swaps.TwoEdgeSwapNeighborhood;
import org.example.utils.Instance;
import org.example.utils.Solution;
import org.example.utils.Utils;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Project3Main {

    public static void main(String[] args) {
        String filename = "kroA200.tsp";
        int numRuns = 10;
        Random random = new Random();

        Instance instance;
        try {
            instance = Utils.loadInstance(filename);
        } catch (IOException e) {
            System.err.println("Błąd przy wczytywaniu instancji: " + e.getMessage());
            return;
        }

        int[][] distanceMatrix = Utils.computeDistanceMatrix(instance);
        Map<Integer, List<Integer>> candidateEdges = Utils.generateCandidateVertices(distanceMatrix, 10);

        List<Neighborhood> neighborhoods = List.of(
                new TwoEdgeSwapNeighborhood(),
                new BetweenCycleSwapNeighborhood()
        );

        List<AlgorithmEntry> algorithms = List.of(
                new AlgorithmEntry("Heurystyka konstrukcyjna", (inst, matrix, rng) ->
                        new WeightedRegretHeuristicAlgorithm(1, -1).solve(inst, matrix, rng)),


                new AlgorithmEntry("Steepest bez LM", (inst, matrix, rng) -> LocalSearch.steepest( new RandomHeuristic().solve(instance, distanceMatrix, random), neighborhoods, matrix)),

                new AlgorithmEntry("Steepest z LM", (inst, matrix, rng) -> {
                    return LocalSearch.steepestWithMoveList(new RandomHeuristic().solve(inst, matrix, rng), neighborhoods, matrix);
                }),

                new AlgorithmEntry("Steepest z kandydatami", (inst, matrix, rng) -> LocalSearch.steepestWithCandidateEdges( new RandomHeuristic().solve(instance, distanceMatrix, random), neighborhoods, matrix, candidateEdges))
        );

        for (AlgorithmEntry entry : algorithms) {
            long total = 0, min = Long.MAX_VALUE, max = Long.MIN_VALUE;
            long totalTime = 0, minTime = Long.MAX_VALUE, maxTime = Long.MIN_VALUE;

            for (int i = 0; i < numRuns; i++) {
                Random rng = new Random(random.nextLong());

                Instant startTime = Instant.now();
                Solution sol = entry.algorithm.apply(instance, distanceMatrix, rng);
                Instant endTime = Instant.now();

                long cost = sol.totalCost(distanceMatrix);
                long duration = Duration.between(startTime, endTime).toMillis();

                total += cost;
                totalTime += duration;
                min = Math.min(min, cost);
                max = Math.max(max, cost);
                minTime = Math.min(minTime, duration);
                maxTime = Math.max(maxTime, duration);
            }

            double avg = total / (double) numRuns;
            double avgTime = totalTime / (double) numRuns;

            System.out.printf("%-30s | ŚR: %.2f, MIN: %d, MAX: %d\n", entry.name, avg, min, max);
            System.out.printf("                     Czas [ms] => ŚR: %.2f, MIN: %d, MAX: %d\n\n", avgTime, minTime, maxTime);
        }
    }

    static class AlgorithmEntry {
        String name;
        TriFunction<Instance, int[][], Random, Solution> algorithm;

        AlgorithmEntry(String name, TriFunction<Instance, int[][], Random, Solution> algorithm) {
            this.name = name;
            this.algorithm = algorithm;
        }
    }

    @FunctionalInterface
    interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }
}
