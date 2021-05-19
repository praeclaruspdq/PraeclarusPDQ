package com.processdataquality.praeclarus.ui.component.canvas;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;

/**
 * Wrapper for a HTML5 canvas. Heavily based on the org.vaadin.pekkam canvas addon.
 * @author Michael Adams
 * @date 18/5/21
 */
@Tag("canvas")
public class Canvas extends Component implements HasStyle, HasSize {

    private final Context2D _ctx;

    /**
     * Creates a new canvas with the given coordinate range
     * @param width pixel width of the canvas
     * @param height pixel height of the canvas
     */
    public Canvas(int width, int height) {
        _ctx = new Context2D(this);

        getElement().setAttribute("width", String.valueOf(width));
        getElement().setAttribute("height", String.valueOf(height));
    }


    @Override
    public void setWidth(String width) {
        HasSize.super.setWidth(width);
    }


    @Override
    public void setHeight(String height) {
        HasSize.super.setHeight(height);
    }


    @Override
    public void setSizeFull() {
        HasSize.super.setSizeFull();
    }
}
