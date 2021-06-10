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
 * @date 9/6/21
 */
public class VertexStateIndicator {

    public enum State {

        DORMANT ("#C54E57"),          // dull red
        RUNNING ("#EEDC5B"),          // dull yellow
        COMPLETED ("#749f64");        // dull green

        String _colour;

        State(String colour) {_colour = colour; }

        String colour() { return _colour; }
    }


    private static final double RADIUS = 5;
    private static final double LINE_WIDTH = 1;
    private static final double X_INSET = 12;
    private static final double Y_INSET = 12;

    private State state;

    public VertexStateIndicator() { state = State.DORMANT; }


    public void setState(State s) { state = s; }


    public void render(double vx, double vy, Context2D ctx) {
        double x = vx + Vertex.WIDTH - X_INSET;
        double y = vy + Vertex.HEIGHT - Y_INSET;

        ctx.lineWidth(LINE_WIDTH);
        ctx.strokeStyle("gray");
        ctx.fillStyle(state.colour());
        ctx.beginPath();
        ctx.arc(x, y, RADIUS, 0, Math.PI * 2, false);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }
}
