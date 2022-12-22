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

import com.processdataquality.praeclarus.action.AbstractAction;
import com.processdataquality.praeclarus.plugin.AbstractPlugin;

/**
 * A container node for a generic action to be performed on data inputs
 *
 * @author Michael Adams
 * @date 12/5/21
 */
public class ActionNode extends Node {

    public ActionNode(AbstractPlugin plugin) {
        super(plugin);
    }


    /**
     * Runs a plugin's AbstractAction algorithm using this node's input tables and write the
     * result to the output table
     */
    @Override
    public void run() throws Exception {
        setState(NodeState.EXECUTING);
        setOutput(((AbstractAction) getPlugin()).run(getInputs()));
        setState(NodeState.COMPLETED);
    }
}
