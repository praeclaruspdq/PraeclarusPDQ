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


import com.processdataquality.praeclarus.ui.canvas.Vertex;
import com.processdataquality.praeclarus.ui.canvas.Workflow;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

/**
 * @author Michael Adams
 * @date 27/5/21
 */
public class VertexLabelDialog extends Dialog {

    private final TextField field = new TextField("Update Name");

    public VertexLabelDialog(Workflow parent, Vertex vertex) {
        setCloseOnOutsideClick(false);
        setModal(true);
        field.setValue(vertex.getLabel());
        add(field);

        Button ok = new Button("OK", event -> {
            vertex.setLabel(field.getValue());
            parent.setChanged(true);
            parent.render();
            close();
        });

        Button cancel = new Button("Cancel", event -> close());

        add(new HorizontalLayout(ok, cancel));
    }

}
