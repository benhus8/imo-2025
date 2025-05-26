package org.example;

import org.example.algorithms.LocalSearch;
import org.example.algorithms.RandomHeuristic;
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
import java.util.Random;

public class Project4Main {

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

        List<Neighborhood> neighborhoods = List.of(new TwoEdgeSwapNeighborhood(), new BetweenCycleSwapNeighborhood());

        // calculate the avg mls time
        int mslsRuns = 2;
        long startMSLSTime = System.currentTimeMillis();
        LocalSearch.multipleStartLocalSearch(instance, neighborhoods, distanceMatrix, mslsRuns, new Random());
        long endMSLSTime = System.currentTimeMillis();
        long avgMSLSTime = (endMSLSTime - startMSLSTime) / mslsRuns;
        System.out.println("Avg MSLS time: " + avgMSLSTime + "ms");


        List<QuadAlgorithmEntry> algorithms = List.of(
                new QuadAlgorithmEntry("MSLS", (inst, matrix, rng, startSolution) -> LocalSearch.multipleStartLocalSearch(instance, neighborhoods, matrix, 200, rng)),

                new QuadAlgorithmEntry("ILS", (inst, matrix, rng, startSolution) -> LocalSearch.iteratedLocalSearch(startSolution, neighborhoods, matrix, avgMSLSTime, rng)),

                new QuadAlgorithmEntry("LNS", (inst, matrix, rng, startSolution) -> LocalSearch.largeNeighborhoodSearch(startSolution, neighborhoods, matrix, avgMSLSTime, rng))
                );


        for (QuadAlgorithmEntry entry : algorithms) {
            long total = 0, min = Long.MAX_VALUE, max = Long.MIN_VALUE;
            long totalTime = 0, minTime = Long.MAX_VALUE, maxTime = Long.MIN_VALUE;

            for (int i = 0; i < numRuns; i++) {
                System.out.println("Uruchamiam " + entry.name + " - uruchomienie nr " + (i + 1) + " z " + numRuns);
                Random rng = new Random(random.nextLong());

                Solution randomStartSolution = new RandomHeuristic().solve(instance, distanceMatrix, rng);

                Instant startTime = Instant.now();
                Solution sol = entry.algorithm.apply(instance, distanceMatrix, rng, randomStartSolution);
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

    @FunctionalInterface
    interface QuadFunction<A, B, C, D, R> {
        R apply(A a, B b, C c, D d);
    }

    static class QuadAlgorithmEntry {
        String name;
        QuadFunction<Instance, int[][], Random, Solution, Solution> algorithm;

        QuadAlgorithmEntry(String name, QuadFunction<Instance, int[][], Random, Solution, Solution> algorithm) {
            this.name = name;
            this.algorithm = algorithm;
        }
    }
}
