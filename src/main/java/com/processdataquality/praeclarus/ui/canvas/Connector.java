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
 * @date 19/5/21
 */
public class Connector {

    private final Port _source;
    private final Port _target;

    public Connector(Port source, Port target) {
        _source = source;
        _target = target;
    }

    public Vertex getSource() {
        return _source.getParent();
    }

    public Vertex getTarget() {
        return _target.getParent();
    }

    public boolean connects(Vertex vertex) {
        return getSource().equals(vertex) || getTarget().equals(vertex);
    }

    public void render(Context2D ctx) {
        ctx.strokeStyle("black");
        ctx.lineWidth(2);
        ctx.beginPath();
        ctx.moveTo(0,5);
        ctx.lineTo(5,5);
        ctx.stroke();
    }
}
