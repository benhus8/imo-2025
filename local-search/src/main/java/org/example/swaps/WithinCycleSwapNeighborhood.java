package org.example.swaps;

import org.example.interfaces.Neighborhood;
import org.example.utils.Move;
import org.example.utils.Solution;

import java.util.ArrayList;
import java.util.List;

public class WithinCycleSwapNeighborhood implements Neighborhood {
    @Override
    public List<Move> generateMoves(Solution solution, int[][] matrix) {
        List<Move> moves = new ArrayList<>();
        for (int cycleIdx = 0; cycleIdx <= 1; cycleIdx++) {
            List<Integer> cycle = (cycleIdx == 0) ? solution.cycle1 : solution.cycle2;
            int size = cycle.size();

            for (int i = 0; i < size; i++) {
                for (int j = i + 1; j < size; j++) {
                    long delta = computeDelta(cycle, i, j, matrix);
                    moves.add(new Move(Move.Type.SWAP_WITHIN_CYCLE, i, j, cycleIdx, delta));
                }
            }
        }
        return moves;
    }

    private long computeDelta(List<Integer> cycle, int i, int j, int[][] matrix) {
        int n = cycle.size();
        if (i == j) return 0;
        int a = cycle.get(i);
        int b = cycle.get(j);
        int aPrev = cycle.get((i - 1 + n) % n);
        int aNext = cycle.get((i + 1) % n);
        int bPrev = cycle.get((j - 1 + n) % n);
        int bNext = cycle.get((j + 1) % n);

        long before = matrix[aPrev][a] + matrix[a][aNext] + matrix[bPrev][b] + matrix[b][bNext];
        long after = matrix[aPrev][b] + matrix[b][aNext] + matrix[bPrev][a] + matrix[a][bNext];

        if (Math.abs(i - j) == 1 || Math.abs(i - j) == n - 1) {
            before = matrix[aPrev][a] + matrix[a][b] + matrix[b][bNext];
            after = matrix[aPrev][b] + matrix[b][a] + matrix[a][bNext];
        }
        return after - before;
    }
}
