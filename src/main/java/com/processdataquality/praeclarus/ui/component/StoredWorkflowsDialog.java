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


import com.processdataquality.praeclarus.ui.canvas.Workflow;
import com.processdataquality.praeclarus.ui.repo.StoredWorkflow;
import com.processdataquality.praeclarus.ui.repo.WorkflowStore;
import com.processdataquality.praeclarus.ui.util.UiUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import org.springframework.boot.configurationprocessor.json.JSONException;

import java.util.List;
import java.util.Optional;

/**
 * @author Michael Adams
 * @date 1/11/21
 */
public class StoredWorkflowsDialog extends Dialog {

    private static final String PANEL_WIDTH = "760px";
    private static final String GRID_HEIGHT = "300px";
    private static final String DESC_HEIGHT = "85px";


    private final TextArea descArea = createDescArea();
    private final VerticalLayout gridContainer = new VerticalLayout();
    private Button loadBtn;
    private final Grid<StoredWorkflow> privateGrid = createPrivateGrid();;
    private final Grid<StoredWorkflow> sharedGrid = createSharedGrid();
    private final Workflow parentWorkflow;
    private StoredWorkflow selectedWorkflow;

    public StoredWorkflowsDialog(Workflow workflow) {
        parentWorkflow = workflow;
        setCloseOnOutsideClick(false);
        setModal(true);
        setWidth("820px");
        setHeight("600px");
        add(new HorizontalLayout(leftPanel()));
        add(createButtons());
    }


    private VerticalLayout leftPanel() {
        VerticalLayout layout = new VerticalLayout();
        layout.add(createHeader());
        layout.add(createTabs());
        layout.add(initGridContainer());
        layout.add(descArea);
        return layout;
    }


    private H4 createHeader() {
        H4 header = new H4("Stored Workflows");
        UiUtil.removeTopMargin(header);
        return header;
    }


    private Tabs createTabs() {
        Tab privateTab = new Tab("Private");
        Tab sharedTab = new Tab("Shared");
        Tabs tabs = new Tabs(privateTab, sharedTab);
        tabs.setSelectedTab(privateTab);
        tabs.addSelectedChangeListener(event -> setTabContent(event.getSelectedTab()));
        return tabs;
    }


    private VerticalLayout initGridContainer() {
        gridContainer.setPadding(false);
        gridContainer.add(privateGrid);
        return gridContainer;
    }


    private void setTabContent(Tab selected) {
        descArea.setValue("");
        gridContainer.removeAll();
        if (selected.getLabel().equals("Private")) {
            privateGrid.deselectAll();
            gridContainer.add(privateGrid);
        }
        else {
            sharedGrid.deselectAll();
            gridContainer.add(sharedGrid);
        }
    }

    private Grid<StoredWorkflow> createPrivateGrid() {
        return createGrid(WorkflowStore.findPrivate("user"));
    }


    private Grid<StoredWorkflow> createSharedGrid() {
        return createGrid(WorkflowStore.findShared());
    }


    private Grid<StoredWorkflow> createGrid(List<StoredWorkflow> items) {
        Grid<StoredWorkflow> grid = new Grid<>(StoredWorkflow.class, false);
        grid.addColumn(StoredWorkflow::getName).setHeader("Name");
        grid.addColumn(StoredWorkflow::getOwner).setHeader("Owner");
        grid.addColumn(StoredWorkflow::getCreationTime).setHeader("Created");
        grid.addColumn(StoredWorkflow::getLastSavedTime).setHeader("Last Saved");
        grid.setItems(items);
        grid.setWidth(PANEL_WIDTH);
        grid.setHeight(GRID_HEIGHT);

        grid.addSelectionListener(selection -> {
            Optional<StoredWorkflow> workflow = selection.getFirstSelectedItem();
            if (workflow.isPresent()) {
                selectedWorkflow = workflow.get();
                String description = selectedWorkflow.getDescription();
                descArea.setValue(description);
                loadBtn.setEnabled(true);
            }
            else {
                selectedWorkflow = null;
                descArea.setValue("");
                loadBtn.setEnabled(false);
            }
        });

        return grid;
    }


    private TextArea createDescArea() {
        TextArea ta = new TextArea();
        ta.setLabel("Description");
        ta.setWidth(PANEL_WIDTH);
        ta.setHeight(DESC_HEIGHT);
        ta.setReadOnly(true);
        return ta;
    }

    private HorizontalLayout createButtons() {
        loadBtn = new Button("Load", event -> {
            load();
            close();
        });
        loadBtn.setEnabled(false);

        HorizontalLayout layout = new HorizontalLayout(loadBtn,
                new Button("Cancel", event -> close()));
        layout.setPadding(true);
        return layout;
    }


    private void load() {
        if (parentWorkflow.hasChanges()) {
            MessageDialog dialog = new MessageDialog(
                    "Store changes to existing workflow?");
            dialog.setText("Click 'Store' to save changes, " +
                    "'Discard' to discard changes, 'Cancel' to keep working.");

            dialog.addConfirmButton(new Button("Store", s -> {
                storeExistingWorkflow();
                loadSelected();
            }));

            dialog.addRejectButton(new Button("Discard", d -> loadSelected()));

            dialog.addCancelButton();
            dialog.open();
        }
        else {
            loadSelected();
        }
    }


    private void storeExistingWorkflow() {
        try {
            parentWorkflow.store();
        }
        catch (JSONException je) {
            Announcement.error("Failed to store workflow: " + je.getMessage());
        }
    }


    private void loadSelected() {
        parentWorkflow.fileLoaded(selectedWorkflow.getJson());
        Announcement.success("'" + selectedWorkflow.getName() + "' successfully loaded.");
    }

}
