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

import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 25/5/21
 */
public class NodeRunner implements NodeStateListener {

    public enum RunnerState { RUNNING, STEPPING, IDLE }

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
    public void nodeStateChanged(Node node) {
        switch (node.getState()) {
            case COMPLETED: complete(node); break;
            case PAUSED: announceNodePaused(node); break;
        }
    }

    
    public void step(Node node) {
        _stepToNode = node;
        launch(node, RunnerState.STEPPING);
    }


    public void run(Node node) {
        launch(node, RunnerState.RUNNING);
    }


    public void resume(Node node) {
        node.run();          // pattern node - run() will call part 2 of node's run cmd
    }


    public void stepBack(Node node) {
        rollbackAllNext(node);        // all subsequent nodes must also roll back
        node.reset();
    }


    public void reset() {
        setState(RunnerState.IDLE);
    }


    public RunnerState getState() { return _runnerState; }


    private void launch(Node node, RunnerState launchState) {
        if (node == null) return;  // error - no node

        if (_runnerState == RunnerState.IDLE) {
            setState(launchState);
        }
        // else error already in stepping/running

        // check all previous have completed
        if (completeAllPrevious(node)) {
            start(node);
        }
    }


    private boolean completeAllPrevious(Node node) {
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


    private void rollbackAllNext(Node node) {
        for (Node next : node.next()) {
            if (next.hasCompleted()) {
                next.reset();
                rollbackAllNext(next);
            }
        }
    }

    private void start(Node node) {
        node.addStateListener(this);
        node.runPreTask();
        node.run();
    }


    private void complete(Node node) {
        node.runPostTask();
        node.removeStateListener(this);
        
        switch (_runnerState) {
            case STEPPING: if (node == _stepToNode) {
                _stepToNode = null;
                setState(RunnerState.IDLE);
                break;
            } 
            case RUNNING: runNext(node); break;
            case IDLE: node.reset(); break;
        }
    }


    private void runNext(Node node) {
        if (node.hasNext()) {
            for (Node next : node.next()) {
                if (completeAllPrevious(next)) {
                    start(next);
                }
            }
        }
        else {
            setState(RunnerState.IDLE);
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
