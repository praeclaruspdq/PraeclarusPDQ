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
import com.processdataquality.praeclarus.workspace.node.Node;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Michael Adams
 * @date 19/5/21
 */
public class Vertex implements CanvasPrimitive {

    public static final double WIDTH = 100;
    public static final double HEIGHT = 80;
    public static final double CORNER_RADIUS = 10;
    public static final double LINE_WIDTH = 1;

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

    private final VertexStateIndicator _indicator = new VertexStateIndicator();
    private final int _id;
    private final Node _node;

    private double _x;
    private double _y;
    private String _label;
    private Port _inPort;
    private Port _outPort;
    private Point _dragOffset;


    public Vertex(double x, double y, Node node) {
        this(x, y, node, ID_GENERATOR.incrementAndGet());
    }


    public Vertex(double x, double y, Node node, int id) {
        _x = x;
        _y = y;
        _id = id;
        _label = node.getName();
        _node = node;
        if (node.allowsInput()) {
            _inPort = new Port(this, Port.Style.INPUT);
        }
        if (node.allowsOutput()) {
            _outPort = new Port(this, Port.Style.OUTPUT);
        }
    }


    public double x() { return _x; }

    public double y() { return  _y; }

    public int getID() { return _id; }
    

    public void setLabel(String label) { _label = label; }

    public String getLabel() { return _label; }


    public Node getNode() { return _node; }

    
    public boolean contains(double pX, double pY) {
        return pX > _x && pX < _x + WIDTH && pY > _y && pY < _y + HEIGHT;
    }


    public Port getPortAt(double x, double y) {
        if (_inPort != null && _inPort.contains(x, y)) {
            return _inPort;
        }
        if (_outPort != null && _outPort.contains(x, y)) {
            return _outPort;
        }
        return null;
    }


    public Port getInputPort() { return _inPort; }

    public Port getOutputPort() { return _outPort; }


    public void setRunState(VertexStateIndicator.State state) {
        _indicator.setState(state);
    }


    public JSONObject asJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", _id);
        json.put("x", _x);
        json.put("y", _y);
        json.put("label", _label);

        PDQPlugin plugin = _node.getPlugin();
        json.put("plugin", plugin.getClass().getName());
        json.put("options", plugin.getOptions().getChangesAsJson());
        return json;
    }

    public void setDragOffset(double x, double y) {
        double dx = x - _x;
        double dy = y - _y;
        _dragOffset = new Point(dx, dy);
    }


    public void moveTo(double x, double y) {
        _x = x - _dragOffset.x;
        _y = y - _dragOffset.y;
    }

    
    public void render(Context2D ctx, CanvasPrimitive selected) {
        String colour = this.equals(selected) ? "blue" : "gray";
        ctx.strokeStyle("black");
        ctx.lineWidth(this.equals(selected) ? LINE_WIDTH * 3 : LINE_WIDTH);
        ctx.beginPath();
        renderVertex(ctx);
        if (this.equals(selected)) {
            ctx.fillStyle("#D0D0D0");
            ctx.fill();
        }
        ctx.stroke();
        _indicator.render(x(), y(), ctx);

        renderPorts(ctx, selected);
        renderLabel(ctx, colour);
    }


    private void renderVertex(Context2D ctx) {
        ctx.moveTo(_x + CORNER_RADIUS, _y);
        ctx.lineTo(_x + WIDTH - CORNER_RADIUS, _y);
        ctx.quadraticCurveTo(_x + WIDTH, _y, _x + WIDTH, _y + CORNER_RADIUS);
        ctx.lineTo(_x + WIDTH, _y + HEIGHT - CORNER_RADIUS);
        ctx.quadraticCurveTo(_x + WIDTH, _y + HEIGHT, _x + WIDTH - CORNER_RADIUS, _y + HEIGHT);
        ctx.lineTo(_x + CORNER_RADIUS, _y + HEIGHT);
        ctx.quadraticCurveTo(_x, _y + HEIGHT, _x, _y + HEIGHT - CORNER_RADIUS);
        ctx.lineTo(_x, _y + CORNER_RADIUS);
        ctx.quadraticCurveTo(_x, _y, _x + CORNER_RADIUS, _y);
        ctx.closePath();
    }


    private void renderPorts(Context2D ctx, CanvasPrimitive selected) {
        if (_inPort != null) {
            _inPort.render(ctx, selected);
        }
        if (_outPort != null) {
            _outPort.render(ctx, selected);
        }
    }


    private void renderLabel(Context2D ctx, String colour) {
        double innerX = _x + 10;
        double innerY = _y + 20;
        int fontSize = 14;
        double lineHeight=fontSize*1.286;
        double maxWidth = WIDTH - 20;
        String line = "";

        ctx.textBaseline("top");
        ctx.font("14px Arial");
        ctx.beginPath();
        ctx.fillStyle(colour);
        for (String word : _label.split(" ")) {
//            String temp = line + word + " ";
////            double metrics = ctx.measureText(temp);
//            double tempWidth = 90; //metrics.width;
//            if (tempWidth > maxWidth) {
//                ctx.fillText(line, innerX, innerY);
//                line = word + " ";
//                innerY += lineHeight;
//            }
//            else {
//                line = temp;
//            }
//        }
            ctx.fillText(word, innerX, innerY, WIDTH - 20);
            innerY+=lineHeight;
        }
        ctx.stroke();
    }

}
