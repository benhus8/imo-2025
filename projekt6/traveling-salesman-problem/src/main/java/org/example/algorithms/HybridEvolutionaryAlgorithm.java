package org.example.algorithms;

import org.example.interfaces.Neighborhood;
import org.example.swaps.BetweenCycleSwapNeighborhood;
import org.example.swaps.TwoEdgeSwapNeighborhood;
import org.example.utils.Solution;
import org.example.utils.Instance;

import java.util.*;

public class HybridEvolutionaryAlgorithm {

    private final int populationSize = 20;
    private final long maxTimeMillis;
    private final Random rng;
    private final int[][] matrix;

    private final List<Solution> population = new ArrayList<>();
    List<Neighborhood> neighborhoods = List.of(new TwoEdgeSwapNeighborhood(), new BetweenCycleSwapNeighborhood());

    public HybridEvolutionaryAlgorithm(long maxTimeMillis, int[][] matrix, Random rng) {
        this.maxTimeMillis = maxTimeMillis;
        this.matrix = matrix;
        this.rng = rng;
    }

    public Solution run(Instance instance) {
        initializePopulation(instance);

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < maxTimeMillis) {
            Solution parent1 = selectRandom();
            Solution parent2 = selectRandom();

            while (parent1.equals(parent2)) {
                parent2 = selectRandom();
            }

            Solution offspring = recombine(parent1, parent2);

            offspring = LocalSearch.mutate(offspring, rng);

            offspring = LocalSearch.steepest(offspring, neighborhoods, matrix);

            if (isValidOffspring(offspring)) {
                addToPopulation(offspring);
            }
        }

        System.out.println(getBestSolution().cycle1);
        System.out.println(getBestSolution().cycle2);
        return getBestSolution();
    }

    private void initializePopulation(Instance instance) {
        for (int i = 0; i < populationSize; i++) {
            Solution sol = new WeightedRegretHeuristicAlgorithm(1, -1).solve(instance, matrix, rng);
            sol = LocalSearch.steepest(sol, neighborhoods, matrix);
            addToPopulation(sol);
        }
    }

    private Solution selectRandom() {
        return population.get(rng.nextInt(population.size()));
    }

    private boolean isValidOffspring(Solution candidate) {
        return population.stream().noneMatch(sol -> Math.abs(sol.totalCost(matrix) - candidate.totalCost(matrix)) < 1e-6);
    }

    private void addToPopulation(Solution solution) {
        population.add(solution);
        population.sort(Comparator.comparingLong(sol -> sol.totalCost(matrix)));
        if (population.size() > populationSize) {
            population.remove(population.size() - 1);
        }
    }

    private Solution getBestSolution() {
        return population.get(0);
    }

    private Solution recombine(Solution p1, Solution p2) {
        Set<String> edgesP2 = extractEdges(p2);
        Solution child = new Solution(
                new ArrayList<>(p1.cycle1),
                new ArrayList<>(p1.cycle2)
        );

        child.cycle1.removeIf(v -> !hasEdge(v, child.cycle1, edgesP2));
        child.cycle2.removeIf(v -> !hasEdge(v, child.cycle2, edgesP2));

        Solution repaired = new WeightedRegretHeuristicAlgorithm(1, -1).solvePartialSolution(child, matrix, rng);
        return repaired;
    }

    private Set<String> extractEdges(Solution sol) {
        Set<String> edges = new HashSet<>();
        addCycleEdges(edges, sol.cycle1);
        addCycleEdges(edges, sol.cycle2);
        return edges;
    }

    private void addCycleEdges(Set<String> edges, List<Integer> cycle) {
        int size = cycle.size();
        for (int i = 0; i < size; i++) {
            int from = cycle.get(i);
            int to = cycle.get((i + 1) % size);
            edges.add(edgeKey(from, to));
        }
    }

    private boolean hasEdge(int vertex, List<Integer> cycle, Set<String> validEdges) {
        int i = cycle.indexOf(vertex);
        if (i == -1) return false;
        int prev = cycle.get((i - 1 + cycle.size()) % cycle.size());
        int next = cycle.get((i + 1) % cycle.size());
        return validEdges.contains(edgeKey(vertex, prev)) || validEdges.contains(edgeKey(vertex, next));
    }

    private String edgeKey(int a, int b) {
        return Math.min(a, b) + "-" + Math.max(a, b);
    }
}