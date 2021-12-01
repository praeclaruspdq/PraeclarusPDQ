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

package com.processdataquality.praeclarus.logging;

/**
 * @author Michael Adams
 * @date 30/11/21
 */
public enum LogConstant {

    // Authentication
    LOGON_SUCCESS("Successful Logon"),
    LOGON_FAIL("Failed Logon"),
    LOGOFF("Logoff"),

    // Workflow
    WORKFLOW_CREATED("Workflow created"),
    WORKFLOW_LOADED("Workflow loaded"),
    WORKFLOW_SAVED("Workflow saved"),
    WORKFLOW_RENAMED("Workflow renamed"),

    // Node
    NODE_ADDED("Node added"),
    NODE_REMOVED("Node removed"),
    NODE_CHANGED("Node changed"),

    // Connector
    CONNECTOR_ADDED("Connector added"),
    CONNECTOR_REMOVED("Connector removed"),

    // Execution
    NODE_RUN("Node executed"),
    NODE_ROLLBACK("Node rolled back"),
    NODE_PATTERN_DETECTED("Pattern Node detect"),
    NODE_PATTERN_REPAIRED("Pattern node repair")
    ;

    private final String _label;

    LogConstant(String label) { _label = label; }

    public String asString() { return _label; }

}
