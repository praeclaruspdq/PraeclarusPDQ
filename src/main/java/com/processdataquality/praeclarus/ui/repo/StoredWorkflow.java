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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

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

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }
}
