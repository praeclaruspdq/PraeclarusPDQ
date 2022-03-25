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

package com.processdataquality.praeclarus.ui.parameter.editor;

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.ui.parameter.PluginParameter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

/**
 * An abstract class defining a single entry field for a property. Sub-classes define
 * what type of field to create.
 *
 * @author Michael Adams
 * @date 5/5/21
 */
public abstract class AbstractEditor extends HorizontalLayout {

    private final PDQPlugin _plugin;         // the plugin this field is a property for
    private final PluginParameter _param;


    // adds the label and field as a single component
    public AbstractEditor(PDQPlugin plugin, PluginParameter param) {
        super();
        _plugin = plugin;
        _param = param;
        add(createLabel(param), createField(param));
        setWidth("100%");
        setMargin(false);
        getElement().getStyle().set("margin-top", "5px");
    }

    // to be implemented by sub-classes
    protected abstract Component createField(PluginParameter param);


    private Label createLabel(PluginParameter param) {
        Label l = new Label(param.getName());
        l.setWidth("25%");
        l.getElement().getStyle().set("font-size", "14px");
        return l;
    }


    protected TextField initTextField(PluginParameter parameter) {
        TextField field = new TextField();
        String value = parameter.getStringValue();
        if (value == null || value.equals("null")) value = "";
        field.setValue(value);

        field.addValueChangeListener(e -> {
            parameter.setStringValue(e.getValue());
           updateProperties(parameter);
        });
        return field;
    }

    
    protected void updateProperties(PluginParameter parameter) {
        _plugin.getOptions().update(parameter.getOption());
    }


    protected PDQPlugin getPlugin() { return _plugin; }

    protected PluginParameter getParam() { return _param; }

}
