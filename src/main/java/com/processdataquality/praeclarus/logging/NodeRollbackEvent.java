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

import com.processdataquality.praeclarus.node.Node;

import javax.persistence.Entity;

/**
 * @author Michael Adams
 * @date 30/11/21
 */
@Entity
public class NodeRollbackEvent extends AbstractLogEvent {

    private String nodeId;
    private String nodeName;

    protected NodeRollbackEvent() { }

    public NodeRollbackEvent(String user, Node node) {
        super(user, LogConstant.NODE_ROLLBACK);
        setNodeId(node.getInternalID());
        setNodeName(node.getName());
    }


    public String getNodeId() { return nodeId; }

    public void setNodeId(String nodeId) { this.nodeId = nodeId; }


    public String getNodeName() { return nodeName; }

    public void setNodeName(String nodeName) { this.nodeName = nodeName; }


    @Override
    public String toString() {
        return super.toString() + "; Node: " + nodeName;
    }
    
}
