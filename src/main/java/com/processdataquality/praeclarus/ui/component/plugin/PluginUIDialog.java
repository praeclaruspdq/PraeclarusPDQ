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

package com.processdataquality.praeclarus.ui.component.plugin;


import com.processdataquality.praeclarus.plugin.uitemplate.ButtonAction;
import com.processdataquality.praeclarus.plugin.uitemplate.PluginUI;
import com.processdataquality.praeclarus.ui.builder.PluginUIBuilder;
import com.processdataquality.praeclarus.node.Node;
import com.vaadin.flow.component.dialog.Dialog;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 1/11/21
 */
public class PluginUIDialog extends Dialog {

    private final Set<PluginUIListener> _listeners = new HashSet<>();

    public PluginUIDialog(PluginUI pluginUI, Node node) {
        setCloseOnOutsideClick(false);
        setModal(true);
        setWidth("800px");
        setHeight("600px");
        add(new PluginUIBuilder(pluginUI).build(node, this));
    }


    public PluginUIDialog(PluginUI pluginUI, Node node, PluginUIListener listener) {
        this(pluginUI, node);
        addListener(listener);
    }

    public void addListener(PluginUIListener listener) {
        _listeners.add(listener);
    }

    public void close(ButtonAction action, Node node) {
        super.close();
        for (PluginUIListener listener : _listeners) {
            listener.pluginUICloseEvent(action, node);
        }
    }

}
