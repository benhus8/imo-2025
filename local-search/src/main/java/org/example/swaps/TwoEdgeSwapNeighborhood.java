package org.example.swaps;

import org.example.interfaces.Neighborhood;
import org.example.utils.Move;
import org.example.utils.Solution;

import java.util.ArrayList;
import java.util.List;

public class TwoEdgeSwapNeighborhood implements Neighborhood {
    @Override
    public List<Move> generateMoves(Solution solution, int[][] matrix) {
        List<Move> moves = new ArrayList<>();

        for (int cycleIdx = 0; cycleIdx <= 1; cycleIdx++) {
            List<Integer> cycle = (cycleIdx == 0) ? solution.cycle1 : solution.cycle2;
            int size = cycle.size();

            for (int i = 0; i < size - 1; i++) {
                for (int j = i + 2; j < size; j++) {
                    long delta = computeDelta(cycle, i, j, matrix);
                    moves.add(new Move(Move.Type.TWO_EDGE_SWAP, i, j, cycleIdx, delta));
                }
            }
        }

        return moves;
    }

    private long computeDelta(List<Integer> cycle, int i, int j, int[][] matrix) {
        int a = cycle.get(i);
        int b = cycle.get(i + 1);
        int c = cycle.get(j);
        int d = cycle.get((j + 1) % cycle.size());

        long before = matrix[a][b] + matrix[c][d];
        long after = matrix[a][c] + matrix[b][d];

        return after - before;
    }
}

