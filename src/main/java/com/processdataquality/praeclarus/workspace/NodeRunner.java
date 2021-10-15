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

import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 25/5/21
 */
public class NodeRunner {

    public enum State { RUNNING, STEPPING, ABORTED, IDLE }

    private Node _lastCompletedNode;
    private State _state = State.IDLE;
    private final Set<NodeRunnerListener> _listeners = new HashSet<>();

    public NodeRunner() { }
    
    public void addListener(NodeRunnerListener listener) {
        _listeners.add(listener);
    }

    public boolean removeListener(NodeRunnerListener listener) {
        return _listeners.remove(listener);
    }


    public void run(Node node) {
        if (_state == State.ABORTED) {
            reset();
            return;
        }
        _state = State.RUNNING;
        node = step(node);
        if (node.hasCompleted()) {
            if (node.hasNext()) {
                node.next().forEach(this::run);
            }
            else {
                _state = State.IDLE;
            }
        }
        else {
            announceNodePaused(node);
        }
    }


    /**
     * Runs the specified node, and any unrun nodes preceding it
     * @param node the node to run
     * @return the node passed in, or a previous unrun pattern node that is incomplete
     */
    public Node step(Node node) {
        if (_state == State.IDLE) {
            _state = State.STEPPING;
        }

        // need all previous nodes to have run before this one can
        for (Node previous : node.previous()) {
            if (!previous.hasOutput()) {
                step(previous);
                if (!previous.hasCompleted()) return previous;   // 2-part pattern node
            }
        }
        if (node.runPreTask()) {                   // lets UIs do any pre-work necessary
            announceNodeStarted(node);
            node.run();
            if (node.hasCompleted()) {
                node.runPostTask();                    // lets UIs do any post-work necessary
                setLastCompletedNode(node);
                if (_state == State.STEPPING) {
                    _state = State.IDLE;
                }
            }
        }
        return node;
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
        node.reset();
        _lastCompletedNode = node.isHead() ? null : node.previous().iterator().next();
        announceNodeRollback(node);
    }

    
    public void reset() {
        setLastCompletedNode(null);
        _state = State.IDLE;
    }

    public void abort() {
        _state = State.ABORTED;
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

    private void announceNodePaused(Node node) {
         _listeners.forEach(l -> l.nodePaused(node));
    }

    private void announceNodeRollback(Node node) {
         _listeners.forEach(l -> l.nodeRollback(node));
    }

}
