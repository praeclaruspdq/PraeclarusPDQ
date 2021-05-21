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

    private double x;
    private double y;
    private String label;
    private final Node node;
    private Port _inPort;
    private Port _outPort;

    public Vertex(double x, double y, Node node) {
        this.x = x;
        this.y = y;
        this.label = node.getName();
        this.node = node;
        double halfHeight = HEIGHT / 2;
        if (node.allowsInput()) {
            _inPort = new Port(this, x, y + halfHeight, Port.Style.INPUT);
        }
        if (node.allowsOutput()) {
            _outPort = new Port(this, x + WIDTH, y + halfHeight, Port.Style.OUTPUT);
        }
    }

    
    public void setLabel(String label) { this.label = label; }

    public String getName() { return node.getName(); }


    public Node getNode() { return node; }

    
    public boolean contains(double pX, double pY) {
        return pX > x && pX < x + WIDTH && pY > y && pY < y + HEIGHT;
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


    public void render(Context2D ctx, CanvasPrimitive selected) {
        String colour = this.equals(selected) ? "blue" : "gray";
        ctx.beginPath();
        ctx.strokeStyle(colour);
        ctx.lineWidth(1);
        ctx.rect(x, y, WIDTH, HEIGHT);
        ctx.stroke();

        renderPorts(ctx);
        renderLabel(ctx);
    }


    private void renderPorts(Context2D ctx) {
        if (_inPort != null) {
            _inPort.render(ctx, null);
        }
        if (_outPort != null) {
            _outPort.render(ctx, null);
        }
    }


    private void renderLabel(Context2D ctx) {
        double innerX = x + 10;
        double innerY = y + 20;

        ctx.beginPath();
        ctx.font("16px Arial");
//        ctx.fillStyle("black");
        for (String word : label.split(" ")) {
            ctx.fillText(word, innerX, innerY, WIDTH - 20);
            innerY+=30;
        }
        ctx.stroke();
    }

}
