package com.processdataquality.praeclarus.ui.component;

import org.vaadin.pekkam.Canvas;
import org.vaadin.pekkam.CanvasRenderingContext2D;

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
        CanvasRenderingContext2D ctx = getContext();
        ctx.setStrokeStyle("red");
        ctx.beginPath();
        ctx.moveTo(x1, y1);
        ctx.lineTo(x2, y2);
        ctx.closePath();
        ctx.stroke();
    }


}
