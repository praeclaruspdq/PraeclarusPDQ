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

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.workspace.Workspace;
import com.processdataquality.praeclarus.workspace.node.Node;
import com.processdataquality.praeclarus.workspace.node.NodeFactory;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 2/6/21
 */
public class WorkflowLoader {

    private final Workflow _workflow;      // frontend
    private final Workspace _workspace;    // backend

    public WorkflowLoader(Workflow workflow, Workspace workspace) {
        _workflow = workflow;
        _workspace = workspace;
    }


    public void load(String jsonStr) throws JSONException {
       _workflow.clear();
       _workspace.clear();
       _workflow.setLoading(true);
        JSONObject json = new JSONObject(jsonStr);
        Map<Integer, Vertex> vertices = loadVertices(json.getJSONArray("vertices"));
        loadConnectors(json.getJSONArray("connectors"), vertices);
        _workflow.setLoading(false);
        selectHeadVertex(vertices);
    }


    private Map<Integer, Vertex> loadVertices(JSONArray array) throws JSONException {
        Map<Integer, Vertex> vertexMap = new HashMap<>();
        for (int i=0; i < array.length(); i++) {
            JSONObject json = array.getJSONObject(i);
            int id = json.getInt("id");
            double x = json.getDouble("x");
            double y = json.getDouble("y");
            String label = json.getString("label");
            PDQPlugin plugin = newPluginInstance(json.getString("plugin"));
            if (plugin != null) {
                addOptions(plugin, json.getJSONObject("options"));
                Node node = NodeFactory.create(plugin);
                _workspace.addNode(node);
                Vertex vertex = new Vertex(x, y, node, id);
                vertex.setLabel(label);
                _workflow.addVertex(vertex);
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


    private void selectHeadVertex(Map<Integer, Vertex> vertices) {
        if (! vertices.isEmpty()) {
            Vertex anyVertex = vertices.values().iterator().next();       // get any vertex
            Set<Node> heads = _workspace.getHeads(anyVertex.getNode());
            if (! heads.isEmpty()) {
                _workflow.setSelectedNode(heads.iterator().next());       // set any head
            }
        }
    }

}
