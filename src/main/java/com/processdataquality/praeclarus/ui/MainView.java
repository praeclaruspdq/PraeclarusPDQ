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

package com.processdataquality.praeclarus.ui;

import com.processdataquality.praeclarus.ui.component.OutputPanel;
import com.processdataquality.praeclarus.ui.component.PluginsPanel;
import com.processdataquality.praeclarus.ui.component.PropertiesPanel;
import com.processdataquality.praeclarus.ui.component.WorkflowPanel;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.Route;

/**
 * The primary view for the web UI
 *
 * @author Michael Adams
 * @date 14/4/21
 */
@Route
@JsModule("./src/unload.js")
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
public class MainView extends VerticalLayout {

    private final PropertiesPanel _propsPanel;
    private final WorkflowPanel _workflowPanel;


    public MainView() {
        setId("mainview");
        _propsPanel = new PropertiesPanel();
        _workflowPanel = new WorkflowPanel(this);
        SplitLayout masterLayout = new SplitLayout();
        masterLayout.addToPrimary(leftPanel());
        masterLayout.addToSecondary(centrePanel());
        add(masterLayout);
        masterLayout.setSizeFull();
        setSizeFull();
    }

    public PropertiesPanel getPropertiesPanel() { return _propsPanel; }


    public WorkflowPanel getWorkflowPanel() { return _workflowPanel; }


    public void setUnsavedChanges(boolean changed) {
        String js = "window." + (changed ? "set" : "reset") + "Changed()";
        UI.getCurrent().getPage().executeJs(js);
    }
    

    private SplitLayout leftPanel() {
        SplitLayout leftLayout = new SplitLayout();
        leftLayout.setOrientation(SplitLayout.Orientation.VERTICAL);

        // title image added here to save wasted space across page top
        leftLayout.addToPrimary(getTitleImage(), new PluginsPanel());
        leftLayout.addToSecondary(_propsPanel);
        leftLayout.setWidth("23%");
        leftLayout.addSplitterDragendListener(e -> _workflowPanel.onResize());
        return leftLayout;
    }


    private SplitLayout centrePanel() {
        SplitLayout centreLayout = new SplitLayout();
        centreLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
        centreLayout.addToPrimary(_workflowPanel);
        centreLayout.addToSecondary(new OutputPanel(this));
        centreLayout.setWidth("77%");
        centreLayout.addSplitterDragendListener(e -> _workflowPanel.onResize());
        return centreLayout;
    }

    
    private Image getTitleImage() {
        Image image = new Image("icons/praeclarus.png", "Praeclarus");
        image.setHeight("48px");
        return image;
    }

}
