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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 12/5/21
 */
public class Workspace {

    private final NodeRunner _runner = new NodeRunner(this);
    private final List<Node> _heads = new ArrayList<>();

    public NodeRunner getRunner() { return _runner; }

    public void addNode(Node node) { _heads.add(node); }

    public void removeNode(Node node) {
        Node previous = node.previous();
        if (previous != null) {
            disconnect(previous, node);
        }
        Node next = node.next();
        if (next != null) {
            disconnect(node, next);
        }
    }


    public void connect(Node source, Node target) {
        source.setNext(target);
        target.setPrevious(source);
        _heads.remove(target);
        if (source.hasOutput()) {
            target.addInput(source.getOutput());
        }
    }


    public void disconnect(Node source, Node target) {
        source.setNext(null);
        target.setPrevious(null);
        _heads.add(target);
        target.clearInput(source.getOutput());
    }


    public void reset() {
        for (Node head : _heads) {
            clearInputsInPath(head);
        }
    }


    private void clearInputsInPath(Node node) {
        node.clearInputs();
        if (node.hasNext()) clearInputsInPath(node.next());
    }

    
    public List<String> getNodeNamesInPath(Node nodeInPath) {
        return getNodeNamesInPath(new ArrayList<>(), getHead(nodeInPath));
    }

    private List<String> getNodeNamesInPath(List<String> names, Node node) {
        names.add(node.getName());
        if (node.hasNext()) getNodeNamesInPath(names, node.next());
        return names;
    }


    public Node getHead(Node node) {
        return node.isHead() ? node : getHead(node.previous());
    }

    public Node getTail(Node node) {
        return node.isTail() ? node : getTail(node.next());
    }

}
