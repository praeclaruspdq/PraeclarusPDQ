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
import tech.tablesaw.api.Table;

/**
 * @author Michael Adams
 * @date 25/5/21
 */
public class NodeRunner {

    public enum State { RUNNING, STEPPING, IDLE }

    private final Workspace _workspace;
    private Node _lastCompletedNode;
    private State _state = State.IDLE;


    public NodeRunner(Workspace workspace) {
        _workspace = workspace;
    }

    public void run(Node node) {
        _state = State.RUNNING;
        while (node != null) {
            step(node);
            if (! node.hasCompleted()) {
                break;
            }
            node = node.next();
        }
    }


    public void step(Node node) {
        _state = State.STEPPING;
        node.run();
        if (node.hasCompleted()) {
            _lastCompletedNode = node;
            if (node.hasNext()) {
                node.next().addInput(node.getOutput());
            }
            else if (node.hasCompleted()) {
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


    public void back(Node node) {
        Table output = node.getOutput();
        if (output != null && node.hasNext()) {
            node.next().clearInput(output);
        }
        node.reset();
        _lastCompletedNode = node.hasPrevious() ? node.previous() : null;
    }


    public void restart(Node node) {
        _workspace.reset();
        run(_workspace.getHead(node));
    }


    public void reset() {
        _lastCompletedNode = null;
        _state = State.IDLE;
    }


    public Node getLastCompletedNode() { return _lastCompletedNode; }

}
