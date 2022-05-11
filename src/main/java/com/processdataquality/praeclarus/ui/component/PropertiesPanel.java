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

import com.processdataquality.praeclarus.option.HasOptions;
import com.processdataquality.praeclarus.option.Option;
import com.processdataquality.praeclarus.option.Options;
import com.processdataquality.praeclarus.ui.component.layout.VerticalScrollLayout;
import com.processdataquality.praeclarus.ui.parameter.EditorFactory;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * @author Michael Adams
 * @date 16/4/21
 */
public class PropertiesPanel extends VerticalLayout {

    private final EditorFactory _factory = new EditorFactory();
    private VerticalScrollLayout _form = null;

    public PropertiesPanel() {
        add(new H4("Parameters"));
        setSizeFull();
    }


    public void set(HasOptions container) {
        removeProperties();
        if (container != null) {
           setForm(createForm(container));
        }
    }


    private void removeProperties() {
        if (_form != null) {
            remove(_form);
        }
    }


    private VerticalScrollLayout createForm(HasOptions container) {
        VerticalScrollLayout form = new VerticalScrollLayout();
        Options options = container.getOptions();
        for (Option option : options.sort()) {
            form.add(_factory.create(container, option));
        }
        return form;
    }

    
    private void setForm(VerticalScrollLayout form) {
        _form = form;
        add(_form);
        _form.setSpacing(false);
        _form.setSizeFull();
    }
    
}
