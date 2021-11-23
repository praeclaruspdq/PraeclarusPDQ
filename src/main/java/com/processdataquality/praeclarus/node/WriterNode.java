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

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.writer.DataWriter;
import tech.tablesaw.api.Table;

import java.io.IOException;

/**
 * A container node for a log data writer
 *
 * @author Michael Adams
 * @date 12/5/21
 */
public class WriterNode extends Node {

    public WriterNode(PDQPlugin plugin, String id) {
        super(plugin, id);
    }


    /**
     * Gets incoming data from a predecessor node and writes it to a data 'sink' as
     * defined in this node's plugin
     */
    @Override
    public void run() {
        setState(NodeState.EXECUTING);
        try {
            Table input = getInputs().get(0);     // a writer node has only one input
            ((DataWriter) getPlugin()).write(input);
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
        setState(NodeState.COMPLETED);
    }
    
}
