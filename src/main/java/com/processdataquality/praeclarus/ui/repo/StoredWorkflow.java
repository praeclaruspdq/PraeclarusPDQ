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

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

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
    JSONObject jsonObject;


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


    public JSONObject toSummaryJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", getId());
        json.put("name", getName());
        json.put("owner", owner);
        json.put("public", isShared());
        json.put("description", getDescription());
        json.put("creationTime", getCreationTime());
        json.put("lastSavedTime", getLastSavedTime());
        return json;
    }


    private String parseString(String field) {
        try {
            String value = getJsonObject().optString(field);
            if (value != null) {
                return value;
            }
        }
        catch (JSONException e) {
            //desc not found;
        }
        return "No " + field;
    }


    private JSONObject getJsonObject() throws JSONException {
        if (jsonObject == null) {
            jsonObject = new JSONObject(json);
        }
        return jsonObject;
    }

}
