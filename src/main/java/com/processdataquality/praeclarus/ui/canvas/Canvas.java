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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.page.PendingJavaScriptResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for a HTML5 canvas. Heavily based on the org.vaadin.pekkam canvas addon.
 * @author Michael Adams
 * @date 18/5/21
 */
@Tag("canvas")
@JsModule("./src/workflow.js")
public class Canvas extends Component implements HasStyle, HasSize {

    private final Context2D _ctx;
    private final List<CanvasEventListener> _listeners = new ArrayList<>();

    /**
     * Creates a new canvas with the given coordinate range
     * @param width pixel width of the canvas
     * @param height pixel height of the canvas
     */
    public Canvas(int width, int height) {
        setId("workflowCanvas");
        _ctx = new Context2D(this);
        setCoOrdSpace(String.valueOf(width), String.valueOf(height));
        UI.getCurrent().getPage().executeJs("window.init()");
    }

    public void addListener(CanvasEventListener listener) {
        _listeners.add(listener);
    }

    @ClientCallable
    private void mousedown(double x, double y) {
        _listeners.forEach(l -> l.mouseDown(x, y));
    }

    @ClientCallable
    private void mousemove(double x, double y) {
        _listeners.forEach(l -> l.mouseMove(x, y));
    }

    @ClientCallable
    private void mouseup(double x, double y) {
        _listeners.forEach(l -> l.mouseUp(x, y));
    }

    @ClientCallable
    private void mouseclick(double x, double y) {
        _listeners.forEach(l -> l.mouseClick(x, y));
    }


    public Context2D getContext() { return _ctx; }


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



    private void setCoOrdSpace(String width, String height) {
        getElement().setAttribute("width", width);
        getElement().setAttribute("height", height);
    }


//    private void addResizeListener() {
//        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> redraw());
//        this.getElement().addEventListener("resize", e -> redraw());
//    }

    public void redraw() {
//        String width = callJsMethod("getClientWidth");
//        String height = getElement().getAttribute("clientHeight");
//        setCoOrdSpace(width, height);
        setDimensions();
//        drawNode();
    }

    public void setDimensions() {
        getElement().executeJs("window.setDimensions()");
    }


    public String callJsMethod(String method) {
        PendingJavaScriptResult result =
                getElement().callJsFunction("return window." + method);
        System.out.println(result);
        return result.toString();
    }

}
