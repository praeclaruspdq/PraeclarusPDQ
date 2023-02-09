/*
 * Copyright (c) 2021-2022 Queensland University of Technology
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

import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Lettuce.Cluster.Refresh;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.processdataquality.praeclarus.graph.GraphRunner;
import com.processdataquality.praeclarus.option.HasOptions;
import com.processdataquality.praeclarus.option.Option;
import com.processdataquality.praeclarus.option.Options;
import com.processdataquality.praeclarus.ui.MainView;
import com.processdataquality.praeclarus.ui.component.layout.VerticalScrollLayout;
import com.processdataquality.praeclarus.ui.parameter.EditorFactory;
import com.processdataquality.praeclarus.ui.util.UiUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * @author Michael Adams
 * @date 16/4/21
 */
public class PropertiesPanel extends VerticalLayout {

    private final EditorFactory _factory = new EditorFactory();
    private final MainView _parent;
    private VerticalScrollLayout _form = null;

    public PropertiesPanel(MainView parent) { 
    	_parent = parent;
    	H4 title = new H4("Parameters");
    	Button refreshButton = new Button(UiUtil.createIcon(VaadinIcon.REFRESH), e -> {
    		_parent.getWorkflowPanel().showPluginProperties(_parent.getWorkflowPanel().getWorkflow().getSelectedNode());
    	});
    	HorizontalLayout hl = new HorizontalLayout(); 
    	hl.add(title);
        hl.add(refreshButton);
        hl.setSpacing(false);
        hl.setWidthFull();
    	hl.setJustifyContentMode(JustifyContentMode.BETWEEN);
        add(hl);
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
        if (options != null) {
            for (Option option : options.sort()) {
                form.add(_factory.create(container, option));
            }
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
