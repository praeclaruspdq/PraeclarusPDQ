/*
 * Copyright (c) 2022 Queensland University of Technology
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

package com.processdataquality.praeclarus.graph;

import com.processdataquality.praeclarus.logging.EventType;
import com.processdataquality.praeclarus.node.Node;

/**
 * @author Michael Adams
 * @date 19/5/2022
 */
public class GraphRunnerEvent {

    private final EventType eventType;
    private final Node node;

    public GraphRunnerEvent(EventType eventType, Node node) {
        this.eventType = eventType;
        this.node = node;
    }


    public Node getNode() { return node; }


    public EventType getEventType() { return eventType; }
}
