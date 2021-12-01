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

package com.processdataquality.praeclarus.ui.canvas;

import com.processdataquality.praeclarus.node.NodeUtil;
import com.processdataquality.praeclarus.node.Node;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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


    public void load(String jsonStr) throws JSONException, IOException {
       _workflow.clear();
       _workflow.setLoading(true);
        NodeUtil nodeUtil = new NodeUtil();
        JSONObject json = new JSONObject(jsonStr);
        loadOptions(json);
        Map<Integer, Vertex> vertices = loadVertices(json.getJSONArray("vertices"), nodeUtil);
        loadConnectors(json.getJSONArray("connectors"), vertices);
        _workflow.setLoading(false);
        selectHeadVertex(vertices, nodeUtil);
    }


    private void loadOptions(JSONObject json) throws JSONException {
        _workflow.setName(json.getString("name"));
        _workflow.setId(json.getString("id"));
    }


    private Map<Integer, Vertex> loadVertices(JSONArray array, NodeUtil nodeUtil)
            throws JSONException, IOException {
        Map<Integer, Vertex> vertexMap = new HashMap<>();
        for (int i=0; i < array.length(); i++) {
            JSONObject json = array.getJSONObject(i);
            int id = json.getInt("id");
            double x = json.getDouble("x");
            double y = json.getDouble("y");
            String label = json.getString("label");
            Node node = nodeUtil.fromJson(json.getJSONObject("node"));
            if (node != null) {
                Vertex vertex = new Vertex(x, y, node, id);
                vertex.setLabel(label);
                _workflow.addVertex(vertex);
                if (node.hasCompleted()) {
                    vertex.setRunState(VertexStateIndicator.State.COMPLETED);
                }
                vertexMap.put(id, vertex);
            }
        }
        return vertexMap;
    }


    private void loadConnectors(JSONArray array, Map<Integer, Vertex> vertices)
            throws JSONException {
        for (int i=0; i < array.length(); i++) {
            JSONObject json = array.getJSONObject(i);
            int sourceID = json.getInt("source");
            int targetID = json.getInt("target");
            Vertex source = vertices.get(sourceID);
            Vertex target = vertices.get(targetID);
            if (! (source == null || target == null)) {
                Port sourcePort = source.getOutputPort();
                Port targetPort = target.getInputPort();
                _workflow.addConnector(new Connector(sourcePort, targetPort));
            }
        }
    }


    private void selectHeadVertex(Map<Integer, Vertex> vertices, NodeUtil nodeUtil) {
        if (! vertices.isEmpty()) {
            Vertex anyVertex = vertices.values().iterator().next();       // get any vertex
            Set<Node> heads = nodeUtil.getHeads(anyVertex.getNode());
            if (! heads.isEmpty()) {
                _workflow.setSelectedNode(heads.iterator().next());       // set any head
            }
        }
    }

}
