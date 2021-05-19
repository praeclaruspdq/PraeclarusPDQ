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

import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 19/5/21
 */
public class Vertex {

    public static final double WIDTH = 100;
    public static final double HEIGHT = 80;

    private double x;
    private double y;
    private String label;
    private Node node;
    private boolean selected;
    private final Set<Port> _ports = new HashSet<>();

    public Vertex(double x, double y, Node node) {
        this.x = x;
        this.y = y;
        this.label = node.getName();
        this.node = node;
        double halfHeight = HEIGHT / 2;
        _ports.add(new Port(this, x, y + halfHeight));
        _ports.add(new Port(this, x + WIDTH, y + halfHeight));
    }

    
    public void setSelected(boolean b) { selected = b; }

    public void setLabel(String label) { this.label = label; }

    public String getName() { return node.getName(); }
    

    public void render(Context2D ctx) {
        ctx.lineWidth(2);
        ctx.beginPath();
        ctx.rect(x, y, WIDTH, HEIGHT);
        ctx.stroke();

        renderPorts(ctx);
        renderLabel(ctx);
    }


    private void renderPorts(Context2D ctx) {
        for (Port port : _ports) {
            port.render(ctx);
        }
    }


    private void renderLabel(Context2D ctx) {
        double innerX = x + 10;
        double innerY = y + 20;

        ctx.font("16px Arial");
        for (String word : label.split(" ")) {
            ctx.fillText(word, innerX, innerY, WIDTH - 20);
            innerY+=30;
        }
    }

}
