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

package com.processdataquality.praeclarus.ui.builder;

import com.processdataquality.praeclarus.plugin.uitemplate.*;
import com.processdataquality.praeclarus.ui.component.PluginUIDialog;
import com.processdataquality.praeclarus.ui.component.VerticalScrollLayout;
import com.processdataquality.praeclarus.ui.util.UiUtil;
import com.processdataquality.praeclarus.workspace.node.Node;
import com.processdataquality.praeclarus.workspace.node.PatternNode;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

/**
 * @author Michael Adams
 * @date 2/11/21
 */
public class PluginUIBuilder {

    private Grid<Row> _grid;
    private Table _table;


    public VerticalLayout build(PluginUI pluginUI, Node node, PluginUIDialog dialog) {
        VerticalLayout parent = new VerticalLayout();
        parent.add(new H4(pluginUI.getTitle()));
        for (UIContainer layout : pluginUI.getLayouts()) {
             parent.add(buildContainer(layout, node, dialog));
        }
        return parent;
    }


    private Component buildContainer(UIContainer layout, Node node, PluginUIDialog dialog) {
        HasComponents container;
        if (layout.getOrientation() == UIContainer.Orientation.HORIZONTAL) {
            container = new HorizontalLayout();
        }
        else {
            container = new VerticalLayout();
        }
        for (UIComponent uiComponent : layout.getComponents()) {
            Component c = buildContent(uiComponent, node, dialog);
            if (c != null) container.add();
        }
        return (Component) container;
    }

    
    private Component buildContent(UIComponent component, Node node, PluginUIDialog dialog) {
        if (component instanceof UIButton) {
            UIButton uib = (UIButton) component;
            return buildButton(uib, node, dialog);
        }
        else if (component instanceof UITable) {
            UITable uit = (UITable) component;
            _table = uit.getTable();
            _grid = UiUtil.tableToGrid(_table);
            UiUtil.removeTopMargin(_grid);
            if (uit.isMultiSelect()) {
                _grid.setSelectionMode(Grid.SelectionMode.MULTI);
            }
            VerticalScrollLayout layout = new VerticalScrollLayout();
            layout.add(_grid);
            return layout;
        }
        return null;
    }


    private Button buildButton(UIButton uib, Node node, PluginUIDialog dialog) {
        Button button = new Button(uib.getLabel());
        ButtonAction action = uib.getAction();
        if (action == ButtonAction.REPAIR) {
            button.addClickListener(buttonClickEvent -> {
                Table repairs = UiUtil.gridSelectedToTable(_grid, _table);
                ((PatternNode) node).setRepairs(repairs);
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
        return button;
    }


    private void close(ButtonAction action, Node node, PluginUIDialog dialog) {
        dialog.close(action, node);
    }

}
