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
 * Renders an indicator on a Vertex of its current state
 *
 * @author Michael Adams
 * @date 9/6/21
 */
public class VertexStateIndicator {

    // an enum of available states and their associated colours
    public enum State {

        DORMANT ("#C54E57"),          // dull red
        RUNNING ("#EEDC5B"),          // dull yellow
        PAUSED ("#49759C"),           // dull blue
        COMPLETED ("#749f64");        // dull green

        String _colour;

        State(String colour) {_colour = colour; }

        String colour() { return _colour; }
    }


    // some constants on where and how to render the shape of the indicator
    private static final double RADIUS = 5;
    private static final double LINE_WIDTH = 1;
    private static final double X_INSET = 12;
    private static final double Y_INSET = 12;

    private State state;                         // the current state of this indicator

    // the initial state is 'dormant'
    public VertexStateIndicator() { state = State.DORMANT; }


    /**
     * Sets the current state of this indicator
     * @param s the state
     */
    public void setState(State s) { state = s; }


    /**
     * Renders this indicator (as a circle in bottom right of a Vertex)
     * @param vx the Vertex's x-coord
     * @param vy the Vertex's y-coord
     * @param ctx the graphics context
     */
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
