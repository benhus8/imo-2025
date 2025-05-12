package org.example.utils;

import java.util.ArrayList;
import java.util.List;

public class Solution {
    public List<Integer> cycle1;
    public List<Integer> cycle2;
    public int iterations = 0;

    public Solution(List<Integer> cycle1, List<Integer> cycle2) {
        this.cycle1 = cycle1;
        this.cycle2 = cycle2;
    }

    public Solution(Solution solution) {
        this.cycle1 = new ArrayList<>(solution.cycle1);
        this.cycle2 = new ArrayList<>(solution.cycle2);
        this.iterations = solution.iterations;
    }

    public long totalCost(int[][] matrix) {
        long cost = 0;
        cost += cycleCost(cycle1, matrix);
        cost += cycleCost(cycle2, matrix);
        return cost;
    }

    private long cycleCost(List<Integer> cycle, int[][] matrix) {
        long cost = 0;
        int size = cycle.size();
        if(size > 0) {
            for(int i = 0; i < size; i++){
                int from = cycle.get(i);
                int to = cycle.get((i + 1) % size);
                cost += matrix[from][to];
            }
        }
        return cost;
    }
}