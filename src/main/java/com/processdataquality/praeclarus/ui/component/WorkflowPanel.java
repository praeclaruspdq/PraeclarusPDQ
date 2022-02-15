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

import com.processdataquality.praeclarus.node.*;
import com.processdataquality.praeclarus.pattern.ImperfectionPattern;
import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.plugin.PluginService;
import com.processdataquality.praeclarus.plugin.uitemplate.ButtonAction;
import com.processdataquality.praeclarus.plugin.uitemplate.PluginUI;
import com.processdataquality.praeclarus.ui.MainView;
import com.processdataquality.praeclarus.ui.canvas.*;
import com.processdataquality.praeclarus.ui.util.UiUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.boot.configurationprocessor.json.JSONException;

import java.util.List;

/**
 * @author Michael Adams
 * @date 30/4/21
 */
@CssImport("./styles/pdq-styles.css")
@JsModule("./src/fs.js")
public class WorkflowPanel extends VerticalLayout
        implements NodeRunnerListener, PluginUIListener {

    private final Workflow _workflow;                 // frontend
    private final MainView _parent;
    private final RunnerButtons _runnerButtons;
    private final Canvas _canvas;
    private final NodeRunner _runner;

    private Button _removeButton;
    private Button _saveButton;


    public WorkflowPanel(MainView parent) {
        _parent = parent;
        _canvas = new Canvas(1600, 800);
        _runner = new NodeRunner();
        _workflow = new Workflow(this, _canvas.getContext());
        _canvas.addListener(_workflow);
        _runnerButtons = initRunnerButtons();
        addVertexSelectionListener(_runnerButtons);
        _runner.addListener(this);
        
        VerticalLayout vl = new VerticalLayout();
        vl.add(new H4("Workflow"), _runnerButtons);
        UiUtil.removeTopMargin(vl);
        add(vl, createCanvasContainer());
    }


    @Override
    public void pluginUICloseEvent(ButtonAction action, Node node) {
        _runner.resume(node);          // action doesn't matter
    }
    

    @Override
    public void runnerStateChanged(NodeRunner.RunnerState state) {
        _runnerButtons.setState(state);
    }


    @Override
    public void runnerNodePaused(Node node) {

        // pattern detected but not yet repaired
        if (node instanceof PatternNode && ! node.hasCompleted()) {
            PluginUI ui = ((ImperfectionPattern) node.getPlugin()).getUI();
            if (ui != null) {
                new PluginUIDialog(ui, node, this).open();
            }
        }
    }


    public boolean hasChanges() { return _workflow.hasChanges(); }


    public void setWorkflowChanged(boolean changed) {
        _parent.setUnsavedChanges(changed);
    }


    private RunnerButtons initRunnerButtons() {
        RunnerButtons buttons = new RunnerButtons(_runner);
        _removeButton = createRemoveButton();
        _removeButton.setEnabled(false);
        _saveButton = createSaveButton();
        _saveButton.setEnabled(false);
        buttons.addButton(_removeButton);
        buttons.addButton(createLoadButton());
        buttons.addButton(_saveButton);
        return buttons;
    }

    
    private VerticalLayout createCanvasContainer() {
        DropTarget<Canvas> dropTarget = DropTarget.create(_canvas);
        dropTarget.setDropEffect(DropEffect.COPY);
        dropTarget.addDropListener(event -> {
            if (event.getDropEffect() == DropEffect.COPY) {
                if (event.getDragData().isPresent()) {

                    @SuppressWarnings("unchecked")
                    List<TreeItem> droppedItems = (List<TreeItem>) event.getDragData().get();

                    TreeItem item = droppedItems.get(0);     // only one is dropped
                    Node node = addPluginInstance(item);
                    _workflow.addVertex(node);
                    _runnerButtons.enable();
                }
            }
        });

        VerticalScrollLayout container = new VerticalScrollLayout();
        container.add(_canvas);
        UiUtil.removeTopMargin(container);
        return container;
    }


    public void changedSelected(Node selected) {
        showPluginProperties(selected);
        _runnerButtons.enable();
    }


    public void canvasSelectionChanged(CanvasPrimitive selected) {
        _removeButton.setEnabled(selected instanceof Vertex);
        _saveButton.setEnabled(_workflow.hasContent());
    }


    public void showPluginProperties(Node selected) {
        PDQPlugin plugin = selected != null ? selected.getPlugin() : null;
        if (plugin != null) {
            _parent.getPropertiesPanel().set(plugin);
        }
        else {
            _parent.getPropertiesPanel().set(_workflow);
        }
    }


    private Node addPluginInstance(TreeItem item) {
        String pTypeName = item.getRoot().getLabel();
        PDQPlugin instance = null;
        if (pTypeName.equals("Readers")) {
            instance = PluginService.readers().newInstance(item.getName());
        }
        if (pTypeName.equals("Writers")) {
            instance = PluginService.writers().newInstance(item.getName());
        }
        if (pTypeName.equals("Patterns")) {
            instance = PluginService.patterns().newInstance(item.getName());
        }
        if (pTypeName.equals("Actions")) {
             instance = PluginService.actions().newInstance(item.getName());
        }

        Node node = NodeFactory.create(instance);
        showPluginProperties(node);
        return node;
    }
    
    
    /**
     * @return the runner for this panel
     */
    public NodeRunner getRunner() { return _runner; }


    private Button createRemoveButton() {
        Icon icon = VaadinIcon.TRASH.create();
        icon.setSize("24px");
        Button removeButton = new Button(icon, e -> {
            _workflow.removeSelected();
            _saveButton.setEnabled(_workflow.hasContent());
        });
        removeButton.getElement().setAttribute("title", "Remove node");
        return removeButton;
    }


    private Button createLoadButton() {
        Icon icon = VaadinIcon.FOLDER_OPEN_O.create();
        icon.setSize("24px");
        Button loadButton = new Button(icon, e -> {
            if (_workflow.hasChanges()) {
                MessageDialog dialog = new MessageDialog(
                        "Save changes to existing workflow?");
                dialog.setText("Click 'Save' to save changes, " +
                        "'Discard' to discard changes, 'Cancel' to keep working.");
                dialog.addButton(new Button("Save", s -> saveThenLoadNewWorkflow()));
                dialog.addButton(new Button("Discard", d -> _canvas.loadFromFile()));
                dialog.addButton(new Button("Cancel"));
                dialog.open();
            }
            else {
                _canvas.loadFromFile();
            }
        });
        loadButton.getElement().setAttribute("title", "Load workflow");
        return loadButton;
    }


    private Button createSaveButton() {
        Icon icon = VaadinIcon.DOWNLOAD.create();
        icon.setSize("24px");
        Button saveButton = new Button(icon, e -> saveWorkflow());
        saveButton.getElement().setAttribute("title", "Save workflow");
        return saveButton;
    }


    private boolean saveWorkflow() {
        try {
            String jsonStr = _workflow.asJson().toString(3);
            _canvas.saveToFile(jsonStr);
            _workflow.setChanged(false);
            return true;
        }
        catch (JSONException je) {
            Notification.show("Failed to save file: " + je.getMessage());
        }
        return false;
    }


    private void saveThenLoadNewWorkflow() {
        try {
            String jsonStr = _workflow.asJson().toString(3);
            _canvas.saveThenLoadFile(jsonStr);
        }
        catch (JSONException je) {
            Notification.show("Failed to save file: " + je.getMessage());
        }
    }


    public void onResize() {
        _workflow.render();
    }


    public void addVertexSelectionListener(CanvasSelectionListener listener) {
        _workflow.addVertexSelectionListener(listener);
    }
}
