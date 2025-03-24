package org.example.implementations;

import org.example.interfaces.Heuristic;
import org.example.utils.Insertion;
import org.example.utils.Instance;
import org.example.utils.Solution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.example.utils.Utils.getAllVertices;


public class RegretHeuristicAlgorithm implements Heuristic {
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
            double bestRegret = -1;
            int bestVertex = -1;
            int bestCycle = -1;
            Insertion bestIns = null;

            for (int v : allVertices) {
                Insertion ins1 = null, ins2 = null;
                if (cycle1.size() < target1) {
                    ins1 = bestInsertionCost(cycle1, v, distanceMatrix);
                    ins1.belongsToCycle = 1;
                }
                if (cycle2.size() < target2) {
                    ins2 = bestInsertionCost(cycle2, v, distanceMatrix);
                    ins2.belongsToCycle = 2;
                }
                List<Insertion> insList = new ArrayList<>();
                if (ins1 != null) insList.add(ins1);
                if (ins2 != null) insList.add(ins2);
                if (insList.isEmpty()) continue;

                insList.sort(Comparator.comparingInt(i -> i.cost));
                int regret = (insList.size() > 1) ? insList.get(1).cost - insList.get(0).cost : insList.get(0).cost;
                if (regret > bestRegret) {
                    bestRegret = regret;
                    bestVertex = v;
                    bestCycle = insList.getFirst().belongsToCycle;
                    bestIns = insList.getFirst();
                }
            }
            if (bestVertex == -1)
                break;
            if (bestCycle == 1) {
                cycle1.add(bestIns.insertPosition, bestVertex);
            } else {
                cycle2.add(bestIns.insertPosition, bestVertex);
            }
            allVertices.remove(Integer.valueOf(bestVertex));
        }
        return new Solution(cycle1, cycle2);
    }
}
