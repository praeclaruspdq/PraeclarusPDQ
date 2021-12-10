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

package com.processdataquality.praeclarus.node;

import com.processdataquality.praeclarus.logging.Logger;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Michael Adams
 * @date 7/12/21
 */
@Entity
public class Network {

    @Id
    private String id;
    private String name;
    private String creator;
    private String owner;
    private String description;
    private LocalDateTime creationTime;
    private LocalDateTime lastSavedTime;
    private String userContent;

    @Transient
    private final Set<Node> nodeSet = new HashSet<>();


    private Network(Builder builder) {
        creator = builder.creator;
        id = builder.id != null ? builder.id : UUID.randomUUID().toString();
        name = builder.name != null ? builder.name : "New Network";
        owner = builder.owner != null ? builder.owner : creator;
        creationTime = builder.creationTime != null ? builder.creationTime :
                LocalDateTime.now();
        lastSavedTime = builder.lastSavedTime;
        description = builder.description;
        userContent = builder.userContent;
        Logger.saveNetworkIfNew(this);
    }


    public String getId() { return id; }

    public String getCreator() { return creator; }

    public LocalDateTime getCreationTime() { return creationTime; }

    public LocalDateTime getLastSavedTime() { return lastSavedTime; }


    public String getName() { return name; }

    public void updateName(String name)  {
        setName(name);
        save();
    }


    public String getOwner() { return owner; }

    public void updateOwner(String owner)  {
        setOwner(owner);
        save();
    }


    public String getDescription() { return description; }

    public void updateDescription(String description) {
        setDescription(description);
        save();
    }

    
    public String getUserContent() { return userContent; }

    public void updateUserContent(String content) {
        setUserContent(content);
        save();
    }


    public JSONObject asJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("creator", creator);
        json.put("owner", owner);
        json.put("creationTime", creationTime.format(Logger.dtFormatter));
        if (description != null) {
            json.put("description", description);
        }
        if (lastSavedTime != null) {
            json.put("lastSavedTime", lastSavedTime.format(Logger.dtFormatter));
        }
        return json;
    }


    public void addNode(Node node) {
        nodeSet.add(node);
    //    Logger.saveNode(node);
    }


    /**
     * Removes a node from the workspace, disconnecting it from all predecessor
     * and successor nodes
     * @param node the node to remove
     */
    public void removeNode(Node node) {
        node.previous().forEach(previous -> previous.removeNext(node));
        node.next().forEach(next -> next.removePrevious(node));
        nodeSet.remove(node);
    }


    public Set<Node> getHeads() {
        if (!nodeSet.isEmpty()) {
            return getHeads(nodeSet.iterator().next());      // start from any node
        }
        return Collections.emptySet();
    }


    /**
     * Gets each head node on each branch that eventually target a node
     * @param node the node to get the heads for
     * @return the Set of head nodes that lead to the node passed
     */
    public Set<Node> getHeads(Node node) {
        Set<Node> heads = new HashSet<>();
        for (Node previous : node.previous()) {
            if (previous.isHead()) {
                heads.add(previous);
            }
            else heads.addAll(getHeads(previous)); // check all pre-set nodes recursively
        }
        if (heads.isEmpty()) heads.add(node);              // the node passed is a head
        return heads;
    }


    public Set<Node> getTails() {
        if (!nodeSet.isEmpty()) {
            return getTails(nodeSet.iterator().next());      // start from any node
        }
        return Collections.emptySet();
    }

    
    /**
     * Gets each tail node on each branch that is an eventual target of a node
     * @param node the node to get the tails for
     * @return the Set of tail nodes that lead from the node passed
     */
    public Set<Node> getTails(Node node) {
        Set<Node> tails = new HashSet<>();
        for (Node next : node.next()) {
            if (next.isTail()) {
                tails.add(next);
            }
            else tails.addAll(getTails(next));  // check all post-set nodes recursively
        }
        if (tails.isEmpty()) tails.add(node);     // the node passed is a tail
        return tails;
   }


   private void save() {
       Logger.saveNetwork(this);
   }

    // these are only used by JPA
    protected Network() { }
    protected void setId(String id) { this.id = id; }
    protected void setCreator(String creator) { this.creator = creator; }
    protected void setCreationTime(LocalDateTime time) { creationTime = time; }
    protected void setLastSavedTime(LocalDateTime time) { lastSavedTime = time; }
    protected void setName(String name)  { this.name = name; }
    protected void setOwner(String owner)  { this.owner = owner; }
    protected void setDescription(String description)  { this.description = description; }
    protected void setUserContent(String content) { userContent = content; }


   public static class Builder {
       private String id;
       private String name;
       private final String creator;
       private String owner;
       private String description;
       private LocalDateTime creationTime;
       private LocalDateTime lastSavedTime;
       private String userContent;

       public Builder(String creator) {
           this.creator = creator;
       }

       public Builder id(String id) {
           this.id = id;
           return this;
       }

       public Builder name(String name) {
           this.name = name;
           return this;
       }

       public Builder owner(String owner) {
           this.owner = owner;
           return this;
       }

       public Builder description(String description) {
           this.description = description;
           return this;
       }

       public Builder creationTime(LocalDateTime creationTime) {
           this.creationTime = creationTime;
           return this;
       }

       public Builder lastSavedTime(LocalDateTime lastSaved) {
           this.lastSavedTime = lastSaved;
           return this;
       }

       public Builder userContent(String content) {
           this.userContent = content;
           return this;
       }

       public Network build() {
           return new Network(this);
       }

   }

}
