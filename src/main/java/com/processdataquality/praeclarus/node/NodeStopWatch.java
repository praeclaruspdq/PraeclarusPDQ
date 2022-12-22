/*
 * Copyright (c) 2022 Queensland University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.processdataquality.praeclarus.node;

import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Records node execution times
 * @author Michael Adams
 * @date 20/5/2022
 */
public class NodeStopWatch {

    private static final double NANO = Math.pow(10, 9);

    private long started;

    // A node may start and pause execution several times
    private final List<Pair<Long, Long>> stages = new ArrayList<>();


    public NodeStopWatch() { }

    
    public void stateChange(NodeState state) {
        switch (state) {
            case EXECUTING: stages.clear();                    // deliberate fallthrough
            case RESUMED: started = now(); break;
            case COMPLETED:                                    // deliberate fallthrough
            case PAUSED: stages.add(Pair.of(started, now())); break;
        }
    }
    

    /** @return the duration of a single, unpaused, execution */
    public long getDuration() {
        return getDuration(0);
    }


    public long getDuration(int index) {
        if (stages.size() < index - 1) return -1;
        return stages.get(index).getSecond() - stages.get(index).getFirst();
    }


    public double getDurationAsMillis() { return getDurationAsMillis(0); }

    public double getLastDurationAsMillis() {
        return getDurationAsMillis(stages.size() -1);
    }


    public double getDurationAsMillis(int index) {
        return getDurationAsSeconds(index) * 1000D;
    }


    public double getDurationAsSeconds() { return getDurationAsSeconds(0); }

    public double getLastDurationAsSeconds() {
        return getDurationAsSeconds(stages.size() -1);
    }

    public double getDurationAsSeconds(int index) { return getDuration(index) / NANO; }


    private long now() { return System.nanoTime(); }

}
