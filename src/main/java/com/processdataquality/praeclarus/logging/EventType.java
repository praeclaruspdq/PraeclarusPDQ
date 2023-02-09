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

package com.processdataquality.praeclarus.logging;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 30/11/21
 */
public enum EventType {

    // Authentication
    LOGON_SUCCESS("Success"),
    LOGON_FAIL("Failed"),
    LOGOFF("Logoff"),

    // Graph
    GRAPH_CREATED("Graph created"),
    GRAPH_UPLOADED("Graph uploaded"),
    GRAPH_DOWNLOADED("Graph saved"),
    GRAPH_LOADED("Graph loaded"),
    GRAPH_STORED("Graph stored"),
    GRAPH_DISCARDED("Graph discarded"),

    // Node
    NODE_ADDED("Node added"),
    NODE_REMOVED("Node removed"),

    // Options
    OPTION_VALUE_CHANGED("Option value changed"),

    // Connector
    CONNECTOR_ADDED("Connector added"),
    CONNECTOR_REMOVED("Connector removed"),

    // Execution
    NODE_COMPLETED("Node completed"),
    NODE_ROLLBACK("Node rolled back"),
    NODE_PAUSED("Node paused"),
    NODE_PATTERN_DETECTED("Pattern Node detect"),
    NODE_PATTERN_REPAIRED("Pattern node repair")
    ;

    private final String _label;
    private static final Map<String, EventType> _map;

    static {
        Map<String, EventType> map = new HashMap<>();
        for (EventType constant : EventType.values()) {
            map.put(constant.asString(), constant);
        }
        _map = Collections.unmodifiableMap(map);
    }


    EventType(String label) {
        _label = label;
    }

    public String asString() { return _label; }

    public static EventType fromString(String label) {
        return _map.get(label);
    }

}
