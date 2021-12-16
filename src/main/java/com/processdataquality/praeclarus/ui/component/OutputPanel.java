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

import com.processdataquality.praeclarus.node.Node;
import com.processdataquality.praeclarus.repo.Differ;
import com.processdataquality.praeclarus.repo.LogEntry;
import com.processdataquality.praeclarus.repo.Repo;
import com.processdataquality.praeclarus.ui.MainView;
import com.processdataquality.praeclarus.ui.canvas.CanvasPrimitive;
import com.processdataquality.praeclarus.ui.canvas.CanvasSelectionListener;
import com.processdataquality.praeclarus.ui.canvas.Vertex;
import com.processdataquality.praeclarus.ui.util.UiUtil;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.eclipse.jgit.api.errors.GitAPIException;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.io.IOException;
import java.util.List;

/**
 * @author Michael Adams
 * @date 8/11/21
 */
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
public class OutputPanel extends VerticalLayout implements CanvasSelectionListener {

    private enum Page { OUTPUT, HISTORY, DIFF, NONE }
    
    private final VerticalLayout title = new VerticalLayout();
    private final VerticalScrollLayout page = new VerticalScrollLayout();
    private final HorizontalLayout buttonBar = new HorizontalLayout();

    private Page currentPage = Page.NONE;
    private Vertex selectedVertex;

    
    public OutputPanel(MainView parent) {
        parent.getWorkflowPanel().addVertexSelectionListener(this);
        setId("OutputPanel");
        add(title, page, buttonsPanel());
        page.setSizeFull();
        setFlexGrow(1f, page);
        setMinHeight("200px");
    }


    @Override
    public void canvasSelectionChanged(CanvasPrimitive selected) {
        selectedVertex = (selected instanceof Vertex) ? (Vertex) selected : null;
        show();
    }

    
    private HorizontalLayout buttonsPanel() {
        buttonBar.add(createButton(
                "Output", new Icon(VaadinIcon.DATABASE), Page.OUTPUT));
        buttonBar.add(createButton(
                "Log", new Icon(VaadinIcon.FILE_PROCESS), Page.HISTORY));
        buttonBar.add(createButton(
                "Diff", new Icon(VaadinIcon.PLUS_MINUS), Page.DIFF));
        return buttonBar;
    }


    private Button createButton(String label, Icon icon, Page page) {
        icon.setSize("20px");
        Button b = new Button(label, icon, e -> buttonClick(e, page));
        b.addThemeVariants(ButtonVariant.LUMO_SMALL);
        b.setHeight("24px");
        b.setWidth("96px");
        return b;
    }


    private void buttonClick(ClickEvent<Button> e, Page page) {
        deselectAllButtons();
        Button b = e.getSource();
        if (currentPage == page) {               // click on current button = unselect
            currentPage = Page.NONE;
        }
        else {
            b.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            currentPage = page;
        }
        show();
    }


    private void deselectAllButtons() {
        for (int i=0; i < buttonBar.getComponentCount(); i++) {
            Component c = buttonBar.getComponentAt(i);
            if (c instanceof Button) {
                ((Button) c).removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            }
        }
    }


    private void show() {
        clearPage();      // there's been a selection change
        switch (currentPage) {
            case NONE: break;   // nothing to do
            case OUTPUT: showOutput(); break;
            case HISTORY: showHistory(); break;
            case DIFF: showDiff(); break;
        }
    }


    private void showOutput() {
        if (selectedVertex != null) {
            Node node = selectedVertex.getNode();
            Table output = node.getOutput();
            if (output != null) {
                Grid<Row> grid = tableToGrid(output);
                setTitle(node.getLabel());
                page.add(grid);
            }
            else {
                page.add(new Html("<p>The selected node has not yet completed</p>"));
            }
        }
        else {
            page.add(new Html("<p>Select a completed node to show its output</p>"));
        }
    }


    private void showHistory() {
        if (selectedVertex != null) {
            Node node = selectedVertex.getNode();
            Table output = node.getOutput();
            if (output != null) {
                try {
                    setTitle("Change Log for Selected Dataset");
                    List<LogEntry> entries = Repo.getLog(output.name());
                    Grid<LogEntry> grid = UiUtil.logEntriesToGrid(entries);
                    UiUtil.removeTopMargin(grid);
                    page.add(grid);
                }
                catch (GitAPIException | IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                 page.add(new Html("<p>The selected node has not yet completed</p>"));
            }
        }
        else {
            page.add(new Html("<p>Select any completed node to show the dataset log</p>"));
        }
    }


    private void showDiff() {
        if (selectedVertex != null) {
            Node node = selectedVertex.getNode();
            String tableName = node.getTableID();
            if (tableName != null) {
                try {
                    setTitle("Show Dataset Differences");
                    Node prevNode = getPreviousDatasetNode(node);
                    if (prevNode != null) {
                        String dataset = getDatasetFromRepo(node);
                        String prevDataset = getDatasetFromRepo(prevNode);
                        List<Table> diffList = new Differ().diff(dataset, prevDataset);
                        VerticalLayout outerLayout = new VerticalLayout();
                        VerticalLayout prevLayout = createDiffTableLayout(diffList.get(0), prevNode.getLabel());
                        VerticalLayout currLayout = createDiffTableLayout(diffList.get(1), node.getLabel());
                        outerLayout.add(prevLayout, currLayout);
                        outerLayout.setFlexGrow(1, prevLayout, currLayout);
                        page.add(outerLayout);
                    }
                    else page.add(new Html("<p>The selected node has no previous nodes to compare to</p>"));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                 page.add(new Html("<p>The selected node has not yet completed</p>"));
            }
        }
        else {
            page.add(new Html("<p>Select any completed node to show the dataset log</p>"));
        }
    }


    private String getDatasetFromRepo(Node node) throws IOException {
        String tableName = node.getTableID();
        if (tableName != null) {
            return Repo.fetchContent(node.getCommitID(), tableName);
        }
        throw new IOException("The selected node does not contain a dataset object");
    }


    private Node getPreviousDatasetNode(Node node) {
        for (Node prevNode : node.previous()) {
            if (node.getTableID().equals(prevNode.getTableID())) {
                return prevNode;
            }
        }
        return null;
    }


    private VerticalLayout createDiffTableLayout(Table table, String subTitle) {
        VerticalLayout vl = new VerticalLayout();
        Grid<Row> grid = tableToGrid(table);
        vl.add(new H5(subTitle), new VerticalScrollLayout(grid));
        return vl;
    }


    private Grid<Row> tableToGrid(Table table) {
        Grid<Row> grid = UiUtil.tableToGrid(table);
        UiUtil.removeTopMargin(grid);
//        grid.setHeight("150px");
        return grid;
    }


    private void setTitle(String text) {
        title.removeAll();
        title.add(new H4(text));
    }


    private void clearPage() {
        title.removeAll();
        page.removeAll();
    }
}
