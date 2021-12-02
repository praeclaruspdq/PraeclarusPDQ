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

import javax.persistence.Entity;

/**
 * @author Michael Adams
 * @date 30/11/21
 */
@Entity
public class WorkflowRenameEvent extends AbstractLogEvent {

    private String oldName;
    private String newName;

    protected WorkflowRenameEvent() { }

    public WorkflowRenameEvent(String user, String oldName, String newName) {
        super(user, LogConstant.WORKFLOW_RENAMED);
        setOldName(oldName);
        setNewName(newName);
    }


    public String getOldName() { return oldName; }

    public void setOldName(String reason) { this.oldName = reason; }


    public String getNewName() { return newName; }

    public void setNewName(String newName) { this.newName = newName; }


    @Override
    public String toString() {
        return super.toString() + " From: " + oldName + ", To: " + newName;
    }

}
