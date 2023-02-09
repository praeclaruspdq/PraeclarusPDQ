/*
 * Copyright (c) 2021-2022 Queensland University of Technology
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

import com.processdataquality.praeclarus.pattern.AbstractDataPattern;
import com.processdataquality.praeclarus.plugin.AbstractPlugin;
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


    public PatternNode(AbstractPlugin plugin) {
        super(plugin);
    }


    /**
     * Gets incoming data from a predecessor node and either detects or repairs,
     * based on the current node state, using its defined algorithm
     */
    @Override
    public void run() throws Exception {
        AbstractDataPattern imperfectionPattern = (AbstractDataPattern) getPlugin();
        imperfectionPattern.getInputs().addAll(getInputs());
        Table master = getInputs().get(0);         // first input is the master
        if (getState() == NodeState.UNSTARTED && imperfectionPattern.canDetect()) {

            // load plugin with all incoming plugins' aux datasets
            imperfectionPattern.getAuxiliaryDatasets().putAll(getAuxiliaryInputs());
            
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
            setState(NodeState.RESUMED);
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
    public void reset() throws Exception {
        super.reset();
        detected = null;
    }


    /**
     * @return the table of detected results
     */
    public Table getDetected() {
        return detected;
    }


    /**
     * Updates the plugin's UI template with any changes made at the front end
     * @param ui the updated UI
     */
    public void updateUI(PluginUI ui) {
       ((AbstractDataPattern) getPlugin()).setUI(ui);
    }

}
