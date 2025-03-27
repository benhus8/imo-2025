package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

interface LocalSearchAlgorithm {
    Solution run(Solution initial, int[][] graph);
}

interface Move {
    int delta(Solution s, int[][] graph);

    void apply(Solution s);
}

public class Proj2 {


    public static void main(String[] args) {
        int[][] graph = generateRandomGraph(10); // 10-node graph
        Solution initial = Solution.random(graph);

        LocalSearchAlgorithm greedy = new GreedySearch();
        Solution result = greedy.run(initial, graph);

        System.out.println("Final cost: " + result.cost(graph));
        System.out.println("Cycle A: " + result.cycleA);
        System.out.println("Cycle B: " + result.cycleB);
    }

    static int[][] generateRandomGraph(int n) {
        Random rand = new Random();
        int[][] g = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                g[i][j] = g[j][i] = rand.nextInt(100) + 1;
            }
        }
        return g;
    }
}

class Solution {
    List<Integer> cycleA, cycleB;

    public Solution(List<Integer> a, List<Integer> b) {
        this.cycleA = new ArrayList<>(a);
        this.cycleB = new ArrayList<>(b);
    }

    public static Solution random(int[][] graph) {
        List<Integer> all = new ArrayList<>();
        for (int i = 0; i < graph.length; i++) all.add(i);
        Collections.shuffle(all);
        return new Solution(all.subList(0, all.size() / 2), all.subList(all.size() / 2, all.size()));
    }

    public int cost(int[][] graph) {
        return tourCost(cycleA, graph) + tourCost(cycleB, graph);
    }

    private int tourCost(List<Integer> cycle, int[][] graph) {
        int cost = 0;
        for (int i = 0; i < cycle.size(); i++) {
            int from = cycle.get(i);
            int to = cycle.get((i + 1) % cycle.size());
            cost += graph[from][to];
        }
        return cost;
    }

    public Solution deepCopy() {
        return new Solution(new ArrayList<>(cycleA), new ArrayList<>(cycleB));
    }
}

class GreedySearch implements LocalSearchAlgorithm {
    public Solution run(Solution current, int[][] graph) {
        boolean improvement;
        do {
            improvement = false;
            List<Move> neighbors = Neighborhood.getAllMoves(current);
            Collections.shuffle(neighbors);

            for (Move m : neighbors) {
                int delta = m.delta(current, graph);
                if (delta < 0) {
                    m.apply(current);
                    improvement = true;
                    break;
                }
            }
        } while (improvement);

        return current;
    }
}

class Neighborhood {
    public static List<Move> getAllMoves(Solution s) {
        List<Move> moves = new ArrayList<>();
        moves.addAll(generateSwapMoves(s.cycleA, true));
        moves.addAll(generateSwapMoves(s.cycleB, false));
        moves.addAll(generateCrossMoves(s));
        return moves;
    }

    private static List<Move> generateSwapMoves(List<Integer> cycle, boolean isA) {
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < cycle.size(); i++) {
            for (int j = i + 1; j < cycle.size(); j++) {
                moves.add(new SwapMove(i, j, isA));
            }
        }
        return moves;
    }

    private static List<Move> generateCrossMoves(Solution s) {
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < s.cycleA.size(); i++) {
            for (int j = 0; j < s.cycleB.size(); j++) {
                moves.add(new CrossMove(i, j));
            }
        }
        return moves;
    }
}

class SwapMove implements Move {
    int i, j;
    boolean isA;

    public SwapMove(int i, int j, boolean isA) {
        this.i = i;
        this.j = j;
        this.isA = isA;
    }

    public int delta(Solution s, int[][] graph) {
        List<Integer> cycle = isA ? s.cycleA : s.cycleB;
        List<Integer> temp = new ArrayList<>(cycle);
        Collections.swap(temp, i, j);
        int before = s.cost(graph);
        Solution tempSol = s.deepCopy();
        if (isA) tempSol.cycleA = temp;
        else tempSol.cycleB = temp;
        int after = tempSol.cost(graph);
        return after - before;
    }

    public void apply(Solution s) {
        List<Integer> cycle = isA ? s.cycleA : s.cycleB;
        Collections.swap(cycle, i, j);
    }
}

class CrossMove implements Move {
    int i, j;

    public CrossMove(int i, int j) {
        this.i = i;
        this.j = j;
    }

    public int delta(Solution s, int[][] graph) {
        List<Integer> a = new ArrayList<>(s.cycleA);
        List<Integer> b = new ArrayList<>(s.cycleB);
        Collections.swap(a, i, i); // just to copy
        Collections.swap(b, j, j);
        int before = s.cost(graph);
        int temp = a.get(i);
        a.set(i, b.get(j));
        b.set(j, temp);
        Solution tempSol = new Solution(a, b);
        int after = tempSol.cost(graph);
        return after - before;
    }

    public void apply(Solution s) {
        int temp = s.cycleA.get(i);
        s.cycleA.set(i, s.cycleB.get(j));
        s.cycleB.set(j, temp);
    }
}

