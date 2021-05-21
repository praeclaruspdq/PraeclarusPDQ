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
    private final double x;     // origin
    private final double y;     // origin

    public Port(Vertex v, double x, double y, Style style) {
        parent = v;
        this.x = x;
        this.y = y;
        this.style = style;
    }

    public double x() { return x; }

    public double y() { return y; }

    public Vertex getParent() { return parent; }

    public Style getStyle() { return style; }

    public Point getConnectPoint() {
        double px = style == Style.INPUT ? x - RADIUS : x + RADIUS;
        return new Point(px, y);
    }


    public void render(Context2D ctx, CanvasPrimitive selected) {
        double rotation = Math.PI / 2;
        double startAngle = (style == Style.INPUT ? 0 : Math.PI) + rotation;
        double endAngle = (style == Style.INPUT ? Math.PI  : Math.PI * 2) + rotation;

        ctx.beginPath();
        ctx.strokeStyle("gray");
        ctx.fillStyle("gray");
        ctx.arc(x, y, RADIUS, startAngle, endAngle, false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }


    public boolean contains(double pX, double pY) {
        double dx = pX - x;
        double dy = pY - y;
        return dx*dx + dy*dy < RADIUS * RADIUS;
    }
}
