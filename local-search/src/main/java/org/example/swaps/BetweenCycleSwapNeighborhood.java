package org.example.swaps;

import org.example.interfaces.Neighborhood;
import org.example.utils.Move;
import org.example.utils.Solution;

import java.util.ArrayList;
import java.util.List;

public class BetweenCycleSwapNeighborhood implements Neighborhood {
    @Override
    public List<Move> generateMoves(Solution solution, int[][] matrix) {
        List<Move> moves = new ArrayList<>();

        for (int i = 0; i < solution.cycle1.size(); i++) {
            for (int j = 0; j < solution.cycle2.size(); j++) {
                long delta = computeDelta(solution, i, j, matrix);
                moves.add(new Move(Move.Type.SWAP_BETWEEN_CYCLES, i, j, -1, delta));
            }
        }

        return moves;
    }

    private long computeDelta(Solution solution, int i, int j, int[][] matrix) {
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

