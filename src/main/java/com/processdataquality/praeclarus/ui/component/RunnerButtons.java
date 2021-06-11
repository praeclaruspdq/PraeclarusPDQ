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
import com.processdataquality.praeclarus.workspace.NodeRunner;
import com.processdataquality.praeclarus.workspace.Workspace;
import com.processdataquality.praeclarus.workspace.node.Node;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;

/**
 * @author Michael Adams
 * @date 14/5/21
 */
public class RunnerButtons extends Div {

    private final Workspace _workspace;
    private final Workflow _workflow;

    private NodeRunner.State _state;
    
    private Button _runButton;
    private Button _stepButton;
    private Button _backButton;
    private Button _stopButton;


    public RunnerButtons(Workspace workspace, Workflow workflow) {
        _workspace = workspace;
        _workflow = workflow;
        addButtons();
        _state = NodeRunner.State.IDLE;
        getElement().getStyle().set("top-margin", "0");
        enable();
    }


    public void enable() {
        switch (_state) {
            case RUNNING:
            case STEPPING: {
                _runButton.setEnabled(false);
                _stepButton.setEnabled(false);
                _backButton.setEnabled(false);
                _stopButton.setEnabled(true);
                break;
            }
            case IDLE: {
                Node node = _workflow.getSelectedNode();
                _runButton.setEnabled(node != null);
                _stepButton.setEnabled(node != null);
                _backButton.setEnabled(node != null && node.hasPrevious());
                _stopButton.setEnabled(false);
                break;
            }
        }
        setIconColor(_runButton, "green");
        setIconColor(_stepButton, "blue");
        setIconColor(_backButton, "blue");
        setIconColor(_stopButton, "red");
    }


    private void setIconColor(Button b, String color) {
        ((Icon)b.getIcon()).setColor(b.isEnabled() ? color : "gray");
    }

    public void addButton(Button b) {
        add(b);
    }

    private void setState(NodeRunner.State state) { _state = state; }

    private NodeRunner.State getState() { return _state; }

    private void addButtons() {
        Icon runIcon = createIcon(VaadinIcon.PLAY, "green");
        _runButton = new Button(runIcon, e -> {
            Node node = _workflow.getSelectedNode();
            if (node != null) {
                setState(NodeRunner.State.RUNNING);
                runNode(node);
            }
        });

        Icon stepIcon = createIcon(VaadinIcon.STEP_FORWARD, "blue");
        _stepButton = new Button(stepIcon, e -> {
            Node node = _workflow.getSelectedNode();
            if (node != null) {
                setState(NodeRunner.State.STEPPING);
                runNode(node);
            }
        });

        Icon backIcon = createIcon(VaadinIcon.STEP_BACKWARD, "blue");
        _backButton = new Button(backIcon, e -> {
            Node node = _workflow.getSelectedNode();
            if (node != null) {
                setState(NodeRunner.State.STEPPING);
                _workspace.getRunner().stepBack(node);
                setState(NodeRunner.State.IDLE);
            }
        });

        Icon stopIcon = createIcon(VaadinIcon.CLOSE_CIRCLE_O,"red");
        _stopButton = new Button(stopIcon, e -> {
            _workspace.reset();
            setState(NodeRunner.State.IDLE);
        });
        
        add(_runButton, _stepButton, _backButton, _stopButton);
    }


    private Icon createIcon(VaadinIcon vIcon, String colour) {
        Icon icon = vIcon.create();
        icon.setSize("24px");
        icon.setColor(colour);
        return icon;
    }


    private void runNode(Node node) {
        try {
            if (getState() == NodeRunner.State.RUNNING) {
                _workspace.getRunner().run(node);
            }
            else if (getState() == NodeRunner.State.STEPPING) {
                _workspace.getRunner().step(node);
            }
        }
        catch (IllegalArgumentException iae) {
            Notification.show(iae.getMessage());
        }
        finally {
            setState(NodeRunner.State.IDLE);
//            Node lastCompleted = _workspace.getRunner().getLastCompletedNode();
//            if (lastCompleted != null) {
//                _workflow.setSelectedNode(lastCompleted);
//            }
        }

    }

}
