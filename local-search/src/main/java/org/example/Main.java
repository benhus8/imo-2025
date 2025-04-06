package org.example;

import org.example.algorithms.GreedyCycleAlgorithm;
import org.example.algorithms.NearestNeighborAlgorithm;
import org.example.algorithms.RegretHeuristicAlgorithm;
import org.example.algorithms.WeightedRegretHeuristicAlgorithm;
import org.example.utils.Instance;
import org.example.utils.Utils;

import java.io.IOException;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        String filename = "kroA200.tsp";

        Instance instance;
        try {
            instance = Utils.loadInstance(filename);
        } catch (IOException e) {
            System.err.println("Error loading instance: " + e.getMessage());
            return;
        }
        int[][] distanceMatrix = Utils.computeDistanceMatrix(instance);

        int numRuns = 100;
        Random random = new Random();

        System.out.println("Heurystyka najbliższego sąsiada:");
        Utils.runExperiments("Nearest Neighbor", new NearestNeighborAlgorithm(), instance, distanceMatrix, numRuns, random);

        System.out.println("Heurystyka weight  żal:");
        Utils.runExperiments("Ważony żal", new WeightedRegretHeuristicAlgorithm(1, -1), instance, distanceMatrix, numRuns, random);


        System.out.println("\nMetoda rozbudowy cyklu:");
        Utils.runExperiments("Greedy Cycle", new GreedyCycleAlgorithm(), instance, distanceMatrix, numRuns, random);

        System.out.println("\nHeurystyka z żalem (2-regret):");
        Utils.runExperiments("Regret Heuristic", new RegretHeuristicAlgorithm(), instance, distanceMatrix, numRuns, random);
    }
}