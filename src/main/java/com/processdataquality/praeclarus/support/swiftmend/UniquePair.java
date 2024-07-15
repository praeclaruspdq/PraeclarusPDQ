package com.processdataquality.praeclarus.support.swiftmend;

import java.util.Objects;

public class UniquePair {
    private final int first;
    private final int second;

    public UniquePair(int a, int b) {
        this.first = Math.min(a, b);
        this.second = Math.max(a, b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniquePair that = (UniquePair) o;
        return first == that.first && second == that.second;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    // Getters
    public int getFirst() { return first; }
    public int getSecond() { return second; }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}