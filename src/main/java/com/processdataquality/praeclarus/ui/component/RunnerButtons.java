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

import com.processdataquality.praeclarus.exception.NodeRunnerException;
import com.processdataquality.praeclarus.node.Node;
import com.processdataquality.praeclarus.node.NodeRunner;
import com.processdataquality.praeclarus.ui.canvas.CanvasPrimitive;
import com.processdataquality.praeclarus.ui.canvas.CanvasSelectionListener;
import com.processdataquality.praeclarus.ui.canvas.Vertex;
import com.processdataquality.praeclarus.ui.util.UiUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * @author Michael Adams
 * @date 14/5/21
 */
public class RunnerButtons extends Div implements CanvasSelectionListener {

    private final NodeRunner _runner;

    private NodeRunner.RunnerState _state;
    private Node _selectedNode;
    
    private Button _runButton;
    private Button _stepButton;
    private Button _backButton;
    private Button _stopButton;


    public RunnerButtons(NodeRunner runner) {
        _runner = runner;
        _state = NodeRunner.RunnerState.IDLE;
        createButtons();
        UiUtil.removeTopMargin(this);
    }


    @Override
    public void canvasSelectionChanged(CanvasPrimitive selected) {
        _selectedNode = (selected instanceof Vertex) ? ((Vertex) selected).getNode() : null;
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
                _runButton.setEnabled(_selectedNode != null);
                _stepButton.setEnabled(canRunSelectedNode());
                _backButton.setEnabled(canStepBackSelectedNode());
                _stopButton.setEnabled(false);
                break;
            }
        }
    }


    public void addButton(Button b) {
        add(b);
    }


    protected void setState(NodeRunner.RunnerState state) {
        if (_state != state) {
            _state = state;
            enable();
        }
    }


    private void createButtons() {
        _runButton = createButton(VaadinIcon.PLAY,
                NodeRunner.RunnerAction.RUN,"Run");
        _stepButton = createButton(VaadinIcon.STEP_FORWARD,
                NodeRunner.RunnerAction.STEP, "Step fwd");
        _backButton = createButton(VaadinIcon.STEP_BACKWARD,
                NodeRunner.RunnerAction.STEP_BACK, "Step back");
        _stopButton = createButton(VaadinIcon.STOP,
                NodeRunner.RunnerAction.STOP, "Stop");

        add(_runButton, _stepButton, _backButton, _stopButton);
    }


    private Button createButton(VaadinIcon icon, NodeRunner.RunnerAction runnerAction,
                                String tooltip)  {
        Icon runIcon = UiUtil.createIcon(icon);
        Button button = new Button(runIcon, e -> action(runnerAction));
        UiUtil.setTooltip(button, tooltip);
        button.setEnabled(false);
        return button;
    }


    private boolean canRunSelectedNode() {
        return ! (_selectedNode == null || _selectedNode.hasCompleted());
    }


    private boolean canStepBackSelectedNode() {
        return _selectedNode != null && _selectedNode.hasCompleted();
    }


    private void action(NodeRunner.RunnerAction runnerAction) {
        try {
            _runner.action(runnerAction, _selectedNode);
        }
        catch (NodeRunnerException e) {
            try {
                _selectedNode.reset();
            }
            catch (Exception ex) {
                // unlike this will happen
            }
            _runner.reset();
            String msg = "Error in node '" + _selectedNode.getLabel()  + "': " +  e.getMessage();
            new ErrorMsg(msg).open();
        }
    }

}
