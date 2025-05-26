package org.example.algorithms;

import org.example.interfaces.LocalNeighborhoodAware;
import org.example.interfaces.Neighborhood;
import org.example.swaps.BetweenCycleSwapNeighborhood;
import org.example.swaps.TwoEdgeSwapNeighborhood;
import org.example.utils.Instance;
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
                continue;
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


    public static Solution multipleStartLocalSearch(Instance instance, List<Neighborhood> neighborhoods, int[][] matrix, int numStarts, Random rng) {
        Solution bestOverall = null;
        long bestCost = Long.MAX_VALUE;
        for (int start = 0; start < numStarts; start++) {
            Solution randomSolution = new RandomHeuristic().solve(instance, matrix, new Random());
            Solution improved = steepest(randomSolution, neighborhoods, matrix);
            long cost = improved.totalCost(matrix);
            if (cost < bestCost) {
                bestCost = cost;
                bestOverall = improved;
            }

        }
        return bestOverall;
    }


    public static Solution iteratedLocalSearch(Solution startSolution, List<Neighborhood> neighborhoods, int[][] matrix, long timeLimitMs, Random rng) {
        long startTime = System.currentTimeMillis();

        Solution current = steepest(new Solution(startSolution), neighborhoods, matrix);
        long bestCost = current.totalCost(matrix);
        int perturbationNumber = 0;
        while (System.currentTimeMillis() - startTime < timeLimitMs *10) {
            Solution perturbed = smallPerturbation(new Solution(current), rng, 10);
            perturbed = steepest(perturbed, neighborhoods, matrix);
            long newCost = perturbed.totalCost(matrix);

            if (newCost < bestCost) {
                current = perturbed;
                bestCost = newCost;
                System.out.println("[ILS] New best cost: " + bestCost);
            }
            perturbationNumber++;
        }
        return current;
    }


    public static Solution largeNeighborhoodSearch(Solution startSolution, List<Neighborhood> neighborhoods, int[][] matrix, long timeLimitMs, Random rng) {
        long startTime = System.currentTimeMillis();

        Solution current = startSolution;
        long bestCost = current.totalCost(matrix);
        int perturbationNumber = 0;
        while (System.currentTimeMillis() - startTime < timeLimitMs * 200) {
            Solution destroyed = destroy(new Solution(current), rng);
            Solution improved = new WeightedRegretHeuristicAlgorithm(1, -1).solvePartialSolution(destroyed, matrix, rng);
            long newCost = improved.totalCost(matrix);

            if (newCost < bestCost) {
                current = improved;
                bestCost = newCost;
                System.out.println("[LNS] New best cost: " + bestCost);
            }
            perturbationNumber++;

        }
        System.out.println("Perturbation number for LNS: " + perturbationNumber);
        return current;
    }


    public static Solution smallPerturbation(Solution solution, Random rng, int numSwaps) {
        int size1 = solution.cycle1.size();
        int size2 = solution.cycle2.size();

        if (size1 == 0 || size2 == 0) return solution;

        for (int swap = 0; swap < numSwaps; swap++) {
            int idx1 = rng.nextInt(size1);
            int idx2 = rng.nextInt(size2);

            int temp = solution.cycle1.get(idx1);
            solution.cycle1.set(idx1, solution.cycle2.get(idx2));
            solution.cycle2.set(idx2, temp);
        }

        return solution;
    }

    public static Solution destroy(Solution solution, Random rng) {
        int numRemove = 30;

        if (!solution.cycle1.isEmpty()) {
            int size1 = solution.cycle1.size();
            int removeFrom1 = Math.min(numRemove, size1);

            int startIdx1 = rng.nextInt(size1);
            Set<Integer> toRemove1 = new HashSet<>();
            for (int i = 0; i < removeFrom1; i++) {
                int idx = (startIdx1 + i) % size1;
                toRemove1.add(solution.cycle1.get(idx));
            }
            solution.cycle1.removeIf(toRemove1::contains);
        }

        if (!solution.cycle2.isEmpty()) {
            int size2 = solution.cycle2.size();
            int removeFrom2 = Math.min(numRemove, size2);

            int startIdx2 = rng.nextInt(size2);
            Set<Integer> toRemove2 = new HashSet<>();
            for (int i = 0; i < removeFrom2; i++) {
                int idx = (startIdx2 + i) % size2;
                toRemove2.add(solution.cycle2.get(idx));
            }
            solution.cycle2.removeIf(toRemove2::contains);
        }

        return solution;
    }

    public static Solution mutate(Solution sol, Random rng) {
        Solution mutated = new Solution(
                new ArrayList<>(sol.cycle1),
                new ArrayList<>(sol.cycle2)
        );

        int type = rng.nextInt(3);

        switch (type) {
            case 0 -> swapWithinCycle(mutated, rng);
            case 1 -> swapBetweenCycles(mutated, rng);
            case 2 -> reverseSubsequence(mutated, rng);
        }

        return mutated;
    }

    private static void swapWithinCycle(Solution sol, Random rng) {
        List<Integer> cycle = rng.nextBoolean() ? sol.cycle1 : sol.cycle2;
        if (cycle.size() < 2) return;

        int i = rng.nextInt(cycle.size());
        int j = rng.nextInt(cycle.size());
        Collections.swap(cycle, i, j);
    }

    private static void swapBetweenCycles(Solution sol, Random rng) {
        if (sol.cycle1.isEmpty() || sol.cycle2.isEmpty()) return;

        int i = rng.nextInt(sol.cycle1.size());
        int j = rng.nextInt(sol.cycle2.size());

        int temp = sol.cycle1.get(i);
        sol.cycle1.set(i, sol.cycle2.get(j));
        sol.cycle2.set(j, temp);
    }

    private static void reverseSubsequence(Solution sol, Random rng) {
        List<Integer> cycle = rng.nextBoolean() ? sol.cycle1 : sol.cycle2;
        if (cycle.size() < 4) return;

        int i = rng.nextInt(cycle.size() - 2);
        int j = i + 2 + rng.nextInt(cycle.size() - i - 2);

        List<Integer> sub = new ArrayList<>(cycle.subList(i, j));
        Collections.reverse(sub);
        for (int k = 0; k < sub.size(); k++) {
            cycle.set(i + k, sub.get(k));
        }
    }

}
