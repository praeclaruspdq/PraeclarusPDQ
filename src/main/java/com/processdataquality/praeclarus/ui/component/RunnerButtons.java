/*
 * Copyright (c) 2021-2022 Queensland University of Technology
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
import com.processdataquality.praeclarus.graph.GraphRunner;
import com.processdataquality.praeclarus.node.Node;
import com.processdataquality.praeclarus.ui.canvas.CanvasPrimitive;
import com.processdataquality.praeclarus.ui.canvas.CanvasSelectionListener;
import com.processdataquality.praeclarus.ui.canvas.Vertex;
import com.processdataquality.praeclarus.ui.component.announce.Announcement;
import com.processdataquality.praeclarus.ui.util.UiUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Adams
 * @date 14/5/21
 */
public class RunnerButtons extends Div implements CanvasSelectionListener {

	private static final Logger LOG = LoggerFactory.getLogger(RunnerButtons.class);

	private final GraphRunner _runner;

	private GraphRunner.RunnerState _state;
	private Node _selectedNode;


	private Button _runButton;
	private Button _stepButton;
	private Button _backButton;
	private Button _stopButton;


    private final Span _label = new Span();


    public RunnerButtons(GraphRunner runner) {
        _runner = runner;
        _state = GraphRunner.RunnerState.IDLE;
        createButtons();
        UiUtil.removeTopMargin(this);
        setWidthFull();
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

	public void addSeparator() {
		add(new Label("      "));
	}
	
	private boolean canStepBackSelectedNode() {
		return _selectedNode != null && _selectedNode.hasCompleted();
	}

	protected void setState(GraphRunner.RunnerState state) {
		if (_state != state) {
			_state = state;
			enable();
		}
	}


	private void action(GraphRunner.RunnerAction runnerAction) {
		try {
			_runner.action(runnerAction, _selectedNode);
			Announcement.success(_selectedNode.getLabel() + " completed successfully");
		} catch (NodeRunnerException e) {
			try {
				_selectedNode.reset();
			} catch (Exception ex) {
				// unlikely this will happen
			}
			_runner.reset();
			String msg = "Error in node '" + _selectedNode.getLabel() + "': " + e.getMessage() + ";";
			if (e.getCause() != null) {
				msg = msg + " Caused by: " + e.getCause().getMessage();
			}
			Announcement.error(msg);
			LOG.error(msg, e);
		}
	}

    public void addLabel() {
        _label.getStyle().set("margin-left", "30px");
        _label.getStyle().set("font-style", "italic");
        add(_label);
    }

    public void setLabel(String text) {
        clearLabel();
        _label.add(text);
    }

    public void clearLabel() { _label.removeAll(); }



    private void createButtons() {
        _runButton = createButton(VaadinIcon.PLAY,
                GraphRunner.RunnerAction.RUN,"Run");
        _stepButton = createButton(VaadinIcon.STEP_FORWARD,
                GraphRunner.RunnerAction.STEP, "Step fwd");
        _backButton = createButton(VaadinIcon.STEP_BACKWARD,
                GraphRunner.RunnerAction.STEP_BACK, "Step back");
        _stopButton = createButton(VaadinIcon.STOP,
                GraphRunner.RunnerAction.STOP, "Stop");

        add(_runButton, _stepButton, _backButton, _stopButton);
    }


    private Button createButton(VaadinIcon icon, GraphRunner.RunnerAction runnerAction,
                                String tooltip)  {
        return UiUtil.createToolButton(icon, tooltip, false,
                e -> action(runnerAction));
    }


    private boolean canRunSelectedNode() {
        return ! (_selectedNode == null || _selectedNode.hasCompleted());
    }




}