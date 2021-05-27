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

/**
 * @author Michael Adams
 * @date 19/5/21
 */
public class Port implements CanvasPrimitive {

    public enum Style {INPUT, OUTPUT}

    public static final double RADIUS = 6;

    private final Vertex parent;
    private final Style style;


    public Port(Vertex v, Style style) {
        parent = v;
        this.style = style;
    }

    public double x() {
        double px = parent.x();
        return style == Style.INPUT ? px : px + Vertex.WIDTH;
    }

    public double y() {
        return parent.y() + Vertex.HEIGHT / 2 ;
    }

    public Vertex getParent() { return parent; }

    public Style getStyle() { return style; }

    public boolean isInput() { return style == Style.INPUT; }

    public boolean isOutput() { return style == Style.OUTPUT; }

    public Point getConnectPoint() {
        double px = style == Style.INPUT ? x() - RADIUS : x() + RADIUS;
        return new Point(px, y());
    }


    public void render(Context2D ctx, CanvasPrimitive selected) {
        String colour = parent.equals(selected) ? "blue" : "gray";
        double rotation = Math.PI / 2;
        double startAngle = (style == Style.INPUT ? 0 : Math.PI) + rotation;
        double endAngle = (style == Style.INPUT ? Math.PI  : Math.PI * 2) + rotation;

        ctx.beginPath();
        ctx.strokeStyle(colour);
        ctx.fillStyle(colour);
        ctx.arc(x(), y(), RADIUS, startAngle, endAngle, false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }


    public boolean contains(double pX, double pY) {
        double dx = pX - x();
        double dy = pY - y();
        return dx*dx + dy*dy < RADIUS * RADIUS;
    }
}
