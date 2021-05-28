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
public class Connector implements CanvasPrimitive {

    private static final double HEAD_SIZE = 8;
    private static final double PROXIMITY_TOLERANCE = 3;
    public static final double WIDTH = 3;
    public static final String COLOUR = "black";


    private final Port _source;
    private final Port _target;

    public Connector(Port source, Port target) {
        _source = source;
        _target = target;
    }

    public Vertex getSource() {
        return _source.getParent();
    }

    public Vertex getTarget() {
        return _target.getParent();
    }

    public boolean connects(Vertex vertex) {
        return getSource().equals(vertex) || getTarget().equals(vertex);
    }

    public boolean contains(double x, double y) {
        Point ps = _source.getConnectPoint();
        Point pt = _target.getConnectPoint();
        Point pb = new Point(x, y);
        double diff = getDistance(ps, pb) + getDistance(pb, pt) - getDistance(ps, pt);
        return diff < PROXIMITY_TOLERANCE;
    }


    private double getDistance(Point a, Point b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }


    public void render(Context2D ctx, CanvasPrimitive selected) {
        String colour = this.equals(selected) ? "blue" : COLOUR;
        Point ps = _source.getConnectPoint();
        Point pt = _target.getConnectPoint();
        
        double x1 = (pt.x - ps.x) / 2;
        double dy = Math.abs(pt.y - ps.y);
        double y1 = ps.y - (2 * dy);
        double y2 = pt.y + (2 * dy);
        ctx.beginPath();
        ctx.strokeStyle(colour);
        ctx.lineWidth(WIDTH);
        ctx.moveTo(ps.x, ps.y);
        ctx.lineTo(pt.x, pt.y);
//        ctx.bezierCurveTo(x1, y1, x1, y2, pt.x, pt.y);
        renderHead(ctx, ps, pt);
        ctx.stroke();
    }


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


    private double distance(Point ps, Point pt) {
        double dx = ps.x - pt.x; 
        double dy = ps.y - pt.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
