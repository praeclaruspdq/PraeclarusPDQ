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
import com.processdataquality.praeclarus.ui.task.WriterTask;
import com.processdataquality.praeclarus.workspace.NodeRunner;
import com.processdataquality.praeclarus.workspace.node.Node;
import com.processdataquality.praeclarus.workspace.node.NodeRunnerListener;
import com.processdataquality.praeclarus.workspace.node.PatternNode;
import com.processdataquality.praeclarus.workspace.node.WriterNode;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.*;

/**
 * @author Michael Adams
 * @date 30/4/21
 */
public class ResultsPanel extends VerticalLayout implements NodeRunnerListener {

    Tabs tabs = new Tabs();
    VerticalScrollLayout pages = new VerticalScrollLayout();
    Map<Tab, Component> tabsToPages = new HashMap<>();

    private final MainView _parent;

    public ResultsPanel(MainView parent) {
        _parent = parent;
        setId("ResultsPanel");
        add(new H3("Results"));

        tabs.addSelectedChangeListener(event -> {
            tabsToPages.values().forEach(page -> page.setVisible(false));
            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);
        });

        add(tabs, pages);
        removeTopMargin(pages);
        removeTopMargin(tabs);
        pages.setSizeFull();
        setFlexGrow(1f, pages);

        setSizeFull();
        tabs.setVisible(false);

        getNodeRunner().addListener(this);
    }

    @Override
    public void nodeStarted(Node node) { }

    @Override
    public void nodePaused(Node node) { addResult(node); }

    @Override
    public void nodeCompleted(Node node) { addResult(node); }

    @Override
    public void nodeRollback(Node node) { removeResult(node); }


    public void addResult(Node node) {
        if (node instanceof WriterNode) {        // special treatment for writers
            new WriterTask().run(node);
            return;
        }
        
        Grid<Row> grid = createGrid(node);
        removeTopMargin(grid);

        VerticalScrollLayout page;
        Tab tab = getTab(node);
        if (tab != null) {
            page = (VerticalScrollLayout) tabsToPages.get(tab);
            page.removeAll();
            page.add(grid);
        }
        else {
            tab = new ResultTab(node);
            page = new VerticalScrollLayout(grid);
            removeTopMargin(page);
            tabsToPages.put(tab, page);
            pages.add(page);
            tabs.add(tab);
        }

        if (node instanceof PatternNode) {
            handlePatternResult(node, grid, tab);
        }
        
        tabs.setSelectedTab(tab);
        tabs.setVisible(true);
    }


    private Tab getTab(Node node) {
        for (ResultTab tab : getTabs(node)) {
            if (tab.resultEquals(node)) {
                return tab;
            }
        }
        return null;
     }


    private void handlePatternResult(Node node, Grid<Row> grid, Tab tab) {
        if (!node.hasCompleted()) {
            tab.setLabel(node.getName() + " - Detected");
            grid.setSelectionMode(Grid.SelectionMode.MULTI);

            Button btnRepair = new Button("Repair Selected");
            Button btnDont = new Button("Don't Repair");
            btnRepair.addClickListener(e -> {
                btnRepair.setEnabled(false);   // only allow one repair
                btnDont.setEnabled(false);
                repair(node, grid);
            });
            btnDont.addClickListener(e -> {
                btnRepair.setEnabled(false);   // only allow one repair
                btnDont.setEnabled(false);
                getNodeRunner().resume(node);
            });
            FooterRow footer = grid.appendFooterRow();
            footer.getCells().get(0).setComponent(new HorizontalLayout(btnDont, btnRepair));
        }
        else {
            tab.setLabel(node.getName() + " - Repaired");
        }
    }


    public void removeResult(Node node) {
        for (ResultTab tab : getTabs(node)) {
            Div div = (Div) tabsToPages.remove(tab);
            tabs.remove(tab);
            pages.remove(div);
        }
        tabs.setVisible(! tabsToPages.isEmpty());
    }


    public void clear() {
        tabs.removeAll();
        tabs.setVisible(false);
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

        NodeRunner runner = getNodeRunner();
        runner.resume(node);
    }


    private void removeTopMargin(Component c) {
        c.getElement().getStyle().set("margin-top", "0");
    }


    private NodeRunner getNodeRunner() {
        return _parent.getPipelinePanel().getWorkspace().getRunner();
    }


    private Set<ResultTab> getTabs(Node node) {
        Set<ResultTab> tabSet = new HashSet<>();
        for (int i=0; i < tabs.getComponentCount(); i++) {
             ResultTab tab = (ResultTab) tabs.getComponentAt(i);
             if (tab.nodeEquals(node)) {
                tabSet.add(tab);
             }
         }
         return tabSet;
    }

}
