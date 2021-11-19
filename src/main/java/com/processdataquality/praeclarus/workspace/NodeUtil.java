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

package com.processdataquality.praeclarus.workspace;

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.workspace.node.Node;
import com.processdataquality.praeclarus.workspace.node.NodeFactory;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 12/5/21
 */
public class NodeUtil {


    /**
     * Removes a node from the workspace, disconnecting it from all predecessor
     * and successor nodes
     * @param node the node to remove
     */
    public void removeNode(Node node) {
        node.previous().forEach(previous -> disconnect(previous, node));
        node.next().forEach(next -> disconnect(node, next));
    }


    /**
     * Connects two nodes
     * @param source the source node of the directed connection
     * @param target the target node of the directed connection
     */
    public void connect(Node source, Node target) {
        source.addNext(target);
        target.addPrevious(source);
    }


    /**
     * Disconnects two nodes
     * @param source the source node of the directed connection
     * @param target the target node of the directed connection
     */
    public void disconnect(Node source, Node target) {
        source.removeNext(target);
        target.removePrevious(source);
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


    public Node fromJson(JSONObject json) throws JSONException, IOException {
        Node node = null;
        PDQPlugin plugin = newPluginInstance(json.getString("plugin"));
        if (plugin != null) {
            addOptions(plugin, json.getJSONObject("options"));
            String nodeID = json.getString("id");
            String commitID = json.optString("commitID");
            String tableID = json.optString("tableID");
            node = NodeFactory.create(plugin, nodeID);
            if (!commitID.isEmpty()) {
                node.setCommitID(commitID);
                if (!tableID.isEmpty()) {
                    node.loadOutput(tableID);           // loads from repo
                }
            }
        }
        return node;
    }


    @SuppressWarnings("unchecked")
    private PDQPlugin newPluginInstance(String fqClassName) {
        try {
            Class<PDQPlugin> clazz = (Class<PDQPlugin>) Class.forName(fqClassName);
            return clazz.getDeclaredConstructor().newInstance();
        }
        catch (Throwable e) {
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    private void addOptions(PDQPlugin plugin, JSONObject jsonOptions) throws JSONException {
        if (jsonOptions != null) {
            Iterator<String> keys = jsonOptions.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                plugin.getOptions().add(key, jsonOptions.get(key));
            }
        }
    }


}
