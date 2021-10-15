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

import com.processdataquality.praeclarus.annotations.Plugin;
import com.processdataquality.praeclarus.plugin.PDQPlugin;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A node in a workflow, representing a plugin. This class provides base functionality
 * to be overridden by sub-classes.
 *
 * @author Michael Adams
 * @date 12/5/21
 */
public abstract class Node {

    private final Set<Node> _next;      // set of immediate target nodes for this node
    private final Set<Node> _previous;  // set of immediate source nodes for this node
    private Table _output;              // a table with the result of running this plugin
    private final PDQPlugin _plugin;
    private NodeTask _preTask;          // optional code to run before plugin is run
    private NodeTask _postTask;         // optional code to run after plugin is run
    private boolean _completed = false;


    protected Node(PDQPlugin plugin) {
        _plugin = plugin;
        _next = new HashSet<>();
        _previous = new HashSet<>();
    }


    /**
     * Implemented by subclasses to run a plugin's algorithm
     */
    public abstract void run();


    /**
     * Allows UIs to run any preliminary code before the plugin's run
     * @param task the code to run
     */
    public void setPreTask(NodeTask task) { _preTask = task; }


    /**
     * Runs a pre-task, if one is defined
     * @return true if the run was successful or the task hasn't been set
     */
    public boolean runPreTask() {
        return _preTask == null || _preTask.run(this);
    }


    /**
     * Allows UIs to run any cleanup code after the plugin's run
     * @param task the code to run
     */
    public void setPostTask(NodeTask task) { _postTask = task; }


    /**
     * Runs a prost-task, if one is defined
     * @return true if the run was successful or the task hasn't been set
     */
    public boolean runPostTask() {
        return _postTask == null || _postTask.run(this);
    }


    /**
     * @return the set of Nodes immediately following this node (its flows-into set)
     */
    public Set<Node> next() {return _next; }


    /**
     * Adds a Node to this node's flows-into set
     * @param node the node to add
     */
    public void addNext(Node node) { _next.add(node); }


    /**
     * Removes a Node from this node's flows-into set
     * @param node the Node to remove
     * @return true if the Node was in the set and successfully removed
     */
    public boolean removeNext(Node node) { return _next.remove(node); }


    /**
     * @return true if this node is not the last node in the workflow
     */
    public boolean hasNext() { return !isTail(); }


    /**
     * @return the set of Nodes immediately preceding this node (its flows-from set)
     */
    public Set<Node> previous() { return _previous; }


    /**
     * Adds a Node to this node's flows-from set
     * @param node the node to add
     */
    public void addPrevious(Node node) { _previous.add(node); }


    /**
     * Removes a Node from this node's flows-from set
     * @param node the Node to remove
     * @return true if the Node was in the set and successfully removed
     */
    public boolean removePrevious(Node node) { return _previous.remove(node); }


    /**
     * @return true if this node is not the first node in the workflow
     */
    public boolean hasPrevious() { return !isHead(); }


    /**
     * @return true if this node has no predecessors
     */
    public boolean isHead() { return _previous.isEmpty(); }


    /**
     * @return true if this node has no successors
     */
    public boolean isTail() { return _next.isEmpty(); }


    /**
     * May be overridden by plugins with multi-part run actions
     * @return true if the current run of this node has completed
     */
    public boolean hasCompleted() { return _completed; }


    /**
     * Sets the run completion status of this node
     * @param b the completion status
     */
    protected void setCompleted(boolean b) { _completed = b; }


    /**
     * @return the plugin contained within this node
     */
    public PDQPlugin getPlugin() { return _plugin; }


    /**
     * @return the plugin's name as supplied in its metadata
     */
    public String getName() {
        Plugin metaData = getPlugin().getClass().getAnnotation(Plugin.class);
        return metaData.name();
    }


    /**
     * @return true if the number of predecessor nodes is less than the number of
     * predecessor nodes allowed for the plugin contained by this node
     */
    public boolean allowsInput() {
        return getPlugin().getMaxInputs() > _previous.size();
    }


    /**
     * @return true if the number of successor nodes is less than the number of
     * successor nodes allowed for the plugin contained by this node
     */
    public boolean allowsOutput() {
        return getPlugin().getMaxOutputs() > _next.size();
    }


    /**
     * @return the set of output tables from all predecessor nodes
     */
    public List<Table> getInputs() {
        List<Table> inputs = new ArrayList<>();
        _previous.forEach(node -> {
            if (node.getOutput() != null) inputs.add(node.getOutput());
        });
        return inputs;
    }


    /**
     * Returns this node to its pre-run state
     */
    public void reset() {
        clearOutput();
        _completed = false;
    }


    /**
     * @return the output table (if any) for this node
     */
    public Table getOutput() { return _output; }


    /**
     * Sets the output table for this node
     * @param t the table to set as output
     */
    protected void setOutput(Table t) { _output = t; }


    /**
     * @return true if this node has an output table set
     */
    public boolean hasOutput() { return _output != null; }


    /**
     * Removes the output table (if any) from this node
     * @return the output table removed (if any)
     */
    public Table clearOutput() {
        Table table = _output;
        _output = null;
        return table;
    }

}
