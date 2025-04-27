package org.example.algorithms;

import org.example.interfaces.Heuristic;
import org.example.utils.Insertion;
import org.example.utils.Instance;
import org.example.utils.Solution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.example.utils.Utils.getAllVertices;

public class WeightedRegretHeuristicAlgorithm implements Heuristic {
    private final double w1;
    private final double w2;

    public WeightedRegretHeuristicAlgorithm(double w1, double w2) {
        this.w1 = w1;
        this.w2 = w2;
    }

    @Override
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

        while (!allVertices.isEmpty()) {
            double bestWeightedRegret = -1;
            int bestVertex = -1;
            int bestCycle = -1;
            Insertion bestIns = null;

            for (int v : allVertices) {
                Insertion ins1 = null, ins2 = null;
                boolean canCycle1 = cycle1.size() < target1;
                boolean canCycle2 = cycle2.size() < target2;

                if (!canCycle1 && canCycle2) {
                    ins2 = bestInsertionCost(cycle2, v, distanceMatrix);
                    ins2.belongsToCycle = 2;
                    if (ins2.cost > bestWeightedRegret) {
                        bestWeightedRegret = ins2.cost;
                        bestVertex = v;
                        bestCycle = 2;
                        bestIns = ins2;
                    }
                } else if (canCycle1 && !canCycle2) {
                    ins1 = bestInsertionCost(cycle1, v, distanceMatrix);
                    ins1.belongsToCycle = 1;
                    if (ins1.cost > bestWeightedRegret) {
                        bestWeightedRegret = ins1.cost;
                        bestVertex = v;
                        bestCycle = 1;
                        bestIns = ins1;
                    }
                } else if (canCycle1 && canCycle2) {
                    ins1 = bestInsertionCost(cycle1, v, distanceMatrix);
                    ins1.belongsToCycle = 1;
                    ins2 = bestInsertionCost(cycle2, v, distanceMatrix);
                    ins2.belongsToCycle = 2;

                    List<Insertion> insList = new ArrayList<>(List.of(ins1, ins2));
                    insList.sort(Comparator.comparingInt(i -> i.cost));

                    int cost1 = insList.get(0).cost;
                    int cost2 = insList.get(1).cost;
                    double weightedRegret = w1 * (cost2 - cost1) + w2 * cost1;

                    if (weightedRegret > bestWeightedRegret) {
                        bestWeightedRegret = weightedRegret;
                        bestVertex = v;
                        bestCycle = insList.get(0).belongsToCycle;
                        bestIns = insList.get(0);
                    }
                }
            }

            if (bestVertex == -1) break;

            if (bestCycle == 1) {
                cycle1.add(bestIns.insertPosition, bestVertex);
            } else {
                cycle2.add(bestIns.insertPosition, bestVertex);
            }

            allVertices.remove(Integer.valueOf(bestVertex));
        }
        return new Solution(cycle1, cycle2);
    }

    public int findNearest(int vertex, List<Integer> candidates, int[][] distanceMatrix) {
        int nearest = -1;
        int minDistance = Integer.MAX_VALUE;
        for (int v : candidates) {
            if (distanceMatrix[vertex][v] < minDistance) {
                minDistance = distanceMatrix[vertex][v];
                nearest = v;
            }
        }
        return nearest;
    }

    public Insertion bestInsertionCost(List<Integer> cycle, int vertex, int[][] distanceMatrix) {
        int bestCost = Integer.MAX_VALUE;
        int bestPosition = -1;

        for (int i = 0; i < cycle.size(); i++) {
            int next = (i + 1) % cycle.size();
            int a = cycle.get(i);
            int b = cycle.get(next);
            int cost = distanceMatrix[a][vertex] + distanceMatrix[vertex][b] - distanceMatrix[a][b];
            if (cost < bestCost) {
                bestCost = cost;
                bestPosition = next;
            }
        }
        return new Insertion(bestPosition, bestCost);
    }
}

