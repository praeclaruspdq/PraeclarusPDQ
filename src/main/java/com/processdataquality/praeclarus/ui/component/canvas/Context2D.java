package com.processdataquality.praeclarus.ui.component.canvas;

import java.io.Serializable;

/**
 * Drawing context for a HTML5 canvas. Heavily based on the org.vaadin.pekkam canvas addon.
 * @author Michael Adams
 * @date 18/5/21
 */
public class Context2D {

    private final Canvas _canvas;

    public Context2D(Canvas canvas) {
        _canvas = canvas;
    }

    protected void callJsMethod(String methodName, Serializable... parameters) {
        _canvas.getElement().callJsFunction("getContext('2d')." + methodName,
                parameters);
    }
}
