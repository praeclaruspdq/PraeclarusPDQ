/*
 * Copyright (c) 2022 Queensland University of Technology
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

package com.processdataquality.praeclarus.ui.component.dialog;


import com.processdataquality.praeclarus.node.Node;
import com.processdataquality.praeclarus.ui.canvas.Workflow;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.textfield.TextField;

/**
 * @author Michael Adams
 * @date 27/5/21
 */
public class VertexLabelDialog extends AbstractDialog {

    private final TextField field = new TextField();

    public VertexLabelDialog(Workflow workflow, Node node) {
        super("Vertex Label");
        addField(node.getLabel());
        addButtons(workflow, node);
    }


    private void addField(String value) {
        field.setValue(value);
        field.setWidth("460px");
        field.getStyle().set("margin-bottom", "20px");
        addComponent(field);
    }


    private void addButtons(Workflow workflow, Node node) {
        Button ok = new Button("OK", event -> {
            node.setLabel(field.getValue());
            workflow.setChanged(true);
            workflow.render();
            close();
        });
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Cancel", event -> close());

        getButtonBar().add(cancel, ok);
    }

}
