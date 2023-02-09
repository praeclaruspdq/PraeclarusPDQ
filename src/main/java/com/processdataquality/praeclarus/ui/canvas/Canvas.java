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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for a HTML5 canvas. Heavily based on the org.vaadin.pekkam canvas addon.
 * @author Michael Adams
 * @date 18/5/21
 */
@Tag("canvas")
@JsModule("./src/canvas.js")
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

        // initialise the mouse event and file load listeners on the HTML5 canvas
        UI.getCurrent().getPage().executeJs("window.init()");
    }

    public void addListener(CanvasEventListener listener) {
        _listeners.add(listener);
    }

    public Context2D getContext() { return _ctx; }


    // These @ClientCallable methods are called directly from the canvas listeners and
    // immediately passed to the java-level component listeners

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

    @ClientCallable
    private void mousedblclick(double x, double y) {
        _listeners.forEach(l -> l.mouseDblClick(x, y));
    }

    @ClientCallable
    private void fileloaded(String jsonContent) {
        _listeners.forEach(l -> l.fileLoaded(jsonContent));
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


    public void loadFromFile() {
        UI.getCurrent().getPage().executeJs("loadFile()");
    }


    public void saveToFile(String jsonStr) {
        UI.getCurrent().getPage().executeJs("saveFile($0)", jsonStr);
    }


    // saves the current canvas content to file, then loads new content from another file
    public void saveThenLoadFile(String jsonStr) {
        UI.getCurrent().getPage().executeJs("saveThenLoadFile($0)", jsonStr);
    }


    private void setCoOrdSpace(String width, String height) {
        getElement().setAttribute("width", width);
        getElement().setAttribute("height", height);
    }

}
