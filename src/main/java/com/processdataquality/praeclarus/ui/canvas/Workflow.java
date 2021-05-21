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

import com.processdataquality.praeclarus.ui.component.PipelinePanel;
import com.processdataquality.praeclarus.workspace.node.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 19/5/21
 */
public class Workflow implements CanvasEventListener {

    private final PipelinePanel _parent;
    private final Context2D _ctx;
    private final Set<Vertex> _vertices = new HashSet<>();
    private final Set<Connector> _connectors = new HashSet<>();

    private ActiveLine activeLine;
    private CanvasPrimitive selected;


    public Workflow(PipelinePanel parent, Context2D context) {
        _parent = parent;
        _ctx = context;
    }

    @Override
    public void mouseDown(double x, double y) {
        activeLine = new ActiveLine(x, y);
    }

    @Override
    public void mouseMove(double x, double y) {
        render();
        activeLine.lineTo(_ctx, x, y);
    }

    @Override
    public void mouseUp(double x, double y) {
        Point start = activeLine.getStart();
        Port source = getPortAt(start.x, start.y);
        Port target = getPortAt(x, y);
        if (source != null && target != null) {
            _connectors.add(new Connector(source, target));
        }
        render();
    }

    @Override
    public void mouseClick(double x, double y) {
        selected = setSelected(x, y);
        render();
        Node selectedNode = (selected instanceof Vertex) ? ((Vertex) selected).getNode() : null;
        _parent.showPluginProperties(selectedNode);
    }


    public CanvasPrimitive getSelected() { return selected; }


    public void removeSelected() {
        if (selected instanceof Vertex) {
            removeVertex((Vertex) selected);
        }
        else if (selected instanceof Connector) {
            removeConnector((Connector) selected);
        }
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
        removeVertex(vertex);
        return vertex;
    }


    public void removeVertex(Vertex vertex) {
        if (vertex != null) {
            _vertices.remove(vertex);
            removeConnectors(vertex);
            render();
        }
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


    private Port getPortAt(double x, double y) {
        for (Vertex vertex : _vertices) {
            Port port = vertex.getPortAt(x, y);
             if (port != null) {
                 return port;
             }
        }
        return null;
    }


    private void render() {
        _ctx.clear();
        for (Vertex vertex : _vertices) {
            vertex.render(_ctx, selected);
        }
        for (Connector connector : _connectors) {
            connector.render(_ctx, selected);
        }
    }


    private CanvasPrimitive setSelected(double x, double y) {
        for (Vertex vertex : _vertices) {
            if (vertex.contains(x, y)) {
                return vertex;
            }
        }
        for (Connector connector : _connectors) {
            if (connector.contains(x, y)) {
                return connector;
            }
        }
        return null;
    }

}
