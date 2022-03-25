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

import com.google.common.collect.ImmutableList;
import com.processdataquality.praeclarus.option.ListOption;
import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.ui.parameter.PluginParameter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.select.Select;

/**
 * @author Michael Adams
 * @date 27/10/21
 */
public class ImmutableListEditor extends AbstractEditor {

    public ImmutableListEditor(PDQPlugin plugin, PluginParameter param) {
         super(plugin, param);
     }


    @Override
    @SuppressWarnings("unchecked")
    protected Component createField(PluginParameter param) {
        
        ImmutableList<String> items = (ImmutableList<String>) param.getValue();

        Select<String> field = new Select<>();
        field.setItems(items);
        if (! items.isEmpty()) {
            field.setValue(items.get(0));
            ((ListOption<String>) param.getOption()).setSelected(items.get(0));
        }
        field.setWidth("75%");

        // user has chosen a value
        field.addValueChangeListener(e -> {
            ((ListOption<String>) param.getOption()).setSelected(e.getValue());
        });
        return field;
    }

}
