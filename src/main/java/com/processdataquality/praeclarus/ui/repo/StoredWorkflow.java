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

package com.processdataquality.praeclarus.ui.repo;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * @author Michael Adams
 * @date 4/5/2022
 */
@Entity
public class StoredWorkflow {

    @Id
    private String id;
    private String owner;
    private boolean shared;

    @Column(length=102400)
    private String json;

    @Transient
    JsonObject jsonObject;


    public StoredWorkflow() { }

    public StoredWorkflow(String id, String owner, boolean shared, String json) {
        this.id = id;
        this.owner = owner;
        this.shared = shared;
        this.json = json;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean hasOwner(String owner) { return getOwner().equals(owner); }


    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public String getWorkflowJson() {
        return json;
    }

    public void setWorkflowJson(String json) {
        this.json = json;
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return parseString("description");
    }

    public String getName() {
        return parseString("name");
    }

    public String getCreationTime() {
        return parseString("creationTime");
    }

    public String getLastSavedTime() {
        return parseString("lastSavedTime");
    }

    public String toString() { return getWorkflowJson(); }


    public JsonObject toSummaryJson() {
        JsonObject json = new JsonObject();
        json.add("id", getId());
        json.add("name", getName());
        json.add("owner", owner);
        json.add("public", isShared());
        json.add("description", getDescription());
        json.add("creationTime", getCreationTime());
        json.add("lastSavedTime", getLastSavedTime());
        return json;
    }


    private String parseString(String field) {
        String value = getJsonObject().getString(field, null);
        return value != null ? value : "No " + field;
    }


    private JsonObject getJsonObject() {
        if (jsonObject == null) {
            jsonObject = Json.parse(json).asObject();
        }
        return jsonObject;
    }

}
