package org.example.algorithms;

import org.example.interfaces.Heuristic;
import org.example.utils.Insertion;
import org.example.utils.Instance;
import org.example.utils.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.example.utils.Utils.getAllVertices;

public class GreedyCycleAlgorithm implements Heuristic {
    public Solution solve(Instance instance, int[][] distanceMatrix, Random random) {
        int n = instance.dimension;
        int target1 = n / 2 + n % 2;
        int target2 = n / 2;

        List<Integer> allVertices = getAllVertices(instance);

        int start1 = allVertices.remove(random.nextInt(allVertices.size()));
        int nearest1 = findNearest(start1, allVertices, distanceMatrix);
        allVertices.remove(Integer.valueOf(nearest1));
        List<Integer> cycle1 = new ArrayList<>();
        cycle1.add(start1);
        cycle1.add(nearest1);

        int start2 = allVertices.remove(random.nextInt(allVertices.size()));
        int nearest2 = findNearest(start2, allVertices, distanceMatrix);
        allVertices.remove(Integer.valueOf(nearest2));
        List<Integer> cycle2 = new ArrayList<>();
        cycle2.add(start2);
        cycle2.add(nearest2);


        while (!allVertices.isEmpty() && (cycle1.size() < target1 || cycle2.size() < target2)) {
            Insertion bestInsertion = null;
            int chosenVertex = -1;
            int chosenCycle = -1; // 1 dla cycle1, 2 dla cycle2

            for (int v : allVertices) {
                if (cycle1.size() < target1) {
                    Insertion ins = bestInsertionCost(cycle1, v, distanceMatrix);
                    if (bestInsertion == null || ins.cost < bestInsertion.cost) {
                        bestInsertion = ins;
                        chosenVertex = v;
                        chosenCycle = 1;
                    }
                }
                if (cycle2.size() < target2) {
                    Insertion ins = bestInsertionCost(cycle2, v, distanceMatrix);
                    if (bestInsertion == null || ins.cost < bestInsertion.cost) {
                        bestInsertion = ins;
                        chosenVertex = v;
                        chosenCycle = 2;
                    }
                }
            }
            if (chosenVertex == -1)
                break;
            if (chosenCycle == 1) {
                cycle1.add(bestInsertion.insertPosition, chosenVertex);
            } else {
                cycle2.add(bestInsertion.insertPosition, chosenVertex);
            }
            allVertices.remove(Integer.valueOf(chosenVertex));
        }

        return new Solution(cycle1, cycle2);
    }

}
