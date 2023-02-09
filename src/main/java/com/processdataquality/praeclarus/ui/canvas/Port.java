/*
 * Copyright (c) 2021-2022 Queensland University of Technology
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
 * A connection point for a Vertex on the canvas
 * 
 * @author Michael Adams
 * @date 19/5/21
 */
public class Port implements CanvasPrimitive {

    public enum Style {INPUT, OUTPUT}

    public static final double RADIUS = 6;                         // rendered size

    private final Vertex _vertex;                                   // this port's owner
    private final Style _style;                                     // input or output


    public Port(Vertex vertex, Style style) {
        _vertex = vertex;
        _style = style;
    }


    /**
     * @return the x-coord of this port's origin
     */
    public double x() {
        double px = _vertex.x();
        return isInput() ? px : px + Vertex.WIDTH;
    }


    /**
      * @return the y-coord of this port's origin
     */
    public double y() {
        return _vertex.y() + Vertex.HEIGHT / 2 ;
    }


    /**
     * @return the Vertex that 'owns' this port
     */
    public Vertex getVertex() { return _vertex; }


    /**
     * @return true is this port is an input port
     */
    public boolean isInput() { return _style == Style.INPUT; }


    /**
     * @return true is this port is an output port
     */
    public boolean isOutput() { return _style == Style.OUTPUT; }


    /**
     * @return the point where a connector (arc) may attach to this port
     */
    public Point getConnectPoint() {
        double px = isInput() ? x() - RADIUS : x() + RADIUS;
        return new Point(px, y());
    }


    /**
     * Checks whether a point is within the area of this port
     * @param pX x-coord
     * @param pY y-coord
     * @return true if point is within this port
     */
    public boolean contains(double pX, double pY) {
        double dx = pX - x();
        double dy = pY - y();
        return dx*dx + dy*dy < RADIUS * RADIUS;
    }


    /**
     * Renders this port on the canvas (as a filled semi-circle)
     * @param ctx the graphics context
     * @param selected the currently selected object on the canvas
     */
    public void render(Context2D ctx, CanvasPrimitive selected) {
        String colour = "black";
        double rotation = Math.PI / 2;
        double startAngle = (isInput() ? 0 : Math.PI) + rotation;
        double endAngle = (isInput() ? Math.PI : Math.PI * 2) + rotation;

        ctx.beginPath();
        ctx.strokeStyle(colour);
        ctx.fillStyle(colour);
        ctx.arc(x(), y(), RADIUS, startAngle, endAngle, false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }

}
