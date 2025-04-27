package org.example.interfaces;

import org.example.utils.Move;
import org.example.utils.Solution;

import java.util.List;
import java.util.Set;

public interface LocalNeighborhoodAware extends Neighborhood {
    List<Move> generateMovesForVertices(Solution solution, int[][] matrix, Set<Integer> vertices);
}
