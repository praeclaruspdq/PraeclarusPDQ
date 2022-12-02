/*
 * Copyright (c) 2022 Queensland University of Technology
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

package com.processdataquality.praeclarus.ui.component.dialog;


import com.processdataquality.praeclarus.logging.EventLogger;
import com.processdataquality.praeclarus.ui.canvas.Workflow;
import com.processdataquality.praeclarus.ui.component.announce.Announcement;
import com.processdataquality.praeclarus.ui.component.layout.JustifiedButtonLayout;
import com.processdataquality.praeclarus.ui.repo.StoredWorkflow;
import com.processdataquality.praeclarus.ui.repo.WorkflowStore;
import com.processdataquality.praeclarus.ui.util.UiUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * @author Michael Adams
 * @date 1/11/21
 */
public class StoredWorkflowsDialog extends Dialog {

    private static final Logger LOG = LoggerFactory.getLogger(StoredWorkflowsDialog.class);

    private static final String PANEL_WIDTH = "760px";
    private static final String GRID_HEIGHT = "300px";
    private static final String DESC_HEIGHT = "85px";

    private final Tabs tabs = createTabs();
    private final TextArea descArea = createDescArea();
    private final VerticalLayout gridContainer = new VerticalLayout();
    private Button loadBtn;
    private Button deleteBtn;
    private final Grid<StoredWorkflow> privateGrid = createPrivateGrid();
    private final Grid<StoredWorkflow> sharedGrid = createSharedGrid();
    private final Workflow parentWorkflow;
    private StoredWorkflow selectedWorkflow;


    public StoredWorkflowsDialog(Workflow workflow) {
        parentWorkflow = workflow;
        setCloseOnOutsideClick(false);
        setModal(true);
        setWidth("820px");
        setHeight("600px");
        add(new HorizontalLayout(createPanel()));
        add(createButtons());
    }


    private VerticalLayout createPanel() {
        VerticalLayout layout = new VerticalLayout();
        layout.add(createHeader());
        layout.add(tabs);
        layout.add(initGridContainer());
        layout.add(descArea);
        return layout;
    }


    private H3 createHeader() {
        H3 header = new H3("Stored Workflows");
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
        return createGrid(WorkflowStore.findPrivate(EventLogger.loggedOnUserName()));
    }


    private Grid<StoredWorkflow> createSharedGrid() {
        return createGrid(WorkflowStore.findPublic());
    }


    private Grid<StoredWorkflow> createGrid(List<StoredWorkflow> items) {
        Grid<StoredWorkflow> grid = new Grid<>(StoredWorkflow.class, false);
        grid.addColumn(StoredWorkflow::getName).setHeader("Name").setSortable(true);
        grid.addColumn(StoredWorkflow::getOwner).setHeader("Owner").setSortable(true);
        grid.addColumn(StoredWorkflow::getCreationTime).setHeader("Created").setSortable(true);
        grid.addColumn(StoredWorkflow::getLastSavedTime).setHeader("Last Saved").setSortable(true);
        grid.setItems(items);
        grid.setWidth(PANEL_WIDTH);
        grid.setHeight(GRID_HEIGHT);

        grid.addSelectionListener(selection -> {
            Optional<StoredWorkflow> workflow = selection.getFirstSelectedItem();
            if (workflow.isPresent()) {
                selectedWorkflow = workflow.get();
                String description = selectedWorkflow.getDescription();
                descArea.setValue(description);
                enableButtons(true);
            }
            else {
                selectedWorkflow = null;
                descArea.setValue("");
                enableButtons(false);
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
        loadBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loadBtn.setEnabled(false);

        deleteBtn = new Button("Delete", event -> {
            deleteSelected();
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteBtn.setEnabled(false);

        Button closeBtn = new Button("Close", event -> close());
        closeBtn.getStyle().set("margin-inline-end", "auto");

        HorizontalLayout layout = new JustifiedButtonLayout(closeBtn, deleteBtn, loadBtn);
        layout.setPadding(true);
        return layout;
    }


    private void enableButtons(boolean enable) {
        loadBtn.setEnabled(enable);
        deleteBtn.setEnabled(enable);
    }


    private void clearSelection() {
        selectedWorkflow = null;
        descArea.setValue("");
        enableButtons(false);
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
        parentWorkflow.store();
    }


    private void loadSelected() {
        parentWorkflow.fileLoaded(selectedWorkflow.getWorkflowJson());
        Announcement.success("'" + selectedWorkflow.getName() + "' successfully loaded.");
        EventLogger.graphLoadEvent(parentWorkflow.getGraph());
    }

    
    private void deleteSelected() {
        String confirmMsg = "Please confirm you want to delete the selected workflow from storage. " +
                "This action cannot be undone.";
        YesNoDialog dialog = new YesNoDialog("Confirm Delete?", confirmMsg);

        dialog.getYesButton().addClickListener(e -> {
            WorkflowStore.delete(selectedWorkflow);
            getGridOnView().getListDataView().removeItem(selectedWorkflow);
            EventLogger.graphDiscardedEvent(selectedWorkflow.getId(),
                    selectedWorkflow.getName());
            clearSelection();
        });
        
        dialog.open();
    }


    private Grid<StoredWorkflow> getGridOnView() {
        if (tabs.getSelectedTab().getLabel().equals("Private")) {
            return privateGrid;
        }
        else return sharedGrid;
    }
    
}
