package org.example.utils;

public class Move {
    public final Type type;
    public final int i;
    public final int j;
    public final int cycle; // 0 lub 1 dla within-cycle, -1 dla between-cycles
    public final long delta;
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
}
