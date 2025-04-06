package org.example.utils;

import org.example.interfaces.Heuristic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utils {

    public static Instance loadInstance(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        int dimension = 0;
        List<Point> points = new ArrayList<>();
        boolean inNodeSection = false;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("DIMENSION")) {
                // Przykładowa linia: "DIMENSION: 200"
                String[] parts = line.split(":");
                if (parts.length > 1) {
                    dimension = Integer.parseInt(parts[1].trim());
                }
            } else if (line.equals("NODE_COORD_SECTION")) {
                inNodeSection = true;
            } else if (line.equals("EOF")) {
                break;
            } else if (inNodeSection) {

                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    points.add(new Point(x, y));
                }
            }
        }
        br.close();
        if (points.size() != dimension) {
            System.out.println("Uwaga: liczba wczytanych punktów (" + points.size() +
                               ") nie zgadza się z deklarowaną DIMENSION (" + dimension + ")");
        }
        Instance inst = new Instance();
        inst.dimension = points.size();
        inst.points = points.toArray(new Point[points.size()]);
        return inst;
    }


    public static int[][] computeDistanceMatrix(Instance instance) {
        int n = instance.dimension;
        int[][] matrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                if (i == j) {
                    matrix[i][j] = 0;
                } else {
                    double dx = instance.points[i].x - instance.points[j].x;
                    double dy = instance.points[i].y - instance.points[j].y;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    int d = (int) Math.round(dist);
                    matrix[i][j] = d;
                    matrix[j][i] = d;
                }
            }
        }
        return matrix;
    }


    public static void runExperiments(String heuristicName, Heuristic heuristic, Instance instance, int[][] distanceMatrix, int numRuns, Random random) {
        long total = 0;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (int i = 0; i < numRuns; i++) {

            Solution sol = heuristic.solve(instance, distanceMatrix, new Random(random.nextLong()));
            long cost = sol.totalCost(distanceMatrix);
            total += cost;
            if (cost < min) min = cost;
            if (cost > max) max = cost;
        }
        double average = total / (double) numRuns;
        System.out.println(heuristicName + " (100 uruchomień):");
        System.out.println("Średnia: " + average + ", Minimum: " + min + ", Maksimum: " + max);
    }

    public static List<Integer> getAllVertices(Instance instance) {
        List<Integer> allVertices = new ArrayList<>();
        for (int i = 0; i < instance.dimension; i++) {
            allVertices.add(i);
        }
        return allVertices;
    }

}

