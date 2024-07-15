package com.processdataquality.praeclarus.support.swiftmend;


import java.util.Objects;

public class ActivityPair {
    private final int activityIndex1;
    private final int activityIndex2;

    public ActivityPair(int activityIndex1, int activityIndex2) {
        this.activityIndex1 = activityIndex1;
        this.activityIndex2 = activityIndex2;
    }

    public int getActivityIndex1() {
        return activityIndex1;
    }

    public int getActivityIndex2() {
        return activityIndex2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityPair that = (ActivityPair) o;
        return (activityIndex1 == that.activityIndex1 && activityIndex2 == that.activityIndex2) ||
                (activityIndex1 == that.activityIndex2 && activityIndex2 == that.activityIndex1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityIndex1, activityIndex2) + Objects.hash(activityIndex2, activityIndex1);
    }
}
