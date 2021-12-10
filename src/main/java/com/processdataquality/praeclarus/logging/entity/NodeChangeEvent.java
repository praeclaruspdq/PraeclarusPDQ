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
public class NodeChangeEvent extends NodeEvent {

    private String option;
    private String oldValue;
    private String newValue;

    protected NodeChangeEvent() { }

    public NodeChangeEvent(String user, Node node, String option,
                           String oldValue, String newValue) {
        super(user, LogConstant.NODE_CHANGED, node);
        setOption(option);
        setOldValue(oldValue);
        setNewValue(newValue);
    }


    public String getOption() { return option; }

    public void setOption(String option) { this.option = option; }


    public String getOldValue() { return oldValue; }

    public void setOldValue(String reason) { this.oldValue = reason; }


    public String getNewValue() { return newValue; }

    public void setNewValue(String newName) { this.newValue = newName; }


    @Override
    public String toString() {
        return super.toString() + "; Option: " + getOption() + "; Old Value: " + getOldValue() +
                "; New Value: " + getNewValue();
    }
}
