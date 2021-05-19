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

package com.processdataquality.praeclarus.ui.component.canvas;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.page.PendingJavaScriptResult;

/**
 * Wrapper for a HTML5 canvas. Heavily based on the org.vaadin.pekkam canvas addon.
 * @author Michael Adams
 * @date 18/5/21
 */
@Tag("canvas")
@JsModule("./src/workflow.js")
public class Canvas extends Component implements HasStyle, HasSize {

    private final Context2D _ctx;

    /**
     * Creates a new canvas with the given coordinate range
     * @param width pixel width of the canvas
     * @param height pixel height of the canvas
     */
    public Canvas(int width, int height) {
        setId("workflowCanvas");
        _ctx = new Context2D(this);
        setCoOrdSpace(String.valueOf(width), String.valueOf(height));
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


    public void drawNode(int nodeCount) {
        int sep = 50;
        int width = 100;
        int height = 80;
        int x = 50 + ((sep + width) * nodeCount);
        int y = 50;
        _ctx.lineWidth(2);
        _ctx.beginPath();
        _ctx.rect(x, y, width, height);
        _ctx.stroke();
    
        _ctx.beginPath();
        _ctx.arc(x, y+40,5, 0, 360, false);
        _ctx.fill();
        _ctx.stroke();
    
        _ctx.beginPath();
        _ctx.arc(x+100, y+40,5, 0, 360, false);
        _ctx.fill();
        _ctx.stroke();


//        var words = name.split(" ");
//        var y = topY + 20;
//        var i;
//        for (i=0; i < words.length; i++) {
//            ctx.fillText(words[i], leftX + 10, y, 80);
//            y = y + 30;
//        }

//        _ctx.beginPath();
//        _ctx.rect(20, 20, 100, 80);
//        _ctx.stroke();
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
