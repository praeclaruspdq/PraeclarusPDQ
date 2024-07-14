package com.processdataquality.praeclarus.support.swiftmend;

import java.util.Objects;

public class SimilarityResult {
    private int i;
    private int k;
    private float similarityValue;

    public SimilarityResult(int i, int k, float similarityValue) {
        this.i = i;
        this.k = k;
        this.similarityValue = similarityValue;
    }

    public int getI() {
        return i;
    }

    public int getK() {
        return k;
    }

    public float getSimilarityValue() {
        return similarityValue;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimilarityResult that = (SimilarityResult) o;
        return (i == that.i && k == that.k && Float.compare(that.similarityValue, similarityValue) == 0) ||
                (i == that.k && k == that.i && Float.compare(that.similarityValue, similarityValue) == 0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Math.min(i, k), Math.max(i, k), similarityValue);
    }
}