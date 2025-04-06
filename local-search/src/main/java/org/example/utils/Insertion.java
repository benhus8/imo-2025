package org.example.utils;

public class Insertion {
    public int insertPosition;
    public int cost;
    public int belongsToCycle; // 1/2
    public Insertion(int pos, int cost) {
        this.insertPosition = pos;
        this.cost = cost;
    }
}
