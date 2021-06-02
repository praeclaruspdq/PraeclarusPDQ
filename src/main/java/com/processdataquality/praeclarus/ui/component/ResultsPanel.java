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

import com.processdataquality.praeclarus.ui.MainView;
import com.processdataquality.praeclarus.workspace.node.Node;
import com.processdataquality.praeclarus.workspace.node.PatternNode;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import tech.tablesaw.api.*;

import java.util.*;

/**
 * @author Michael Adams
 * @date 30/4/21
 */
public class ResultsPanel extends VerticalLayout {

    Tabs tabs = new Tabs();
    Div pages = new Div();
    Map<Tab, Component> tabsToPages = new HashMap<>();

    private final MainView _parent;

    public ResultsPanel(MainView parent) {
        _parent = parent;
        add(new H3("Results"));

        tabs.addSelectedChangeListener(event -> {
            tabsToPages.values().forEach(page -> page.setVisible(false));
            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);
        });

        VerticalScrollLayout layout = new VerticalScrollLayout();
        layout.add(tabs, pages);
        add(layout);
        pages.setSizeFull();
        setSizeFull();
        tabs.setVisible(false);
    }

    public void addResult(Node node) {
        Tab tab = new Tab(node.getName());
        Grid<Row> grid = createGrid(node);
        Div page = new Div(grid);
        if (node instanceof PatternNode) {
            grid.setSelectionMode(Grid.SelectionMode.MULTI);
            FooterRow footer = grid.appendFooterRow();
            footer.getCells().get(0).setComponent(
                    new Button("Repair Selected", e -> repair(node, grid)));
        }
        tabsToPages.put(tab, page);
        tabs.add(tab);
        tabs.setVisible(true);
        pages.add(page);
    }


    public void addResults(Node node) {
        addResult(node);
        while (node.hasNext()) {
            node = node.next();
            addResult(node);
        }
    }

    public void removeResult(Node node) {
        String title = node.getName();
        for (int i = 0; i < tabs.getComponentCount(); i++) {
             Tab tab = ((Tab) tabs.getComponentAt(i));
             if (tab.getLabel().equals(title)) {
                 Div div = (Div) tabsToPages.remove(tab);
                 tabs.remove(tab);
                 pages.remove(div);
                 break;
             }
        }
        tabs.setVisible(! tabsToPages.isEmpty());
    }


    public void clear() {
        tabs.removeAll();
        pages.removeAll();
        tabsToPages.clear();
    }


    private Grid<Row> createGrid(Node node) {
        Table table = node.getOutput();
        Grid<Row> grid = new Grid<>();
        for (String name : table.columnNames()) {
            ColumnType colType = table.column(name).type();
            Grid.Column<Row> column;
            if (colType == ColumnType.STRING) {
                column = grid.addColumn(row -> row.getString(name));
            }
            else if (colType == ColumnType.BOOLEAN) {
                column = grid.addColumn(row -> row.getBoolean(name));
            }
            else if (colType == ColumnType.INTEGER) {
                column = grid.addColumn(row -> row.getInt(name));
            }
            else if (colType == ColumnType.LONG) {
                column = grid.addColumn(row -> row.getLong(name));
            }
            else if (colType == ColumnType.FLOAT) {
                column = grid.addColumn(row -> row.getFloat(name));
            }
            else if (colType == ColumnType.DOUBLE) {
                column = grid.addColumn(row -> row.getDouble(name));
            }
            else if (colType == ColumnType.LOCAL_DATE) {
                column = grid.addColumn(row -> row.getDate(name));
            }
            else if (colType == ColumnType.LOCAL_TIME || colType == ColumnType.LOCAL_DATE_TIME) {
                column = grid.addColumn(row -> row.getDateTime(name));
            }
            else if (colType == ColumnType.INSTANT) {
                column = grid.addColumn(row -> row.getInstant(name));
            }
            else {
                column = grid.addColumn(row -> row.getObject(name));
            }

            column.setHeader(name).setAutoWidth(true);
        }

        List<Row> rows = new ArrayList<>();
        for (int i=0; i < table.rowCount(); i++) {
            rows.add(table.row(i));
        }
        grid.setItems(rows);
        return grid;
    }


    private void repair(Node node, Grid<Row> grid) {
        Table repairs = Table.create("Repairs").addColumns(
                        StringColumn.create("Incorrect"),
                        StringColumn.create("Correct"));
        for (Row row : grid.asMultiSelect().getSelectedItems()) {
            repairs.column(0).appendCell(row.getString("Label2"));
            repairs.column(1).appendCell(row.getString("Label1"));
        }
        ((PatternNode) node).setRepairs(repairs);
        _parent.getPipelinePanel().getWorkspace().getRunner().resume(node);
    }

}
