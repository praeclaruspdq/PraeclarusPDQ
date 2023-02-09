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

package com.processdataquality.praeclarus.ui.canvas;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.processdataquality.praeclarus.graph.Graph;
import com.processdataquality.praeclarus.logging.EventLogger;
import com.processdataquality.praeclarus.node.Node;
import com.processdataquality.praeclarus.node.NodeLoader;
import com.processdataquality.praeclarus.repo.graph.GraphStore;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 2/6/21
 */
public class WorkflowLoader {

    private final Workflow _workflow;      // frontend

    public WorkflowLoader(Workflow workflow) {
        _workflow = workflow;
    }


    public void load(String jsonStr) throws IOException {
        EventLogger.ignoreEvents();                  // suppress events while loading
        JsonObject json = Json.parse(jsonStr).asObject();
        Graph graph = loadGraph(json, jsonStr);
        _workflow.clear(graph);
        _workflow.setLoading(true);
        Map<String, Vertex> vertices = loadVertices(json.get("vertices").asArray());
        loadConnectors(json.get("connectors").asArray(), vertices);
        _workflow.setLoading(false);
        EventLogger.captureEvents();
        selectHeadVertex(vertices, graph);
    }


    private Graph loadGraph(JsonObject json, String content) {

        // check if this one is persisted
        String id = json.getString("id", "-1");
        Optional<Graph> optional = GraphStore.get(id);
        if (optional.isPresent()) {
            Graph graph = optional.get();
            graph.refreshOptions();
            return graph;
        }

        // unknown to this deployment
        Graph.Builder builder = new Graph.Builder(json.getString("creator", ""))
                .id(id)
                .name(json.getString("name", ""))
                .owner(json.getString("owner", ""))
                .creationTime(strToDateTime(json.getString("creationTime", "0")))
                .userContent(content);

        String description = json.getString("description", "");
        if (StringUtils.isNotEmpty(description)) {
            builder.description(description);
        }

        String lastSaved = json.getString("lastSavedTime", "");
        if (StringUtils.isNotEmpty(lastSaved)) {
            builder.lastSavedTime(strToDateTime(lastSaved));
        }
        
        return builder.build();
    }


    private Map<String, Vertex> loadVertices(JsonArray array) throws IOException {
        Map<String, Vertex> vertexMap = new HashMap<>();
        NodeLoader nodeLoader = new NodeLoader();
        for (int i=0; i < array.size(); i++) {
            JsonObject json = array.get(i).asObject();
            double x = json.getDouble("x", 0);
            double y = json.getDouble("y", 0);
            Node node = nodeLoader.fromJson(json.get("node").asObject());
            if (node != null) {
                Vertex vertex = new Vertex(x, y, node);
                _workflow.addVertex(vertex);
                if (node.hasCompleted()) {
                    vertex.setRunState(VertexStateIndicator.State.COMPLETED);
                }
                vertexMap.put(vertex.getID(), vertex);
            }
        }
        return vertexMap;
    }


    private void loadConnectors(JsonArray array, Map<String, Vertex> vertices) {
        for (int i=0; i < array.size(); i++) {
            JsonObject json = array.get(i).asObject();
            String sourceID = json.getString("source", "-1");
            String targetID = json.getString("target", "-1");
            Vertex source = vertices.get(sourceID);
            Vertex target = vertices.get(targetID);
            if (! (source == null || target == null)) {
                Port sourcePort = source.getOutputPort();
                Port targetPort = target.getInputPort();
                _workflow.addConnector(new Connector(sourcePort, targetPort));
            }
        }
    }


    private void selectHeadVertex(Map<String, Vertex> vertices, Graph graph) {
        if (! vertices.isEmpty()) {
            Set<Node> heads = graph.getHeads();
            if (! heads.isEmpty()) {
                _workflow.setSelectedNode(heads.iterator().next());       // set any head
            }
        }
    }


    private LocalDateTime strToDateTime(String s) {
        return LocalDateTime.parse(s, EventLogger.dtFormatter);
    }

}
