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


import com.processdataquality.praeclarus.ui.repo.StoredWorkflow;
import com.processdataquality.praeclarus.ui.repo.WorkflowStore;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.Optional;

/**
 * @author Michael Adams
 * @date 1/11/21
 */
public class StoredWorkflowsDialog extends Dialog {

    private static final String PANEL_WIDTH = "360px";
    private static final String PANEL_HEIGHT = "450px";

    private final TextArea descArea = createDescArea();
    private Button loadBtn;

    public StoredWorkflowsDialog() {
        setCloseOnOutsideClick(false);
        setModal(true);
        setWidth("800px");
        setHeight("600px");
        add(new HorizontalLayout(leftPanel(), rightPanel()));
        add(createButtons());
    }


    private VerticalLayout leftPanel() {
        VerticalLayout layout = new VerticalLayout();
        layout.add(new H5("Stored Workflows"));
        layout.add(createGrid());
        return layout;
    }


    private VerticalLayout rightPanel() {
        VerticalLayout layout = new VerticalLayout();
        layout.add(new H5("Description"));
        layout.add(descArea);
        return layout;
    }


    private Grid<StoredWorkflow> createGrid() {
        Grid<StoredWorkflow> grid = new Grid<>(StoredWorkflow.class, false);
        grid.addColumn(StoredWorkflow::getOwner).setHeader("Owner");
        grid.addColumn(StoredWorkflow::isShared).setHeader("Shared");
        grid.setItems(WorkflowStore.findall());
        grid.setWidth(PANEL_WIDTH);
        grid.setHeight(PANEL_HEIGHT);

        grid.addSelectionListener(selection -> {
            Optional<StoredWorkflow> optWorkflow = selection.getFirstSelectedItem();
            if (optWorkflow.isPresent()) {
                String description = getDescription(optWorkflow.get().getJson());
                descArea.setValue(description);
                loadBtn.setEnabled(true);
            }
            else {
                descArea.setValue("");
                loadBtn.setEnabled(false);
            }
        });

        return grid;
    }


    private TextArea createDescArea() {
        TextArea ta = new TextArea();
        ta.setWidth(PANEL_WIDTH);
        ta.setHeight(PANEL_HEIGHT);
        return ta;
    }

    private HorizontalLayout createButtons() {
        loadBtn = new Button("Load", event -> {
            // load it
            close();
        });
        loadBtn.setEnabled(false);

        return new HorizontalLayout(loadBtn, new Button("Cancel", event -> close()));
    }


    private String getDescription(String jsonStr) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            String description = json.optString("description");
            if (description != null) {
                return description;
            }
        }
        catch (JSONException e) {
            //desc not found;
        }
        return "No description";
    }

}
