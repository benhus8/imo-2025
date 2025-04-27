package org.example.interfaces;

import org.example.utils.Move;
import org.example.utils.Solution;

import java.util.List;

public interface Neighborhood {
    List<Move> generateMoves(Solution solution, int[][] matrix);
    List<Move> deprecatedGenerateMoves(Solution solution, int[][] matrix);

     long computeDelta(Solution solution, Move move, int[][] matrix);
}

