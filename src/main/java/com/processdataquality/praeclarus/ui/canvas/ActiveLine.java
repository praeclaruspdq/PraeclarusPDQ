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
 * Draws a line that is currently being defined by user mouse drags
 *
 * @author Michael Adams
 * @date 21/5/21
 */
public class ActiveLine implements CanvasPrimitive {

    private final Point start;                           // fixed line start
    private double toX, toY;                             // dynamic line end coords

    // start a new active line
    protected ActiveLine(double x, double y) {
        start = new Point(x, y);
    }

    /**
     * Update the end of line coords, and render the new line
     * @param ctx the graphics context
     * @param x x-coord
     * @param y y-coord
     */
    public void lineTo(Context2D ctx, double x, double y) {
        toX = x;
        toY = y;
        render(ctx, null);
    }


    /**
     * @return the starting point of this line
     */
    public Point getStart() { return start; }


    /**
     * Renders the line on the canvas
     * @param ctx the graphics context
     * @param selected always null for an active line
     */
    @Override
    public void render(Context2D ctx, CanvasPrimitive selected) {
        ctx.beginPath();
        ctx.lineWidth(Connector.WIDTH);
        ctx.strokeStyle(Connector.COLOUR);
        ctx.moveTo(start.x, start.y);
        ctx.lineTo(toX, toY);
        ctx.stroke();
    }
}
