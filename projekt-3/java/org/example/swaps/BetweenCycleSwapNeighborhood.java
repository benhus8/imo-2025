package org.example.swaps;

import org.example.interfaces.LocalNeighborhoodAware;
import org.example.interfaces.Neighborhood;
import org.example.utils.Move;
import org.example.utils.Solution;

import java.util.*;

public class BetweenCycleSwapNeighborhood implements LocalNeighborhoodAware {

    @Override
    public List<Move> generateMoves(Solution solution, int[][] matrix) {
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < solution.cycle1.size(); i++) {
            for (int j = 0; j < solution.cycle2.size(); j++) {
                moves.add(new Move(Move.Type.SWAP_BETWEEN_CYCLES, i, j, -1));
            }
        }
        return moves;
    }

    public List<Move> deprecatedGenerateMoves(Solution solution, int[][] matrix) {
        List<Move> moves = new ArrayList<>();

        for (int i = 0; i < solution.cycle1.size(); i++) {
            for (int j = 0; j < solution.cycle2.size(); j++) {
                long delta = deprecatedComputeDelta(solution, i, j, matrix);
                moves.add(new Move(Move.Type.SWAP_BETWEEN_CYCLES, i, j, -1, delta));
            }
        }

        return moves;
    }

    public List<Move> generateCandidateMoves(Solution solution, int[][] matrix, Map<Integer, List<Integer>> candidateEdges) {
        List<Move> moves = new ArrayList<>();

        Map<Integer, Integer> indexMap2 = new HashMap<>();
        for (int j = 0; j < solution.cycle2.size(); j++) {
            indexMap2.put(solution.cycle2.get(j), j);
        }

        for (int i = 0; i < solution.cycle1.size(); i++) {
            int u = solution.cycle1.get(i);

            for (int v : candidateEdges.getOrDefault(u, List.of())) {
                Integer j = indexMap2.get(v);
                if (j != null) {
                    moves.add(new Move(Move.Type.SWAP_BETWEEN_CYCLES, i, j, -1));
                }
            }
        }

        return moves;
    }


    public long computeDelta(Solution solution, Move move, int[][] matrix) {
        List<Integer> c1 = solution.cycle1;
        List<Integer> c2 = solution.cycle2;

        int i = move.i;
        int j = move.j;

        int a = c1.get(i);
        int b = c2.get(j);

        int aPrev = c1.get((i - 1 + c1.size()) % c1.size());
        int aNext = c1.get((i + 1) % c1.size());

        int bPrev = c2.get((j - 1 + c2.size()) % c2.size());
        int bNext = c2.get((j + 1) % c2.size());

        long before = matrix[aPrev][a] + matrix[a][aNext] + matrix[bPrev][b] + matrix[b][bNext];
        long after = matrix[aPrev][b] + matrix[b][aNext] + matrix[bPrev][a] + matrix[a][bNext];

        return after - before;
    }

    private long deprecatedComputeDelta(Solution solution, int i, int j, int[][] matrix) {
        List<Integer> c1 = solution.cycle1;
        List<Integer> c2 = solution.cycle2;

        int a = c1.get(i);
        int b = c2.get(j);

        int aPrev = c1.get((i - 1 + c1.size()) % c1.size());
        int aNext = c1.get((i + 1) % c1.size());

        int bPrev = c2.get((j - 1 + c2.size()) % c2.size());
        int bNext = c2.get((j + 1) % c2.size());

        long before = matrix[aPrev][a] + matrix[a][aNext] + matrix[bPrev][b] + matrix[b][bNext];
        long after = matrix[aPrev][b] + matrix[b][aNext] + matrix[bPrev][a] + matrix[a][bNext];

        return after - before;
    }

    @Override
    public List<Move> generateMovesForVertices(Solution solution, int[][] matrix, Set<Integer> vertices) {
        List<Move> moves = new ArrayList<>();

        List<Integer> c1 = solution.cycle1;
        List<Integer> c2 = solution.cycle2;

        int size1 = c1.size();
        int size2 = c2.size();

        Map<Integer, Integer> indexMap1 = new HashMap<>();
        Map<Integer, Integer> indexMap2 = new HashMap<>();

        for (int i = 0; i < size1; i++) indexMap1.put(c1.get(i), i);
        for (int j = 0; j < size2; j++) indexMap2.put(c2.get(j), j);

        for (Integer v : vertices) {
            if (indexMap1.containsKey(v)) {
                int i = indexMap1.get(v);

                for (int j = 0; j < size2; j++) {

                    int prevIdx = (j - 1 + size2) % size2;
                    int nextIdx = (j + 1) % size2;

                    long deltaPrev = computeDeltaSwapBetweenCycles(solution, i, prevIdx, matrix);
                    long deltaNext = computeDeltaSwapBetweenCycles(solution, i, nextIdx, matrix);

                    if (deltaPrev <= deltaNext) {
                        moves.add(new Move(Move.Type.SWAP_BETWEEN_CYCLES, i, prevIdx, -1));
                    } else {
                        moves.add(new Move(Move.Type.SWAP_BETWEEN_CYCLES, i, nextIdx, -1));
                    }
                }
            } else if (indexMap2.containsKey(v)) {
                int j = indexMap2.get(v);

                for (int i = 0; i < size1; i++) {

                    int prevIdx = (i - 1 + size1) % size1;
                    int nextIdx = (i + 1) % size1;

                    long deltaPrev = computeDeltaSwapBetweenCycles(solution, prevIdx, j, matrix);
                    long deltaNext = computeDeltaSwapBetweenCycles(solution, nextIdx, j, matrix);

                    if (deltaPrev <= deltaNext) {
                        moves.add(new Move(Move.Type.SWAP_BETWEEN_CYCLES, prevIdx, j, -1));
                    } else {
                        moves.add(new Move(Move.Type.SWAP_BETWEEN_CYCLES, nextIdx, j, -1));
                    }
                }
            }
        }

        return moves;
    }

    private long computeDeltaSwapBetweenCycles(Solution solution, int i, int j, int[][] matrix) {
        List<Integer> c1 = solution.cycle1;
        List<Integer> c2 = solution.cycle2;

        int a = c1.get(i);
        int b = c2.get(j);

        int aPrev = c1.get((i - 1 + c1.size()) % c1.size());
        int aNext = c1.get((i + 1) % c1.size());

        int bPrev = c2.get((j - 1 + c2.size()) % c2.size());
        int bNext = c2.get((j + 1) % c2.size());

        long before = matrix[aPrev][a] + matrix[a][aNext] + matrix[bPrev][b] + matrix[b][bNext];
        long after = matrix[aPrev][b] + matrix[b][aNext] + matrix[bPrev][a] + matrix[a][bNext];

        return after - before;
    }

}

