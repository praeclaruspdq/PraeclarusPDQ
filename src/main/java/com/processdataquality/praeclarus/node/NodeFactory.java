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
import com.processdataquality.praeclarus.pattern.AbstractDataPattern;
import com.processdataquality.praeclarus.plugin.AbstractPlugin;
import com.processdataquality.praeclarus.reader.DataReader;
import com.processdataquality.praeclarus.writer.DataWriter;

import java.util.UUID;

/**
 * Creates and returns the appropriate Node to contain a plugin
 *
 * @author Michael Adams
 * @date 13/5/21
 */
public class NodeFactory {

    // called when creating a new node
    public static Node create(AbstractPlugin plugin) {
        return create(plugin, UUID.randomUUID().toString());
    }


    // called when restoring a node in a saved workflow
    public static Node create(AbstractPlugin plugin, String id) {
        plugin.setID(id);
        Node node = null;
        if (plugin instanceof DataReader) {
            node = new ReaderNode(plugin);
        }
        if (plugin instanceof DataWriter) {
            node = new WriterNode(plugin);
        }
        if (plugin instanceof AbstractDataPattern) {
            node = new PatternNode(plugin);
        }
        if (plugin instanceof AbstractAction) {
            node = new ActionNode(plugin);
        }

        return node;
    }
}
