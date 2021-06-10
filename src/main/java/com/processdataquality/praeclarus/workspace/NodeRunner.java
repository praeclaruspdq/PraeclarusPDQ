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

package com.processdataquality.praeclarus.workspace;

import com.processdataquality.praeclarus.workspace.node.Node;
import com.processdataquality.praeclarus.workspace.node.NodeRunnerListener;
import tech.tablesaw.api.Table;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 25/5/21
 */
public class NodeRunner {

    public enum State { RUNNING, STEPPING, IDLE }

    private final Workspace _workspace;
    private Node _lastCompletedNode;
    private State _state = State.IDLE;
    private final Set<NodeRunnerListener> _listeners = new HashSet<>();


    public NodeRunner(Workspace workspace) {
        _workspace = workspace;
    }


    public void addListener(NodeRunnerListener listener) {
        _listeners.add(listener);
    }

    public boolean removeListener(NodeRunnerListener listener) {
        return _listeners.remove(listener);
    }


    public void run(Node node) {
        _state = State.RUNNING;
        while (node != null) {
            step(node);
            if (! node.hasCompleted()) {
                break;
            }
            node = node.next();
            if (node == null) _state = State.IDLE;
        }
    }


    public void step(Node node) {
        if (_state == State.IDLE) {
            _state = State.STEPPING;
        }
        announceNodeStarted(node);
        node.run();
        if (node.hasCompleted()) {
            setLastCompletedNode(node);
            if (node.hasNext()) {
                node.next().addInput(node.getOutput());
            }
            if (_state == State.STEPPING) {
                _state = State.IDLE;
            }
        }
    }


    public void resume(Node node) {
        if (_state == State.RUNNING) {
            run(node);
        }
        else if (_state == State.STEPPING) {
            step(node);
        }
        // else error
    }


    public void stepBack(Node node) {
        Table output = node.getOutput();
        if (output != null && node.hasNext()) {
            node.next().clearInput(output);
        }
        node.reset();
        setLastCompletedNode(node.hasPrevious() ? node.previous() : null);
    }


    public void restart(Node node) {
        _workspace.reset();
        run(_workspace.getHead(node));
    }


    public void reset() {
        setLastCompletedNode(null);
        _state = State.IDLE;
    }


    public Node getLastCompletedNode() { return _lastCompletedNode; }

    public State getState() { return _state; }


    private void setLastCompletedNode(Node node) {
        _lastCompletedNode = node;
        if (node != null) {
            announceNodeCompletion(node);
        }
    }

    private void announceNodeCompletion(Node node) {
        _listeners.forEach(l -> l.nodeCompleted(node));
    }

    private void announceNodeStarted(Node node) {
        _listeners.forEach(l -> l.nodeStarted(node));
    }


}
