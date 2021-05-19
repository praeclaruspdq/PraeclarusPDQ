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

package com.processdataquality.praeclarus.ui.component;


import com.processdataquality.praeclarus.ui.component.canvas.Canvas;
import com.processdataquality.praeclarus.ui.component.canvas.Context2D;

/**
 * @author Michael Adams
 * @date 16/4/21
 */
public class PipelineCanvas extends Canvas {

    public PipelineCanvas() {
        this(800,300);
    }

    public PipelineCanvas(int w, int h) {
        super(w, h);
        setHeight("50%");
        setWidth("50%");

        getElement().addEventListener("mousedown", e -> {
           drawLine(50, 50,
                   (int) e.getEventData().getNumber("x"), (int) e.getEventData().getNumber("y"));
        });
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        Context2D ctx = getContext();
//        ctx.setStrokeStyle("red");
        ctx.beginPath();
        ctx.moveTo(x1, y1);
        ctx.lineTo(x2, y2);
        ctx.closePath();
        ctx.stroke();
    }


}
