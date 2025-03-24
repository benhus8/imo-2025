package org.example.utils;

import java.util.List;

public class Solution {
    List<Integer> cycle1;
    List<Integer> cycle2;

    public Solution(List<Integer> cycle1, List<Integer> cycle2) {
        this.cycle1 = cycle1;
        this.cycle2 = cycle2;
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