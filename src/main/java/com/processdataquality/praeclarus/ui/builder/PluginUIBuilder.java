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

package com.processdataquality.praeclarus.ui.builder;

import com.processdataquality.praeclarus.plugin.uitemplate.*;
import com.processdataquality.praeclarus.ui.component.plugin.PluginUIDialog;
import com.processdataquality.praeclarus.ui.component.layout.VerticalScrollLayout;
import com.processdataquality.praeclarus.ui.util.UiUtil;
import com.processdataquality.praeclarus.node.Node;
import com.processdataquality.praeclarus.node.PatternNode;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 2/11/21
 */
public class PluginUIBuilder {

    // maps plugin UI components to their Vaadin instantiations
    private final Map<UIComponent, Component> _componentMap = new HashMap<>();

    private final PluginUI _pluginUI;


    public PluginUIBuilder(PluginUI pluginUI) {
        _pluginUI = pluginUI;
    }


    public VerticalLayout build(Node node, PluginUIDialog dialog) {
        VerticalLayout parent = new VerticalLayout();
        parent.add(new H4(_pluginUI.getTitle()));
        for (UIContainer container : _pluginUI.getContainers()) {
             parent.add(buildLayout(container, node, dialog));
        }
        return parent;
    }


    private Component buildLayout(UIContainer container, Node node, PluginUIDialog dialog) {
        HasComponents layout;
        if (container.getOrientation() == UIContainer.Orientation.HORIZONTAL) {
            layout = new HorizontalLayout();
        }
        else {
            layout = new VerticalLayout();
        }
        for (UIComponent uiComponent : container.getComponents()) {
            Component c = buildContent(uiComponent, node, dialog);
            if (c != null) layout.add(c);
        }
        return (Component) layout;
    }

    
    private Component buildContent(UIComponent component, Node node, PluginUIDialog dialog) {
        if (component instanceof UIContainer) {
            return buildLayout((UIContainer) component, node, dialog);
        }
        if (component instanceof UIButton) {
            UIButton uib = (UIButton) component;
            return buildButton(uib, node, dialog);
        }
        if (component instanceof UITable) {
            return buildGrid((UITable) component);
        }
        return null;
    }


    private Button buildButton(UIButton uib, Node node, PluginUIDialog dialog) {
        Button button = new Button(uib.getLabel());
        ButtonAction action = uib.getAction();
        if (action == ButtonAction.REPAIR) {
            button.addClickListener(buttonClickEvent -> {
                updatePluginUI();
                ((PatternNode) node).updateUI(_pluginUI);
                if (uib.hasListener()) {
                    uib.getListener().clickEvent();
                }
                close(action, node, dialog);
            });
        }
        else if (action == ButtonAction.OK) {
            button.addClickListener(buttonClickEvent -> {
                if (uib.hasListener()) {
                    uib.getListener().clickEvent();
                }
                close(action, node, dialog);
            });
        }
        else if (action == ButtonAction.CANCEL) {
            button.addClickListener(buttonClickEvent -> close(action, node, dialog));
        }
        else if (action == ButtonAction.RUN) {
            button.addClickListener(buttonClickEvent -> {
                if (uib.hasListener()) {
                    uib.getListener().clickEvent();
                }
            });
        }
        _componentMap.put(uib, button);
        return button;
    }


    private VerticalScrollLayout buildGrid(UITable uiTable) {
        Grid<Row> grid = UiUtil.tableToGrid(uiTable.getTable());
        UiUtil.removeTopMargin(grid);
        if (uiTable.isMultiSelect()) {
            grid.setSelectionMode(Grid.SelectionMode.MULTI);
        }
        VerticalScrollLayout layout = new VerticalScrollLayout();
        layout.add(grid);
        _componentMap.put(uiTable, grid);
        return layout;
    }


    private void close(ButtonAction action, Node node, PluginUIDialog dialog) {
        dialog.close(action, node);
    }


    private void updatePluginUI() {
        for (UIContainer container : _pluginUI.getContainers()) {
            for (UIComponent uiComponent : container.getComponents()) {
                 if (uiComponent instanceof UITable) {
                     UITable uiTable = (UITable) uiComponent;
                     Table ogTable = uiTable.getTable();

                     @SuppressWarnings("unchecked")
                     Grid<Row> grid = (Grid<Row>) _componentMap.get(uiComponent);
                     
                     uiTable.setUpdatedTable(UiUtil.gridToTable(grid, ogTable));
                     uiTable.setSelectedRows(UiUtil.gridSelectedToTable(grid, ogTable));
                 }
            }
        }
    }

}
