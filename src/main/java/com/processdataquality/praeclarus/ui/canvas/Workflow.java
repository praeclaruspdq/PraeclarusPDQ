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

import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 19/5/21
 */
public class Workflow {

    private final Context2D _ctx;
    private final Set<Vertex> _vertices = new HashSet<>();
    private final Set<Connector> _connectors = new HashSet<>();


    public Workflow(Context2D context) {
        _ctx = context;
    }

    public void addVertex(Vertex v) {
        _vertices.add(v);
        render();
    }


    public Vertex getVertex(String name) {
        for (Vertex vertex : _vertices) {
            if (vertex.getName().equals(name)) {
                return vertex;
            }
        }
        return null;
    }


    public Vertex removeVertex(String name) {
        Vertex vertex = getVertex(name);
        if (vertex != null) {
            _vertices.remove(vertex);
            removeConnectors(vertex);
            render();
        }
        return vertex;
    }


    public void addConnector(Connector c) {
        _connectors.add(c);
        render();
    }

    public boolean removeConnector(Connector c) {
        boolean success = _connectors.remove(c);
        if (success) render();
        return success;
    }

    private void removeConnectors(Vertex vertex) {
        Set<Connector> removeSet = new HashSet<>();
        for (Connector c : _connectors) {
            if (c.connects(vertex)) {
                removeSet.add(c);
            }
        }
        _connectors.removeAll(removeSet);
        render();
    }


    private void render() {
        _ctx.clear();
        for (Vertex vertex : _vertices) {
            vertex.render(_ctx);
        }
        for (Connector connector : _connectors) {
            connector.render(_ctx);
        }
    }
}
