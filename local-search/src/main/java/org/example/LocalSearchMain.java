package org.example;

import org.example.algorithms.*;
import org.example.interfaces.Heuristic;
import org.example.interfaces.Neighborhood;
import org.example.swaps.BetweenCycleSwapNeighborhood;
import org.example.swaps.TwoEdgeSwapNeighborhood;
import org.example.swaps.WithinCycleSwapNeighborhood;
import org.example.utils.Instance;
import org.example.utils.Solution;
import org.example.utils.Utils;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class LocalSearchMain {

    public static void main(String[] args) {
        String filename = "kroA200.tsp";
        int numRuns = 100;
        Random random = new Random();

        Instance instance;
        try {
            instance = Utils.loadInstance(filename);
        } catch (IOException e) {
            System.err.println("Błąd przy wczytywaniu instancji: " + e.getMessage());
            return;
        }

        int[][] distanceMatrix = Utils.computeDistanceMatrix(instance);

        List<Heuristic> startMethods = List.of(
                new WeightedRegretHeuristicAlgorithm(1, -1),
                new RandomHeuristic()
        );

        List<List<Neighborhood>> allNeighborhoods = List.of(
                List.of(new WithinCycleSwapNeighborhood(), new BetweenCycleSwapNeighborhood()), // Swap + Swap
                List.of(new TwoEdgeSwapNeighborhood(), new BetweenCycleSwapNeighborhood())       // Swap + 2-Edge
        );

        String[] searchNames = {"Greedy", "Steepest", "RandomWalk"};
        int maxIterations = 0;
        for (Heuristic startMethod : startMethods) {
            String startName = (startMethod instanceof RandomHeuristic) ? "Losowy start" : "Heurystyczny start";
            for (int nId = 0; nId < allNeighborhoods.size(); nId++) {
                List<Neighborhood> neighborhoods = allNeighborhoods.get(nId);
                String neighborhoodName = (nId == 0) ? "Swap + Swap" : "Swap + 2-Edge";

                for (String search : searchNames) {
                    long total = 0, min = Long.MAX_VALUE, max = Long.MIN_VALUE;
                    long totalTime = 0, minTime = Long.MAX_VALUE, maxTime = Long.MIN_VALUE;

                    for (int i = 0; i < numRuns; i++) {
                        Solution initial = startMethod.solve(instance, distanceMatrix, new Random(random.nextLong()));
                        Solution result;

                        Instant startTime = Instant.now();


                        if (search.equals("Greedy")) {
                            result = LocalSearch.greedy(initial, neighborhoods, distanceMatrix, new Random(random.nextLong()));
                            maxIterations = Math.max(result.iterations, maxIterations);
                        } else if (search.equals("Steepest")) {
                            result = LocalSearch.steepest(initial, neighborhoods, distanceMatrix);
                            maxIterations = Math.max(result.iterations, maxIterations);
                        } else if (search.equals("RandomWalk")) {
                            result = RandomWalk.randomWalk(initial, neighborhoods, distanceMatrix, new Random(random.nextLong()), maxIterations);
                        } else {
                            throw new IllegalArgumentException("Nieznana metoda przeszukiwania: " + search);
                        }

                        Instant endTime = Instant.now();
                        long duration = Duration.between(startTime, endTime).toMillis();

                        long cost = result.totalCost(distanceMatrix);
                        total += cost;
                        totalTime += duration;

                        min = Math.min(min, cost);
                        max = Math.max(max, cost);

                        minTime = Math.min(minTime, duration);
                        maxTime = Math.max(maxTime, duration);
                    }

                    double avg = total / (double) numRuns;
                    double avgTime = totalTime / (double) numRuns;

                    System.out.printf(
                            "START: %-17s | SĄSIEDZTWO: %-13s | ALGORYTM: %-8s => ŚR: %.2f, MIN: %d, MAX: %d\n",
                            startName, neighborhoodName, search, avg, min, max
                    );
                    System.out.printf(
                            "                            Czas [ms] => ŚR: %.2f, MIN: %d, MAX: %d\n\n",
                            avgTime, minTime, maxTime
                    );
                }
            }
        }

    }
}
