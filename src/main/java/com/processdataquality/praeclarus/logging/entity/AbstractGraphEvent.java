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

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * @author Michael Adams
 * @date 30/11/21
 */
@Entity
@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
public class AbstractGraphEvent extends AbstractLogEvent {

    private String graphID;
    private String graphName;

    protected AbstractGraphEvent() { }

    public AbstractGraphEvent(Graph graph, String user, EventType label) {
        this(graph.getId(), graph.getName(), user, label);
    }


    public AbstractGraphEvent(String id, String name, String user, EventType label) {
         super(user, label);
         setGraphID(id);
         setGraphName(name);
     }


    public String getGraphID() { return graphID; }

    public void setGraphID(String graphID) { this.graphID = graphID; }


    public String getGraphName() { return graphName; }

    public void setGraphName(String graphName) { this.graphName = graphName; }


    @Override
    public String toString() {
        return super.toString() + "; " + graphName + " [" + graphID + "]";
    }
}


