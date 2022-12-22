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
import com.processdataquality.praeclarus.option.Option;

import javax.persistence.Entity;

/**
 * @author Michael Adams
 * @date 30/11/21
 */
@Entity
public class OptionChangeEvent extends AbstractLogEvent {

    private String componentId;
    private String componentLabel;
    private String optionName;
    private String oldValue;
    private String newValue;

    protected OptionChangeEvent() { }

    public OptionChangeEvent(String componentId, String componentLabel, String user,
                             Option option) {
        super(user, EventType.OPTION_VALUE_CHANGED);
        setComponentId(componentId);
        setComponentLabel(componentLabel);
        setOptionName(option.key());
        setOldValue(String.valueOf(option.getPreviousValue()));
        setNewValue(option.asString());
    }


    public String getComponentId() { return componentId; }

    public void setComponentId(String componentId) { this.componentId = componentId; }


    public String getComponentLabel() { return componentLabel; }

    public void setComponentLabel(String componentLabel) {
        this.componentLabel = componentLabel;
    }


    public String getOptionName() { return optionName; }

    public void setOptionName(String option) { this.optionName = option; }


    public String getOldValue() { return oldValue; }

    public void setOldValue(String reason) { this.oldValue = reason; }


    public String getNewValue() { return newValue; }

    public void setNewValue(String newName) { this.newValue = newName; }


    @Override
    public String toString() {
        return super.toString() + "; Option: " + getOptionName() + ": " + getOldValue() +
                " -->" + getNewValue();
    }
}
