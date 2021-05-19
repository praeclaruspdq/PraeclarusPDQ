package com.processdataquality.praeclarus.ui.component;

import com.processdataquality.praeclarus.workspace.Workspace;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.tablesaw.api.Table;

import java.util.List;

/**
 * @author Michael Adams
 * @date 14/5/21
 */
public class RunnerButtons extends Div {

    private enum State { RUNNING, STEPPING, IDLE }

    private final Workspace _workspace;
    private final ResultsPanel _resultsPanel;

    private State _state;
    private Button _runButton;
    private Button _stepButton;
    private Button _backButton;
    private Button _stopButton;


    public RunnerButtons(Workspace workspace, ResultsPanel resultsPanel) {
        _workspace = workspace;
        _resultsPanel = resultsPanel;
        addButtons();
        _state = State.IDLE;
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
                _runButton.setEnabled(_workspace.canRun());
                _stepButton.setEnabled(_workspace.canStep());
                _backButton.setEnabled(_workspace.canStepBack());
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

    private void setState(State state) { _state = state; }

    private void addButtons() {
        Icon runIcon = createIcon(VaadinIcon.PLAY, "green");
        _runButton = new Button(runIcon, e -> {
            setState(State.RUNNING);
            _workspace.restart();
            List<Table> tables = _workspace.run();
            List<String> titles = _workspace.getNodeNames();
            _resultsPanel.addResults(titles, tables);
            setState(State.IDLE);
        });

        Icon stepIcon = createIcon(VaadinIcon.STEP_FORWARD, "blue");
        _stepButton = new Button(stepIcon, e -> {
            setState(State.STEPPING);
            Table table = _workspace.step();
            String title = _workspace.getLastNode().getName();
            _resultsPanel.addResult(title, table);
            setState(State.IDLE);
        });

        Icon backIcon = createIcon(VaadinIcon.STEP_BACKWARD, "blue");
        _backButton = new Button(backIcon, e -> {
            setState(State.STEPPING);
            String title = _workspace.getLastNode().getName();
            _resultsPanel.removeResult(title);
            _workspace.back();
            setState(State.IDLE);
        });

        Icon stopIcon = createIcon(VaadinIcon.CLOSE_CIRCLE_O,"red");
        _stopButton = new Button(stopIcon, e -> {
            _workspace.reset();
            setState(State.IDLE);
        });
        
        add(_runButton, _stepButton, _backButton, _stopButton);
    }


    private Icon createIcon(VaadinIcon vIcon, String colour) {
        Icon icon = vIcon.create();
        icon.setSize("24px");
        icon.setColor(colour);
        return icon;
    }

}
