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
import com.processdataquality.praeclarus.plugin.Options;
import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.ui.parameter.PluginParameter;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * @author Michael Adams
 * @date 16/4/21
 */
public class PropertiesPanel extends VerticalLayout {

    private PDQPlugin _plugin;
    private VerticalScrollLayout _form = null;

    public PropertiesPanel() {
        add(new H4("Parameters"));
        setSizeFull();
    }


    public void setPlugin(PDQPlugin plugin) {
        removeProperties();
        _plugin = plugin;
        if (plugin != null) {
            _form = createForm();
            add(_form);
            _form.setSpacing(false);
            _form.setSizeFull();
        }
    }


    public void removeProperties() {
        if (_form != null) {
            remove(_form);
        }
    }


    private VerticalScrollLayout createForm() {
        VerticalScrollLayout form = new VerticalScrollLayout();
        Options options = _plugin.getOptions();
        for (Option option : options.values()) {
            PluginParameter param = new PluginParameter(option);
            form.add(param.editor(_plugin));
        }
        return form;
    }
    
}
