package org.example.algorithms;

import org.example.interfaces.Neighborhood;
import org.example.utils.Move;
import org.example.utils.Solution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LocalSearch {

    public static Solution steepest(Solution solution, List<Neighborhood> neighborhoods, int[][] matrix) {
        int iterations = 0;

        Solution current = new Solution(
                new ArrayList<>(solution.cycle1),
                new ArrayList<>(solution.cycle2)
        );

        long bestCost = current.totalCost(matrix);

        while (true) {
            iterations++;
            Solution bestCandidate = null;
            long bestCandidateCost = Long.MAX_VALUE;

            for (Neighborhood neighborhood : neighborhoods) {
                for (Move move : neighborhood.generateMoves(current, matrix)) {
                    if (move.delta < 0) {
                        Solution candidate = new Solution(
                                new ArrayList<>(current.cycle1),
                                new ArrayList<>(current.cycle2)
                        );
                        applyMove(candidate, move);
                        long cost = candidate.totalCost(matrix);
                        if (cost < bestCandidateCost) {
                            bestCandidate = candidate;
                            bestCandidateCost = cost;
                        }
                    }
                }
            }

            if (bestCandidate != null && bestCandidateCost < bestCost) {
                current = bestCandidate;
                bestCost = bestCandidateCost;
            } else {
                break;
            }
        }
        current.iterations = iterations;
        return current;
    }

    public static Solution greedy(Solution solution, List<Neighborhood> neighborhoods, int[][] matrix, Random rng) {
        int iterations = 0;
        boolean improved;
        Solution current = new Solution(
                new java.util.ArrayList<>(solution.cycle1),
                new java.util.ArrayList<>(solution.cycle2)
        );

        long bestCost = current.totalCost(matrix);

        do {
            improved = false;
            for (Neighborhood neighborhood : neighborhoods) {
                List<Move> moves = neighborhood.generateMoves(current, matrix);
                Collections.shuffle(moves, rng);
                for (Move move : moves) {
                    if (move.delta < 0) {
                        Solution candidate = new Solution(
                                new java.util.ArrayList<>(current.cycle1),
                                new java.util.ArrayList<>(current.cycle2)
                        );
                        applyMove(candidate, move);
                        long candidateCost = candidate.totalCost(matrix);
                        if (candidateCost < bestCost) {
                            current = candidate;
                            bestCost = candidateCost;
                            improved = true;
                            break;
                        }
                    }
                }
                if (improved) break;
            }
            iterations++;
        } while (improved);

        current.iterations = iterations;
        return current;
    }

    static void applyMove(Solution solution, Move move) {
        if (move.type == Move.Type.SWAP_WITHIN_CYCLE) {
            List<Integer> cycle = (move.cycle == 0) ? solution.cycle1 : solution.cycle2;
            Collections.swap(cycle, move.i, move.j);
        } else if (move.type == Move.Type.SWAP_BETWEEN_CYCLES) {
            Integer temp = solution.cycle1.get(move.i);
            solution.cycle1.set(move.i, solution.cycle2.get(move.j));
            solution.cycle2.set(move.j, temp);
        } else if (move.type == Move.Type.TWO_EDGE_SWAP) {
            List<Integer> cycle = (move.cycle == 0) ? solution.cycle1 : solution.cycle2;
            if (move.j > move.i + 1) {
                List<Integer> sub = cycle.subList(move.i + 1, move.j + 1);
                Collections.reverse(sub);
            }
        }
    }
}
