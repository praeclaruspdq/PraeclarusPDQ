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

package com.processdataquality.praeclarus.node;

import com.processdataquality.praeclarus.pattern.ImperfectionPattern;
import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.plugin.uitemplate.PluginUI;
import tech.tablesaw.api.Table;

/**
 * A container node for an imperfection pattern plugin
 *
 * @author Michael Adams
 * @date 12/5/21
 */
public class PatternNode extends Node {


    private Table detected;                 // a table of pattern matches


    public PatternNode(PDQPlugin plugin, String id) {
        super(plugin, id);
    }


    /**
     * Gets incoming data from a predecessor node and either detects or repairs,
     * based on the current node state, using its defined algorithm
     */
    @Override
    public void run() {
        ImperfectionPattern imperfectionPattern = (ImperfectionPattern) getPlugin();
        Table master = getInputs().get(0);         // only one input
        if (getState() == NodeState.UNSTARTED && imperfectionPattern.canDetect()) {
            setState(NodeState.EXECUTING);
            detected = imperfectionPattern.detect(master);
            if (imperfectionPattern.canRepair()) {
                setState(NodeState.PAUSED);
            }
            else {
                setOutput(master);
                setState(NodeState.COMPLETED);
            }
        }
        else if (getState() != NodeState.COMPLETED && imperfectionPattern.canRepair()) {
            setOutput(imperfectionPattern.repair(master));
            setState(NodeState.COMPLETED);
        }
    }


    /**
     * The output returned depends on the current node state
     * @return if completed, the detected imperfections, else the repaired output
     */
    @Override
    public Table getOutput() {
        if (getState() == NodeState.PAUSED) {
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
    }


    /**
     * Updates the plugin's UI template with any changes made at the front end
     * @param ui the updated UI
     */
    public void updateUI(PluginUI ui) {
       ((ImperfectionPattern) getPlugin()).setUI(ui);
    }

}
