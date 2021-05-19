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

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.reader.DataReader;
import tech.tablesaw.api.Table;

import java.io.IOException;

/**
 * @author Michael Adams
 * @date 12/5/21
 */
public class ReaderNode extends Node {

    public ReaderNode(PDQPlugin plugin) { 
        super(plugin);
        setAllowedInputs(0);
        setAllowedOutputs(1);
    }

    @Override
    public Table run() {
        try {
            Table t = ((DataReader) getPlugin()).read();
            setOutput(t);
            return t;
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
            return null;
        }
    }
}
