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


import com.eclipsesource.json.JsonObject;

/**
 * A directed arc between two nodes. Also handles rendering
 *
 * @author Michael Adams
 * @date 19/5/21
 */
public class Connector implements CanvasPrimitive {

    // some static values to configure or select a line
    private static final double HEAD_SIZE = 8;               // size of arrow head
    private static final double PROXIMITY_TOLERANCE = 3;     // i.e. 3 pixels either side
    public static final double WIDTH = 2;                    // line width
    public static final String COLOUR = "black";

    // each port is owned by either the source or target vertex
    private final Port _source;
    private final Port _target;


    // A connection is between two ports
    public Connector(Port source, Port target) {
        _source = source;
        _target = target;
    }


    /**
     * @return the source Vertex
     */
    public Vertex getSource() {
        return _source.getVertex();
    }


    /**
     * @return the target Vertex
     */
    public Vertex getTarget() {
        return _target.getVertex();
    }


    /**
     * @param vertex the Vertex to test
     * @return true if this line is connected to the specified Vertex
     */
    public boolean connects(Vertex vertex) {
        return getSource().equals(vertex) || getTarget().equals(vertex);
    }


    /**
     * Tests whether a point is on or proximate to this line
     * @param x x-coord
     * @param y y-coord
     * @return true if the point is on or proximate (i.e under a threshold) to this line
     */
    public boolean contains(double x, double y) {
        Point ps = _source.getConnectPoint();
        Point pt = _target.getConnectPoint();
        Point pb = new Point(x, y);
        double diff = distance(ps, pb) + distance(pb, pt) - distance(ps, pt);
        return diff < PROXIMITY_TOLERANCE;
    }


    /**
     * @return a JSON representation of this connector
     */
    public JsonObject asJson() {
        JsonObject json = new JsonObject();
        json.add("source", getSource().getID());
        json.add("target", getTarget().getID());
        return json;
    }


    /**
     * @param a a point describing the start of a line
     * @param b a point describing the end of a line
     * @return the distance between point a and point b
     */
    private double distance(Point a, Point b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }


    /**
     * Renders this connector on the canvas
     * @param ctx the graphics context
     * @param selected the currently selected canvas primitive
     */
    public void render(Context2D ctx, CanvasPrimitive selected) {
        double width = this.equals(selected) ? WIDTH * 2 : WIDTH;
        Point ps = _source.getConnectPoint();
        Point pt = _target.getConnectPoint();
        
        ctx.beginPath();
        ctx.strokeStyle(COLOUR);
        ctx.lineWidth(width);
        ctx.moveTo(ps.x, ps.y);
        ctx.lineTo(pt.x, pt.y);
        renderHead(ctx, ps, pt);
        ctx.stroke();
    }


    /**
     * Renders the 'arrow' head for this connector on the canvas
     * @param ctx the graphics context
     * @param ps the start of the connector line
     * @param pt the end of the connector line
     */
    private void renderHead(Context2D ctx, Point ps, Point pt) {
        double d = Math.max(1.0, distance(ps, pt));
        double ax = -(HEAD_SIZE * (pt.x - ps.x) / d);
        double ay = -(HEAD_SIZE * (pt.y - _source.y()) / d);

        ctx.moveTo(pt.x, pt.y);
        ctx.lineTo(pt.x + ax + ay / 2, pt.y + ay - ax / 2);
        ctx.lineTo(pt.x + ax - ay / 2, pt.y + ay + ax / 2);
        ctx.closePath();
        ctx.fillStyle(COLOUR);
        ctx.fill();
    }


//    private double distance(Point ps, Point pt) {
//        double dx = ps.x - pt.x;
//        double dy = ps.y - pt.y;
//        return Math.sqrt(dx * dx + dy * dy);
//    }
}
