package com.processdataquality.praeclarus.ui.component;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;

/**
 * @author Michael Adams
 * @date 30/4/21
 */
public class PluginsPanel extends VerticalLayout {

    public PluginsPanel() {
        add(new H3("Plugins"));
        add(pluginsTree());
    }


    private TreeGrid<TreeItem> pluginsTree() {
        TreeGrid<TreeItem> tree = new TreeGrid<>();
        TreeData treeData = new TreeData();

        tree.setDragFilter(item -> item.getParent() != null);      // don't drag headers
        tree.setRowsDraggable(true);
        tree.setSelectionMode(Grid.SelectionMode.NONE);
        tree.setItems(treeData.getRootItems(), treeData::getChildItems);
        tree.addComponentHierarchyColumn(
                item -> {
                    Span itemName = new Span(item.getName());
                    VerticalLayout itemLine = new VerticalLayout(itemName);
                    itemLine.setPadding(false);
                    itemLine.setSpacing(false);
                    return itemLine;
                });
        return tree;
    }

}
