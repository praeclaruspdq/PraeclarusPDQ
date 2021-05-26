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

import com.processdataquality.praeclarus.plugin.Options;
import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.plugin.PluginParameter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * @author Michael Adams
 * @date 5/5/21
 */
@CssImport("./styles/pdq-styles.css")
public abstract class AbstractEditor extends HorizontalLayout {

    private final PDQPlugin _plugin;


    public AbstractEditor(PDQPlugin plugin, PluginParameter param) {
        super();
        _plugin = plugin;
        add(createLabel(param), createField(param));
        setWidth("100%");
        setMargin(false);
    }


    protected abstract Component createField(PluginParameter param);


    private Label createLabel(PluginParameter param) {
        Label l = new Label(param.getName());
        l.setWidth("25%");
        return l;
    }

    
    protected void updateProperties(PluginParameter parameter) {
        Options options = _plugin.getOptions();
        options.add(parameter.getName(), parameter.getValue());
        _plugin.setOptions(options);
    }

}
