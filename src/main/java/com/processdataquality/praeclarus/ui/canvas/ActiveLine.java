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
 * @date 21/5/21
 */
public class ActiveLine implements CanvasPrimitive {

    private final Point start;
    private double toX, toY;

    protected ActiveLine(double x, double y) {
        start = new Point(x, y);
    }

    public void lineTo(Context2D ctx, double x, double y) {
        toX = x;
        toY = y;
        render(ctx, null);
    }

    public Point getStart() { return start; }

    public void render(Context2D ctx, CanvasPrimitive selected) {
        ctx.beginPath();
        ctx.lineWidth(Connector.WIDTH);
        ctx.strokeStyle(Connector.COLOUR);
        ctx.moveTo(start.x, start.y);
        ctx.lineTo(toX, toY);
        ctx.stroke();
    }
}
