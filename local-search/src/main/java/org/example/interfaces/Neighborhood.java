package org.example.interfaces;

import org.example.utils.Move;
import org.example.utils.Solution;

import java.util.List;

public interface Neighborhood {
    List<Move> generateMoves(Solution solution, int[][] matrix);
}

