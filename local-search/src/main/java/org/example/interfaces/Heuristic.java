package org.example.interfaces;

import org.example.utils.Insertion;
import org.example.utils.Instance;
import org.example.utils.Solution;

import java.util.List;
import java.util.Random;

public interface Heuristic {
    Solution solve(Instance instance, int[][] distanceMatrix, Random random);

    default Insertion bestInsertionCost(List<Integer> cycle, int vertex, int[][] matrix) {
        int bestPos = 0;
        int bestCost = Integer.MAX_VALUE;
        int m = cycle.size();
        for (int i = 0; i < m; i++) {
            int j = (i + 1) % m;
            int cost = matrix[cycle.get(i)][vertex] + matrix[vertex][cycle.get(j)] - matrix[cycle.get(i)][cycle.get(j)];
            if (cost < bestCost) {
                bestCost = cost;
                bestPos = j;
            }
        }
        return new Insertion(bestPos, bestCost);
    }

    default int findNearest(int current, List<Integer> candidates, int[][] matrix) {
        int best = candidates.getFirst();
        int bestDist = matrix[current][best];
        for (int v : candidates) {
            int d = matrix[current][v];
            if (d < bestDist) {
                bestDist = d;
                best = v;
            }
        }
        return best;
    }
}
