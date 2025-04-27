package org.example.algorithms;


import org.example.interfaces.Heuristic;
import org.example.utils.Instance;
import org.example.utils.Solution;
import org.example.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomHeuristic implements Heuristic {
    public Solution solve(Instance instance, int[][] distanceMatrix, Random random) {
        List<Integer> all = Utils.getAllVertices(instance);
        Collections.shuffle(all, random);

        int half = instance.dimension / 2;
        List<Integer> cycle1 = new ArrayList<>(all.subList(0, half + instance.dimension % 2));
        List<Integer> cycle2 = new ArrayList<>(all.subList(half + instance.dimension % 2, all.size()));

        return new Solution(cycle1, cycle2);
    }
}
