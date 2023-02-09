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

/**
 * @author Michael Adams
 * @date 30/11/21
 */
@Entity
public class ConnectorEvent extends AbstractGraphEvent {

    private String sourceID;
    private String sourceLabel;
    private String targetID;
    private String targetLabel;

    protected ConnectorEvent() { }

    public ConnectorEvent(Graph graph, String user, EventType label, Node source, Node target) {
        super(graph, user, label);
        setGraphID(graph.getId());
        setGraphName(graph.getName());
        setSourceID(source.getID());
        setSourceLabel(source.getLabel());
        setTargetID(target.getID());
        setTargetLabel(target.getLabel());
    }


    public String getSourceID() { return sourceID;}

    public void setSourceID(String sourceID) { this.sourceID = sourceID; }


    public String getSourceLabel() {return sourceLabel; }

    public void setSourceLabel(String source) { this.sourceLabel = source; }


    public String getTargetID() { return targetID; }

    public void setTargetID(String targetID) { this.targetID = targetID; }
    

    public String getTargetLabel() { return targetLabel; }

    public void setTargetLabel(String target) { this.targetLabel = target; }


    @Override
    public String toString() {
        return super.toString() + "; Source: " + sourceLabel + ", Target: " + targetLabel;
    }
}


