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

package com.processdataquality.praeclarus.node;

import com.processdataquality.praeclarus.exception.NodeRunnerException;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 25/5/21
 */
public class NodeRunner implements NodeStateListener {

    public enum RunnerState { RUNNING, STEPPING, IDLE }
    public enum RunnerAction { RUN, STEP, STEP_BACK, RESUME }

    private RunnerState _runnerState = RunnerState.IDLE;
    private final Set<NodeRunnerListener> _listeners = new HashSet<>();
    private Node _stepToNode = null;

    public NodeRunner() { }

    
    public void addListener(NodeRunnerListener listener) {
        _listeners.add(listener);
    }

    public boolean removeListener(NodeRunnerListener listener) {
        return _listeners.remove(listener);
    }

    @Override
    public void nodeStateChanged(Node node) throws Exception {
        switch (node.getState()) {
            case COMPLETED: complete(node); break;
            case PAUSED: announceNodePaused(node); break;
        }
    }

    
    private void step(Node node) throws NodeRunnerException {
        _stepToNode = node;
        launch(node, RunnerState.STEPPING);
    }


    private void run(Node node) throws NodeRunnerException {
        launch(node, RunnerState.RUNNING);
    }


    private void resume(Node node) throws NodeRunnerException {
        try {
            node.run();          // pattern node - run() will call part 2 of node's run cmd
        }
        catch (Throwable t) {
            throw new NodeRunnerException(t.getMessage(), t.getCause());
        }
    }


    private void stepBack(Node node) throws NodeRunnerException {
        try {
            rollbackAllNext(node);        // all subsequent nodes must also roll back
            node.reset();
        }
        catch (Throwable t) {
            throw new NodeRunnerException(t.getMessage(), t.getCause());
        }
    }


    public void action(RunnerAction runnerAction, Node node) throws NodeRunnerException {
        switch (runnerAction) {
            case RUN : run(node); break;
            case STEP : step(node); break;
            case STEP_BACK : stepBack(node); break;
            case RESUME : resume(node); break;
        }
    }


    public void reset() {
        setState(RunnerState.IDLE);
        _stepToNode = null;
    }


    public RunnerState getState() { return _runnerState; }


    private void launch(Node node, RunnerState launchState) throws NodeRunnerException {
        if (node == null) {
            throw new NodeRunnerException("No node selected to run");
        }

        if (_runnerState == RunnerState.IDLE) {
            setState(launchState);
        }
        else throw new NodeRunnerException("Workflow is already running");

        try {
            // check all previous have completed
            if (node.hasCompleted()) {
                runNext(node);
            }
            else if (completeAllPrevious(node)) {
                start(node);
            }
        }
        catch (Exception e) {
            throw new NodeRunnerException(e.getMessage(), e.getCause());
        }
    }


    private boolean completeAllPrevious(Node node) throws Exception {
        for (Node previous : node.previous()) {
            if (completeAllPrevious(previous)) {
                if (previous.canStart()) {
                    start(previous);
                    return false;
                }
                else if (!previous.hasCompleted()) {
                    return false;
                }
            }
        }
        return true;
    }


    private void rollbackAllNext(Node node) throws Exception {
        for (Node next : node.next()) {
            if (next.hasCompleted()) {
                next.reset();
                rollbackAllNext(next);
            }
        }
    }

    private void start(Node node) throws Exception {
        node.addStateListener(this);
        node.runPreTask();
        node.run();
    }


    private void complete(Node node) throws Exception {
        node.runPostTask();
        
        switch (_runnerState) {
            case STEPPING: if (node == _stepToNode) {
                reset();
                break;
            } 
            case RUNNING: runNext(node); break;
            case IDLE: node.reset(); break;
        }
    }


    private void runNext(Node node) throws Exception {
        if (node.hasNext()) {
            for (Node next : node.next()) {
                if (next.canStart() && completeAllPrevious(next)) {
                    start(next);
                }
            }
        }
        else {
            reset();
        }
    }


    private void setState(RunnerState state) {
        _runnerState = state;
        announceStateChanged(_runnerState);
    }

    
    private void announceStateChanged(RunnerState state) {
        _listeners.forEach(l -> l.runnerStateChanged(state));
    }


    private void announceNodePaused(Node node) {
        _listeners.forEach(l -> l.runnerNodePaused(node));
    }

}