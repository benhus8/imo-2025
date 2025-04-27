package org.example.algorithms;

import org.example.interfaces.LocalNeighborhoodAware;
import org.example.interfaces.Neighborhood;
import org.example.swaps.BetweenCycleSwapNeighborhood;
import org.example.swaps.TwoEdgeSwapNeighborhood;
import org.example.utils.Move;
import org.example.utils.Solution;

import java.util.*;

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
                for (Move move : neighborhood.deprecatedGenerateMoves(current, matrix)) {
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

    public static Solution steepestWithCandidateEdges(Solution solution, List<Neighborhood> neighborhoods,
                                                      int[][] matrix, Map<Integer, List<Integer>> candidateVertices) {
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
                List<Move> moves = new ArrayList<>();

                if (neighborhood instanceof TwoEdgeSwapNeighborhood twoOpt) {
                    moves = twoOpt.generateCandidateMoves(current, matrix, candidateVertices);
                } else if (neighborhood instanceof BetweenCycleSwapNeighborhood between) {
                    moves = between.generateCandidateMoves(current, matrix, candidateVertices);
                }

                for (Move move : moves) {
                    long delta = neighborhood.computeDelta(current, move, matrix);
                    if (delta < 0) {
                        Solution candidate = new Solution(
                                new ArrayList<>(current.cycle1),
                                new ArrayList<>(current.cycle2)
                        );
                        applyMove(candidate, move);
                        long cost = bestCost + delta;
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
            if (move.j > move.i + 1 && move.i >= 0 && move.j < cycle.size()) {
                List<Integer> sub = new ArrayList<>(cycle.subList(move.i + 1, move.j + 1));
                Collections.reverse(sub);
                for (int k = 0; k < sub.size(); k++) {
                    cycle.set(move.i + 1 + k, sub.get(k));
                }
            }
        }
    }
    private static Set<Integer> getAffectedVertices(Move move, Solution solution) {
        Set<Integer> affected = new HashSet<>();
        int vi = move.i;
        int vj = move.j;
        int vc = move.cycle;

        affected.add(vi);
        affected.add(vj);

        if (vc != -1) {
            List<Integer> cycle = (vc == 0) ? solution.cycle1 : solution.cycle2;
            int size = cycle.size();

            affected.add((vi - 1 + size) % size);
            affected.add((vi + 1) % size);
            affected.add((vj - 1 + size) % size);
            affected.add((vj + 1) % size);
        } else {
            int size1 = solution.cycle1.size();
            int size2 = solution.cycle2.size();

            affected.add((vi - 1 + size1) % size1);
            affected.add((vi + 1) % size1);
            affected.add((vj - 1 + size2) % size2);
            affected.add((vj + 1) % size2);
        }

        return affected;
    }


    public static Solution steepestWithMoveList(Solution solution, List<Neighborhood> neighborhoods, int[][] matrix) {
        int iterations = 0;
        Solution current = new Solution(
                new ArrayList<>(solution.cycle1),
                new ArrayList<>(solution.cycle2)
        );
        long bestCost = current.totalCost(matrix);

        PriorityQueue<Move> moveQueue = new PriorityQueue<>(Comparator.comparingLong(m -> m.delta));
        Set<Move> seenMoves = new HashSet<>();

        for (Neighborhood neighborhood : neighborhoods) {
            for (Move move : neighborhood.generateMoves(current, matrix)) {
                if (!isMoveIndexValid(move, current)) continue;
                long delta = neighborhood.computeDelta(current, move, matrix);
                move.delta = delta;
                if (delta < 0 && seenMoves.add(move)) {
                    moveQueue.add(move);
                }
            }
        }

        while (!moveQueue.isEmpty()) {
            iterations++;

            Move move = moveQueue.poll();
            Move.Applicability state = move.checkApplicability(current);

            if (state == Move.Applicability.INVALID) {
                seenMoves.remove(move);
                continue;
            }

            if (state == Move.Applicability.REVERSED) {
                continue; // zostaje w LM, ale nie robimy nic
            }

            Solution candidate = new Solution(
                    new ArrayList<>(current.cycle1),
                    new ArrayList<>(current.cycle2)
            );
            applyMove(candidate, move);
            long cost = candidate.totalCost(matrix);

            if (cost < bestCost) {
                current = candidate;
                bestCost = cost;
                seenMoves.remove(move);

                Set<Integer> affected = getAffectedVertices(move, current);

                for (Neighborhood neighborhood : neighborhoods) {
                    if (neighborhood instanceof LocalNeighborhoodAware aware) {
                        for (Move newMove : aware.generateMovesForVertices(current, matrix, affected)) {
                            if (!isMoveIndexValid(newMove, current)) continue;
                            if (!seenMoves.contains(newMove)) {
                                long delta = aware.computeDelta(current, newMove, matrix);
                                newMove.delta = delta;
                                if (delta < 0) {
                                    moveQueue.add(newMove);
                                    seenMoves.add(newMove);
                                }
                            }
                        }
                    }
                }

                continue;
            }
        }

        current.iterations = iterations;
        return current;
    }


    private static boolean isMoveIndexValid(Move move, Solution solution) {
        List<Integer> cycle = (move.cycle == 0) ? solution.cycle1 : solution.cycle2;
        int size = cycle.size();

        if (move.type == Move.Type.TWO_EDGE_SWAP) {
            return move.i >= 0 && move.i + 1 < size &&
                    move.j >= 0 && (move.j + 1) % size < size;
        }

        if (move.type == Move.Type.SWAP_BETWEEN_CYCLES) {
            return move.i >= 0 && move.i < solution.cycle1.size() &&
                    move.j >= 0 && move.j < solution.cycle2.size();
        }

        return true;
    }

}
