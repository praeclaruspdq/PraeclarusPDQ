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

import com.processdataquality.praeclarus.ui.canvas.CanvasPrimitive;
import com.processdataquality.praeclarus.ui.canvas.Vertex;
import com.processdataquality.praeclarus.ui.canvas.CanvasSelectionListener;
import com.processdataquality.praeclarus.ui.util.UiUtil;
import com.processdataquality.praeclarus.node.NodeRunner;
import com.processdataquality.praeclarus.node.Node;
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
        addButtons();
        _state = NodeRunner.RunnerState.IDLE;
        UiUtil.removeTopMargin(this);
        enable();
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

        // sets colour if enabled or gray if disabled
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


    protected void setState(NodeRunner.RunnerState state) {
        if (_state != state) {
            _state = state;
            enable();
        }
    }


    private void addButtons() {
        Icon runIcon = createIcon(VaadinIcon.PLAY, "green");
        _runButton = new Button(runIcon, e -> _runner.run(_selectedNode));
        _runButton.getElement().setAttribute("title", "Run");

        Icon stepIcon = createIcon(VaadinIcon.STEP_FORWARD, "blue");
        _stepButton = new Button(stepIcon, e -> _runner.step(_selectedNode));
        _stepButton.getElement().setAttribute("title", "Step fwd");

        Icon backIcon = createIcon(VaadinIcon.STEP_BACKWARD, "blue");
        _backButton = new Button(backIcon, e -> _runner.stepBack(_selectedNode));
        _backButton.getElement().setAttribute("title", "Step back");

        Icon stopIcon = createIcon(VaadinIcon.CLOSE_CIRCLE_O,"red");
        _stopButton = new Button(stopIcon, e -> _runner.reset());
        _stopButton.getElement().setAttribute("title", "Stop run");

        add(_runButton, _stepButton, _backButton, _stopButton);
    }


    private Icon createIcon(VaadinIcon vIcon, String colour) {
        Icon icon = vIcon.create();
        icon.setSize("24px");
        icon.setColor(colour);
        return icon;
    }


    private boolean canRunSelectedNode() {
        return ! (_selectedNode == null || _selectedNode.hasCompleted());
    }


    private boolean canStepBackSelectedNode() {
        return _selectedNode != null && _selectedNode.hasCompleted();
    }
    
}
