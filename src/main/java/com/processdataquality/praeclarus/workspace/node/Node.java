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

package com.processdataquality.praeclarus.workspace.node;

import com.processdataquality.praeclarus.annotations.PluginMetaData;
import com.processdataquality.praeclarus.plugin.PDQPlugin;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 12/5/21
 */
public abstract class Node {

    private final Set<Node> _next;
    private final Set<Node> _previous;
    private List<Table> _inputs;
    private Table _output;
    private final PDQPlugin _plugin;
    private boolean _completed = false;


    protected Node(PDQPlugin plugin) {
        _plugin = plugin;
        _next = new HashSet<>();
        _previous = new HashSet<>();
    }

    
    public abstract void run();


    public Set<Node> next() {return _next; }

    public void addNext(Node node) { _next.add(node); }

    public boolean removeNext(Node node) { return _next.remove(node); }

    public boolean hasNext() { return !isTail(); }


    public Set<Node> previous() { return _previous; }

    public void addPrevious(Node node) { _previous.add(node); }

    public boolean removePrevious(Node node) { return _previous.remove(node); }
    
    public boolean hasPrevious() { return !isHead(); }


    public boolean isHead() { return _previous.isEmpty(); }

    public boolean isTail() { return _next.isEmpty(); }


    // to be overridden as required
    public boolean hasCompleted() { return _completed; }

    protected void setCompleted(boolean b) { _completed = b; }


    public PDQPlugin getPlugin() { return _plugin; }

    
    public String getName() {
        PluginMetaData metaData = getPlugin().getClass().getAnnotation(PluginMetaData.class);
        return metaData.name();
    }


    public boolean allowsInput() {
        return getPlugin().getMaxInputs() > _previous.size();
    }


    public boolean allowsOutput() {
        return getPlugin().getMaxOutputs() > _next.size();
    }


//    public void addInput(Table t) {
//        int allowedInputs = getPlugin().getMaxInputs();
//        if (allowedInputs <= 0) {
//            throw new IllegalArgumentException("This node does not accept inputs");
//        }
//        if (_inputs == null) _inputs = new ArrayList<>();
//        if (_inputs.size() == allowedInputs) {
//            throw new IllegalArgumentException("Maximum number of inputs already met");
//        }
//        _inputs.add(t) ;
//    }
//
//
//    public void clearInputs() { if (_inputs != null) _inputs.clear(); }
//
//    public void clearInput(Table table) {
//        if (! (_inputs == null || table == null)) {
//            _inputs.remove(table);
//        }
//    }
//
//    public Table getInput() { return getInput(0); }
//
//    public Table getInput(int index) {
//        if (_inputs == null || _inputs.size() == index) {
//            throw new IllegalArgumentException("Inputs list is empty");
//        }
//        if (_inputs.size() < index + 1) {
//            throw new IllegalArgumentException("Index is out of bounds");
//        }
//        return _inputs.get(index);
//    }
//
    public List<Table> getInputs() {
        List<Table> inputs = new ArrayList<>();
        _previous.forEach(node -> {
            if (node.getOutput() != null) inputs.add(node.getOutput());
        });
        return inputs;
    }

    public void reset() {
        clearOutput();
        _completed = false;
    }

    public Table getOutput() { return _output; }

    protected void setOutput(Table t) { _output = t; }

    public boolean hasOutput() { return _output != null; }

    public Table clearOutput() {
        Table table = _output;
        _output = null;
        return table;
    }


}
