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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 7/12/21
 */
@Entity
public class NetworkEntity {

    @Id
    private String id;

    private String name;
    private String creator;
    private String owner;
    private LocalDateTime createdTime;
    private LocalDateTime lastSavedTime;
    private String content;

    @OneToMany
    private Set<NodeEntity> nodeSet;

    public NetworkEntity() { }

    public NetworkEntity(String id, String name, String creator) {
        setId(id);
        setName(name);
        setCreator(creator);
        setOwner(creator);                             // creator is original owner too
        createdTime = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getLastSavedTime() {
        return lastSavedTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        lastSavedTime = LocalDateTime.now();
    }
}
