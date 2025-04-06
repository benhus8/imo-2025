package org.example.algorithms;

import org.example.interfaces.Heuristic;
import org.example.utils.Instance;
import org.example.utils.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.example.utils.Utils.getAllVertices;

public class NearestNeighborAlgorithm implements Heuristic {
    public Solution solve(Instance instance, int[][] distanceMatrix, Random random) {
        int n = instance.dimension;

        int target1 = n / 2 + n % 2;
        int target2 = n / 2;

        List<Integer> allVertices = getAllVertices(instance);

        int start1 = allVertices.remove(random.nextInt(allVertices.size()));

        int start2 = -1;
        int maxDist = -1;
        for (int v : allVertices) {
            if (distanceMatrix[start1][v] > maxDist) {
                maxDist = distanceMatrix[start1][v];
                start2 = v;
            }
        }
        allVertices.remove(Integer.valueOf(start2));

        List<Integer> cycle1 = new ArrayList<>();
        List<Integer> cycle2 = new ArrayList<>();
        cycle1.add(start1);
        cycle2.add(start2);


        boolean turn = true;
        while (!allVertices.isEmpty() && (cycle1.size() < target1 || cycle2.size() < target2)) {
            if (turn && cycle1.size() < target1) {
                int last = cycle1.getLast();
                int next = findNearest(last, allVertices, distanceMatrix);
                cycle1.add(next);
                allVertices.remove(Integer.valueOf(next));
            } else if (!turn && cycle2.size() < target2) {
                int last = cycle2.getLast();
                int next = findNearest(last, allVertices, distanceMatrix);
                cycle2.add(next);
                allVertices.remove(Integer.valueOf(next));
            }

            if (cycle1.size() >= target1 && cycle2.size() < target2) {
                turn = false;
            } else if (cycle2.size() >= target2 && cycle1.size() < target1) {
                turn = true;
            } else {
                turn = !turn;
            }
        }

        return new Solution(cycle1, cycle2);
    }

}
