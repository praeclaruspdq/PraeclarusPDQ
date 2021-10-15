/*
 * Copyright (c) 2021 Queensland University of Technology
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

package com.processdataquality.praeclarus.workspace.node;

import com.processdataquality.praeclarus.pattern.ImperfectionPattern;
import com.processdataquality.praeclarus.plugin.PDQPlugin;
import tech.tablesaw.api.Table;

/**
 * A container node for an imperfection pattern plugin
 *
 * @author Michael Adams
 * @date 12/5/21
 */
public class PatternNode extends Node {

    // unlike other nodes, a pattern node can be in one of four states
    public enum State { IDLE, DETECTED, REPAIRING, COMPLETED }

    private Table detected;                 // a table of pattern matches
    private Table repairs;                  // a table of repair rows (to be performed)
    private State state = State.IDLE;


    public PatternNode(PDQPlugin plugin) {
        super(plugin);
    }


    /**
     * Gets incoming data from a predecessor node and either detects or repairs,
     * based on the current node state, using its defined algorithm
     */
    @Override
    public void run() {
        ImperfectionPattern imperfectionPattern = (ImperfectionPattern) getPlugin();
        Table master = getInputs().get(0);         // only one input
        if (state == State.IDLE && imperfectionPattern.canDetect()) {
            detected = imperfectionPattern.detect(master);
            if (imperfectionPattern.canRepair()) {
                state = State.DETECTED;
            }
            else {
                state = State.COMPLETED;
                setOutput(master);
            }
        }
        else if (state != State.COMPLETED && imperfectionPattern.canRepair()) {
             if (repairs != null) {

                 // perform repairs on a new copy of the log so we can rollback if needed
                 setOutput(imperfectionPattern.repair(master.copy(), repairs));
             }
            state = State.COMPLETED;
        }
    }


    /**
     * Overrides super method
     * @return true if this node has completed its run
     */
    @Override
    public boolean hasCompleted() { return state == State.COMPLETED; }


    /**
     * The output returned depends on the current node state
     * @return if completed, the detected imperfections, else the repaired output
     */
    @Override
    public Table getOutput() {
        if (state == State.DETECTED) {
            return detected;
        }
        return super.getOutput();
    }


    /**
     * Sets this node back to its pre-run state
     */
    @Override
    public void reset() {
        super.reset();
        detected = null;
        repairs = null;
        state = State.IDLE;
    }


    /**
     * @return the current state of this node
     */
    public State getState() { return state; }


    /**
     * @return the table of detected imperfections
     */
    public Table getDetected() { return detected; }


    /**
     * Sets the table with the repair rows (to be performed)
     * @param r the table to set as the repaired table
     */
    public void setRepairs(Table r) { repairs = r; }
    
}
