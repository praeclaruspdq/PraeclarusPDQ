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

import com.processdataquality.praeclarus.plugin.Option;
import com.processdataquality.praeclarus.ui.canvas.Workflow;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

/**
 * @author Michael Adams
 * @date 30/11/21
 */
public class WorkflowOptionEditor extends HorizontalLayout {

    public WorkflowOptionEditor(Workflow workflow, Option option) {
        super();
        add(createLabel(option), createField(workflow, option));
        setWidth("100%");
        setMargin(false);
        getElement().getStyle().set("margin-top", "5px");

    }


    private Label createLabel(Option option) {
        Label l = new Label(option.key());
        l.setWidth("25%");
        l.getElement().getStyle().set("font-size", "14px");
        return l;
    }


    protected TextField createField(Workflow workflow, Option option) {
        TextField tf = initTextField(workflow, option);
        tf.setWidth("75%");
        return tf;
    }


    protected TextField initTextField(Workflow workflow, Option option) {
        TextField field = new TextField();
        String value = option.asString();
        if (value == null || value.equals("null")) value = "";
        field.setValue(value);

        field.addValueChangeListener(e -> {
           workflow.setUserOption(new Option(option.key(), e.getValue()));
        });
        return field;
    }
}
