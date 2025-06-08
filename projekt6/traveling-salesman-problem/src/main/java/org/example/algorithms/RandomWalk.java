package org.example.algorithms;

import org.example.interfaces.Neighborhood;
import org.example.utils.Move;
import org.example.utils.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.example.algorithms.LocalSearch.applyMove;

public class RandomWalk {
    public static Solution randomWalk(Solution solution, List<Neighborhood> neighborhoods, int[][] matrix, Random rng, int maxIterations) {
        Solution current = new Solution(
                new ArrayList<>(solution.cycle1),
                new ArrayList<>(solution.cycle2)
        );

        for (int iter = 0; iter < maxIterations; iter++) {
            Neighborhood neighborhood = neighborhoods.get(rng.nextInt(neighborhoods.size()));
            List<Move> moves = neighborhood.generateMoves(current, matrix);
            if (!moves.isEmpty()) {
                Move move = moves.get(rng.nextInt(moves.size()));
                applyMove(current, move);
            }
        }

        return current;
    }
}
