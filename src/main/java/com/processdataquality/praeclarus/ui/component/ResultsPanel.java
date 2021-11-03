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

import com.processdataquality.praeclarus.pattern.ImperfectionPattern;
import com.processdataquality.praeclarus.plugin.uitemplate.ButtonAction;
import com.processdataquality.praeclarus.plugin.uitemplate.PluginUI;
import com.processdataquality.praeclarus.ui.MainView;
import com.processdataquality.praeclarus.ui.task.WriterTask;
import com.processdataquality.praeclarus.ui.util.UiUtil;
import com.processdataquality.praeclarus.workspace.NodeRunner;
import com.processdataquality.praeclarus.workspace.node.Node;
import com.processdataquality.praeclarus.workspace.node.NodeRunnerListener;
import com.processdataquality.praeclarus.workspace.node.PatternNode;
import com.processdataquality.praeclarus.workspace.node.WriterNode;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import tech.tablesaw.api.Row;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 30/4/21
 */
public class ResultsPanel extends VerticalLayout implements NodeRunnerListener, PluginUIListener {

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
        UiUtil.removeTopMargin(pages);
        UiUtil.removeTopMargin(tabs);
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

    @Override
    public void pluginUICloseEvent(ButtonAction action, Node node) {
        if (action == ButtonAction.REPAIR) {
            getNodeRunner().resume(node);             //todo: resume after cancel?
        }
    }


    public void addResult(Node node) {
        if (node instanceof WriterNode) {        // special treatment for writers
            new WriterTask().run(node);
            return;
        }

        // pattern detected but not yet repaired
        if (node instanceof PatternNode && ! node.hasCompleted()) {
            PluginUI ui = ((ImperfectionPattern) node.getPlugin()).getUI();
            if (ui != null) {
                new PluginUIDialog(ui, node, this).open();
            }
            return;
        }

        Grid<Row> grid = UiUtil.tableToGrid(node.getOutput());
        UiUtil.removeTopMargin(grid);

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
            UiUtil.removeTopMargin(page);
            tabsToPages.put(tab, page);
            pages.add(page);
            tabs.add(tab);
        }
        if (node instanceof PatternNode) {
            tab.setLabel(node.getName() + " - Repaired");
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
