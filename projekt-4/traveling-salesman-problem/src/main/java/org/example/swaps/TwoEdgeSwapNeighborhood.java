package org.example.swaps;

import org.example.interfaces.LocalNeighborhoodAware;
import org.example.interfaces.Neighborhood;
import org.example.utils.Move;
import org.example.utils.Solution;

import java.util.*;

public class TwoEdgeSwapNeighborhood  implements LocalNeighborhoodAware {

    @Override
    public List<Move> generateMoves(Solution solution, int[][] matrix) {
        List<Move> moves = new ArrayList<>();
        for (int cycleIdx = 0; cycleIdx <= 1; cycleIdx++) {
            List<Integer> cycle = (cycleIdx == 0) ? solution.cycle1 : solution.cycle2;
            int size = cycle.size();

            for (int i = 0; i < size - 2; i++) {
                for (int j = i + 2; j < size; j++) {
                    if (i == 0 && j == size - 1) continue;
                    moves.add(new Move(Move.Type.TWO_EDGE_SWAP, i, j, cycleIdx));
                }
            }
        }
        return moves;
    }

    public List<Move> generateCandidateMoves(Solution solution, int[][] matrix, Map<Integer, List<Integer>> candidateEdges) {
        List<Move> moves = new ArrayList<>();

        for (int cycleIdx = 0; cycleIdx <= 1; cycleIdx++) {
            List<Integer> cycle = (cycleIdx == 0) ? solution.cycle1 : solution.cycle2;
            int size = cycle.size();

            Map<Integer, Integer> indexMap = new HashMap<>();
            for (int idx = 0; idx < size; idx++) {
                indexMap.put(cycle.get(idx), idx);
            }

            for (int i = 0; i < size; i++) {
                int u = cycle.get(i);
                for (int v : candidateEdges.getOrDefault(u, List.of())) {
                    Integer j = indexMap.get(v);
                    if (j == null) continue;

                    if (Math.abs(i - j) > 1 && !(i == 0 && j == size - 1) && !(j == 0 && i == size - 1)) {
                        int from = Math.min(i, j);
                        int to = Math.max(i, j);
                        moves.add(new Move(Move.Type.TWO_EDGE_SWAP, from, to, cycleIdx));
                    }
                }
            }
        }

        return moves;
    }

    public  long computeDelta(Solution solution, Move move, int[][] matrix) {
        List<Integer> cycle = (move.cycle == 0) ? solution.cycle1 : solution.cycle2;
        int i = move.i;
        int j = move.j;

        int a = cycle.get(i);
        int b = cycle.get(i + 1);
        int c = cycle.get(j);
        int d = cycle.get((j + 1) % cycle.size());

        long before = matrix[a][b] + matrix[c][d];
        long after = matrix[a][c] + matrix[b][d];

        return after - before;
    }

    @Override
    public List<Move> deprecatedGenerateMoves(Solution solution, int[][] matrix) {
        List<Move> moves = new ArrayList<>();

        for (int cycleIdx = 0; cycleIdx <= 1; cycleIdx++) {
            List<Integer> cycle = (cycleIdx == 0) ? solution.cycle1 : solution.cycle2;
            int size = cycle.size();

            for (int i = 0; i < size - 1; i++) {
                for (int j = i + 2; j < size; j++) {
                    long delta = deprecatedComputeDelta(cycle, i, j, matrix);
                    moves.add(new Move(Move.Type.TWO_EDGE_SWAP, i, j, cycleIdx, delta));
                }
            }
        }

        return moves;
    }

    private long deprecatedComputeDelta(List<Integer> cycle, int i, int j, int[][] matrix) {
        int a = cycle.get(i);
        int b = cycle.get(i + 1);
        int c = cycle.get(j);
        int d = cycle.get((j + 1) % cycle.size());

        long before = matrix[a][b] + matrix[c][d];
        long after = matrix[a][c] + matrix[b][d];

        return after - before;
    }

    @Override
    public List<Move> generateMovesForVertices(Solution solution, int[][] matrix, Set<Integer> vertices) {
        List<Move> moves = new ArrayList<>();
        for (int cycleIdx = 0; cycleIdx <= 1; cycleIdx++) {
            List<Integer> cycle = (cycleIdx == 0) ? solution.cycle1 : solution.cycle2;
            int size = cycle.size();

            for (int i = 0; i < size - 2; i++) {
                if (!vertices.contains(cycle.get(i))) continue;
                for (int j = i + 2; j < size; j++) {
                    if (i == 0 && j == size - 1) continue;
                    moves.add(new Move(Move.Type.TWO_EDGE_SWAP, i, j, cycleIdx));
                }
            }
        }
        return moves;
    }
}


