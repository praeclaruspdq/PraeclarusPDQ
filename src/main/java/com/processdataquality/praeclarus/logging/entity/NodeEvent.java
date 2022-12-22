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

package com.processdataquality.praeclarus.logging.entity;

import com.processdataquality.praeclarus.logging.EventType;
import com.processdataquality.praeclarus.graph.Graph;
import com.processdataquality.praeclarus.node.Node;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * @author Michael Adams
 * @date 30/11/21
 */
@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public class NodeEvent extends AbstractGraphEvent {

    private String nodeId;
    private String nodeName;
    
    protected NodeEvent() { }

    public NodeEvent(Graph graph, Node node, EventType label, String user) {
        super(graph, user, label);
        setNodeId(node.getID());
        setNodeName(node.getLabel());
    }


    public String getNodeId() { return nodeId; }

    public void setNodeId(String nodeId) { this.nodeId = nodeId; }


    public String getNodeName() { return nodeName; }

    public void setNodeName(String nodeName) { this.nodeName = nodeName; }

    @Override
    public String toString() {
        return super.toString() + "; " + nodeName + " [" + nodeId + "]";
    }
}
