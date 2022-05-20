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
import java.util.concurrent.TimeUnit;

/**
 * @author Michael Adams
 * @date 20/5/2022
 */
public class NodeStopWatch implements NodeStateChangeListener {

    private final long started;
    private long stageStart;
    private final List<Pair<Long, Long>> stages = new ArrayList<>();
    private long completed;


    public NodeStopWatch() {
        started = now();
        stageStart = started;
    }


    @Override
    public void nodeStateChanged(Node node) throws Exception {
        switch (node.getState()) {
            case COMPLETED: completed = System.nanoTime(); break;
            case PAUSED: stages.add(Pair.of(stageStart, now())); break;
            case RESUMED: stageStart = now(); break;
        }
    }
    

    public long getDuration() {
        return completed > 0 ? completed - started : -1;
    }


    public long getDuration(TimeUnit unit) {
        return TimeUnit.NANOSECONDS.convert(getDuration(), unit);
    }


    public List<Long> getDurations() {
        List<Long> durations = new ArrayList<>();
        if (stages.isEmpty()) {
            durations.add(getDuration());
        }
        else {
            stages.forEach(s -> durations.add(s.getSecond() - s.getFirst()));
            durations.add(completed - stageStart);
        }
        return durations;
    }


    public List<Long> getDurations(TimeUnit unit) {
        List<Long> durations = new ArrayList<>();
        getDurations().forEach(d -> durations.add(getDuration(unit)));
        return durations;
    }


    private long now() { return System.nanoTime(); }

}
