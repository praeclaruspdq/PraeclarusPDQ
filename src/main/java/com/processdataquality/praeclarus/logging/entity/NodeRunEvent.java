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

package com.processdataquality.praeclarus.logging.entity;

import com.processdataquality.praeclarus.logging.LogConstant;
import com.processdataquality.praeclarus.node.Node;

import javax.persistence.Entity;

/**
 * @author Michael Adams
 * @date 30/11/21
 */
@Entity
public class NodeRunEvent extends AbstractLogEvent {

    private String nodeId;
    private String nodeName;
    private String tableId;
    private String commitId;
    private String outcome;

    protected NodeRunEvent() { }

    public NodeRunEvent(String user, Node node, String outcome) {
        super(user, LogConstant.NODE_RUN);
        setNodeId(node.getInternalID());
        setNodeName(node.getName());
        setTableId(node.getTableID());
        setCommitId(node.getCommitID());
        setOutcome(outcome);
    }


    public String getNodeId() { return nodeId; }

    public void setNodeId(String nodeId) { this.nodeId = nodeId; }


    public String getNodeName() { return nodeName; }

    public void setNodeName(String nodeName) { this.nodeName = nodeName; }


    public String getTableId() { return tableId; }

    public void setTableId(String tableId) { this.tableId = tableId; }


    public String getCommitId() { return commitId; }

    public void setCommitId(String commitId) { this.commitId = commitId; }


    public String getOutcome() { return outcome; }

    public void setOutcome(String outcome) { this.outcome = outcome; }


    @Override
    public String toString() {
        return super.toString() + "; Node: " + nodeName + "; Result: " + outcome;
    }
    
}
