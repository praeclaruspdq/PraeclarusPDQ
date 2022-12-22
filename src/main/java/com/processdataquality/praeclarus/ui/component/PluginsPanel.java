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

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;

/**
 * @author Michael Adams
 * @date 30/4/21
 */
public class PluginsPanel extends VerticalLayout {

    public PluginsPanel() {
        add(new H4("Plugins"));
        add(pluginsTree());
    }


    private TreeGrid<TreeItem> pluginsTree() {
        TreeGrid<TreeItem> tree = new TreeGrid<>();
        TreeData treeData = new TreeData();

        tree.setDragFilter(treeData::isLeaf);      // don't drag headers
        tree.setRowsDraggable(true);
        tree.setSelectionMode(Grid.SelectionMode.NONE);
        tree.setItems(treeData.getRootItems(), treeData::getChildItems);
        tree.addComponentHierarchyColumn(
                item -> {
                    Span itemName = new Span(item.getLabel());
                    if (!treeData.isLeaf(item)) {
                        itemName.getElement().getStyle().set("font-weight", "bold");
                    }
                    VerticalLayout itemLine = new VerticalLayout(itemName);
                    itemLine.setPadding(false);
                    itemLine.setSpacing(false);
                    return itemLine;
                });
        return tree;
    }

}
