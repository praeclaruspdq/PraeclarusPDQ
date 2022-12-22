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

    public void fillStyle(String fillStyle) {
        setProperty("fillStyle", fillStyle);
    }

    public void strokeStyle(String strokeStyle) {
        setProperty("strokeStyle", strokeStyle);
    }

    public void textBaseline(String baseline) {
        setProperty("textBaseline", baseline);
    }

    public void lineWidth(double lineWidth) {
        setProperty("lineWidth", lineWidth);
    }

    public void font(String font) {
        setProperty("font", font);
    }

    public void arc(double x, double y, double radius, double startAngle,
            double endAngle, boolean antiClockwise) {
        callJs("arc", x, y, radius, startAngle, endAngle, antiClockwise);
    }

    public void quadraticCurveTo(double x1, double y1, double x2, double y2) {
        callJs("quadraticCurveTo", x1, y1, x2, y2);
    }

    public void bezierCurveTo(double x1, double y1, double x2, double y2, double endX, double endY) {
        callJs("bezierCurveTo", x1, y1, x2, y2, endX, endY);
    }


    public void beginPath() {
        callJs("beginPath");
    }

    public void clearRect(double x, double y, double width, double height) {
        callJs("clearRect", x, y, width, height);
    }

    public void clear() {
        double width = Double.parseDouble(_canvas.getElement().getAttribute("width"));
        double height = Double.parseDouble(_canvas.getElement().getAttribute("height"));
        clearRect(0,0, width -1, height -1);
    }

    public void closePath() {
        callJs("closePath");
    }

    public void fill() {
        callJs("fill");
    }

    public void fillRect(double x, double y, double width, double height) {
        callJs("fillRect", x, y, width, height);
    }

    public void fillText(String text, double x, double y) {
        callJs("fillText", text, x, y);
    }

    public void fillText(String text, double x, double y, double maxWidth) {
        callJs("fillText", text, x, y, maxWidth);
    }

    public void lineTo(double x, double y) {
        callJs("lineTo", x, y);
    }

    public void moveTo(double x, double y) {
        callJs("moveTo", x, y);
    }

    public void rect(double x, double y, double width, double height) {
        callJs("rect", x, y, width, height);
    }

    public void restore() {
        callJs("restore");
    }

    public void rotate(double angle) {
        callJs("rotate", angle);
    }

    public void save() {
        callJs("save");
    }

    public void scale(double x, double y) {
        callJs("scale", x, y);
    }

    public void stroke() {
        callJs("stroke");
    }

    public void strokeRect(double x, double y, double width, double height) {
        callJs("strokeRect", x, y, width, height);
    }

    public void strokeText(String text, double x, double y) {
        callJs("strokeText", text, x, y);
    }

    public void translate(double x, double y) {
        callJs("translate", x, y);
    }

    protected void setProperty(String propertyName, Serializable value) {
        runScript(String.format("$0.getContext('2d').%s='%s'", propertyName,
                value));
    }

    /**
     * Runs the given js so that the execution order works with callJsMethod().
     * Any $0 in the script will refer to the canvas element.
     */
    private void runScript(String script) {
        _canvas.getElement().getNode().runWhenAttached(
                // This structure is needed to make the execution order work
                // with Element.callFunction() which is used in callJsMethod()
                ui -> ui.getInternals().getStateTree().beforeClientResponse(
                        _canvas.getElement().getNode(),
                        context -> ui.getPage().executeJs(script,
                                _canvas.getElement())));
    }

    protected void callJs(String methodName, Serializable... parameters) {
        _canvas.getElement().callJsFunction("getContext('2d')." + methodName,
                parameters);
    }

//    public double measureText(String text) {
//        retJs("measureText", text);
//        return 0;
//    }
//
//    protected void retJs(String methodName, Serializable... parameters) {
//        PendingJavaScriptResult pend =
//         _canvas.getElement().callJsFunction("return getContext('2d')." + methodName,
//                 parameters);
//        pend.then(Double.class, new SerializableConsumer<Double>() {
//            @Override
//            public void accept(Double aDouble) {
//
//            }
//        });
//     }

}
