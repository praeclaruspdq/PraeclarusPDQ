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

import com.processdataquality.praeclarus.workspace.node.Node;

/**
 * @author Michael Adams
 * @date 19/5/21
 */
public class Vertex implements CanvasPrimitive {

    public static final double WIDTH = 100;
    public static final double HEIGHT = 80;

    private double _x;
    private double _y;
    private String _label;
    private final Node _node;
    private Port _inPort;
    private Port _outPort;
    private Point _dragOffset;

    public Vertex(double x, double y, Node node) {
        _x = x;
        _y = y;
        this._label = node.getName();
        this._node = node;
        if (node.allowsInput()) {
            _inPort = new Port(this, Port.Style.INPUT);
        }
        if (node.allowsOutput()) {
            _outPort = new Port(this, Port.Style.OUTPUT);
        }
    }


    public double x() { return _x; }

    public double y() { return  _y; }

    public void setLabel(String label) { _label = label; }

    public String getName() { return _node.getName(); }


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
        ctx.beginPath();
        ctx.strokeStyle(colour);
        ctx.lineWidth(1);
        ctx.rect(_x, _y, WIDTH, HEIGHT);
        ctx.stroke();

        renderPorts(ctx, selected);
        renderLabel(ctx, colour);
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

        ctx.beginPath();
        ctx.font("14px Arial");
        ctx.fillStyle(colour);
        for (String word : _label.split(" ")) {
            ctx.fillText(word, innerX, innerY, WIDTH - 20);
            innerY+=16;
        }
        ctx.stroke();
    }

}
