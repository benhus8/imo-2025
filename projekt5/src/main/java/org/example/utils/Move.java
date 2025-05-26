package org.example.utils;

import java.util.List;

public class Move {
    public final Type type;
    public final int i;
    public final int j;
    public final int cycle; // 0 lub 1 dla within-cycle, -1 dla between-cycles
    public long delta;
    public enum Applicability {
        APPLICABLE,
        INVALID,
        REVERSED
    }

    public Applicability checkApplicability(Solution solution) {
        List<Integer> cycle = null;


        if (this.type == Type.TWO_EDGE_SWAP) {
            cycle = (this.cycle == 0) ? solution.cycle1 : solution.cycle2;
            int size = cycle.size();

            if (i + 1 >= size || j + 1 >= size) return Applicability.INVALID;

            int a = cycle.get(i);
            int b = cycle.get(i + 1);
            int c = cycle.get(j);
            int d = cycle.get(j + 1);

            boolean ab = edgeExistsInDirection(cycle, a, b);
            boolean cd = edgeExistsInDirection(cycle, c, d);

            boolean ba = edgeExistsInDirection(cycle, b, a);
            boolean dc = edgeExistsInDirection(cycle, d, c);

            if (!ab && cd) {
                return Applicability.REVERSED;
            }
            if (ab && !cd) return Applicability.REVERSED;

            return Applicability.APPLICABLE;
        }

        if (this.type == Type.SWAP_BETWEEN_CYCLES) {
            return isStillValidForBetweenCycles(solution) ? Applicability.APPLICABLE : Applicability.INVALID;
        }

        return Applicability.INVALID;
    }


    private boolean edgeExistsInDirection(List<Integer> cycle, int from, int to) {
        int size = cycle.size();
        for (int i = 0; i < size; i++) {
            int u = cycle.get(i);
            int v = cycle.get((i + 1) % size);
            if (u == from && v == to) return true;
        }
        return false;
    }

    private boolean isStillValidForBetweenCycles(Solution solution) {
        return this.i < solution.cycle1.size() && this.j < solution.cycle2.size();
    }


    public Move(Type type, int i, int j, int cycle) {
        this.type = type;
        this.i = i;
        this.j = j;
        this.cycle = cycle;
    }

    public Move(Type type, int i, int j, int cycle, long delta) {
        this.type = type;
        this.i = i;
        this.j = j;
        this.cycle = cycle;
        this.delta = delta;
    }

    public enum Type {
        SWAP_BETWEEN_CYCLES,
        SWAP_WITHIN_CYCLE,
        TWO_EDGE_SWAP
    }

    public boolean isApplicable(Solution solution) {
        try {
            if (type == Type.SWAP_WITHIN_CYCLE) {
                if (cycle == 0) {
                    return i >= 0 && j >= 0 && i < solution.cycle1.size() && j < solution.cycle1.size();
                } else {
                    return i >= 0 && j >= 0 && i < solution.cycle2.size() && j < solution.cycle2.size();
                }
            } else if (type == Type.SWAP_BETWEEN_CYCLES) {
                return i >= 0 && j >= 0 &&
                       i < solution.cycle1.size() && j < solution.cycle2.size();
            } else if (type == Type.TWO_EDGE_SWAP) {
                List<Integer> cycleList = (cycle == 0) ? solution.cycle1 : solution.cycle2;
                return i >= 0 && j >= 0 && j > i + 1 && j < cycleList.size();
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
