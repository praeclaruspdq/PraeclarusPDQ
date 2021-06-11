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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 12/5/21
 */
public class Workspace {

    private final NodeRunner _runner = new NodeRunner();
    private final List<Node> _heads = new ArrayList<>();


    public void clear() {
        _heads.clear();
        _runner.reset();
    }

    public NodeRunner getRunner() { return _runner; }

    public void addNode(Node node) { _heads.add(node); }

    public void removeNode(Node node) {
        node.previous().forEach(previous -> disconnect(previous, node));
        node.next().forEach(next -> disconnect(node, next));
    }


    public void connect(Node source, Node target) {
        source.addNext(target);
        target.addPrevious(source);
        _heads.remove(target);
    }


    public void disconnect(Node source, Node target) {
        source.removeNext(target);
        target.removePrevious(source);
        _heads.add(target);
    }


    public void reset() { _runner.abort(); }


    public Set<Node> getHeads(Node node) {
        Set<Node> heads = new HashSet<>();
        for (Node previous : node.previous()) {
            if (previous.isHead()) {
                heads.add(previous);
            }
            else heads.addAll(getHeads(previous));
        }
        if (heads.isEmpty()) heads.add(node);
        return heads;
    }

    public Set<Node> getTails(Node node) {
        Set<Node> tails = new HashSet<>();
        for (Node next : node.next()) {
            if (next.isTail()) {
                tails.add(next);
            }
            else tails.addAll(getTails(next));
        }
        if (tails.isEmpty()) tails.add(node);
        return tails;
   }

}
