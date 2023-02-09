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

import com.processdataquality.praeclarus.exception.ReaderException;
import com.processdataquality.praeclarus.plugin.AbstractPlugin;
import com.processdataquality.praeclarus.reader.DataReader;
import tech.tablesaw.api.Table;

import java.util.UUID;

/**
 * A container node for a log data reader
 *
 * @author Michael Adams
 * @date 12/5/21
 */
public class ReaderNode extends Node {

    public ReaderNode(AbstractPlugin plugin) {
        super(plugin);
    }


    /**
     * Reads log data from the plugin contained by this node and sets the data as the
     * node's output 
     */
    @Override
    public void run() throws Exception {
        setState(NodeState.EXECUTING);

        try {
            Table table = ((DataReader) getPlugin()).read();     // load from source
            table.setName(UUID.randomUUID().toString());
            setOutput(table);
            setState(NodeState.COMPLETED);
        }
        catch (Exception e) {

            // see if file was loaded previously, then stored
            String tableID = getTableID();
            if (! (tableID == null || tableID.isEmpty())) {
                loadOutput(tableID);                             // load from repo

                // setState not used here because loadOutput() above sets it to completed
                announceStateChange();
            }
            else throw new ReaderException(e.getMessage(), e.getCause());
        }

    }

}
