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

import com.processdataquality.praeclarus.annotations.Plugin;
import com.processdataquality.praeclarus.plugin.AbstractPlugin;
import com.processdataquality.praeclarus.repo.Repo;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import tech.tablesaw.api.Table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A node in a workflow, representing a plugin. This class provides base functionality
 * to be used and/or extended by subclasses.
 *
 * @author Michael Adams
 * @date 12/5/21
 */
public abstract class Node {

    private final NodeStopWatch _stopWatch = new NodeStopWatch();

    private String _commitID;           // the commit version of the table in the repo
    private String _tableID;            // the file name of the table in the repo
    private String _label;

    private Set<Node> _next;      // set of immediate target nodes for this node
    private Set<Node> _previous;  // set of immediate source nodes for this node

    private AbstractPlugin _plugin;
    private List<NodeStateChangeListener> _listeners;
    private NodeState _state;
    private Table _output;        // a table with the result of running this plugin
    private NodeTask _preTask;          // optional code to run before plugin is run
    private NodeTask _postTask;         // optional code to run after plugin is run

    
    protected Node() { }

    protected Node(AbstractPlugin plugin) {
        _plugin = plugin;
        _next = new HashSet<>();
        _previous = new HashSet<>();
        _listeners = new ArrayList<>();
        _state = NodeState.UNSTARTED;
    }


    /**
     * Implemented by subclasses to run a plugin's algorithm
     */
    public abstract void run() throws Exception;


    public void addStateListener(NodeStateChangeListener listener) { _listeners.add(listener); }

    public boolean removeStateListener(NodeStateChangeListener listener) {
        return _listeners.remove(listener);
    }


    protected void setState(NodeState state) throws Exception {
        if (_state != state) {
            _state = state;
            _stopWatch.stateChange(state);
             announceStateChange();
        }
    }

    /**
     * @return the current state of this node
     */
    public NodeState getState() { return _state; }


    public NodeStopWatch getStopWatch() { return _stopWatch; }


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
     * Connects this node to a target node
     * @param target the target node of the directed connection
     */
    public void connect(Node target) {
        addNext(target);
        target.addPrevious(this);
    }


    /**
     * Disconnects this node to from a target node
     * @param target the target node of the directed connection
     */
    public void disconnect(Node target) {
        removeNext(target);
        target.removePrevious(this);
    }


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
    public boolean hasCompleted() { return _state == NodeState.COMPLETED; }


    public boolean canStart() { return _state == NodeState.UNSTARTED; }

    /**
     * @return the plugin contained within this node
     */
    public AbstractPlugin getPlugin() { return _plugin; }


    public String getID() { return _plugin.getID(); }

    public String getCommitID() { return _commitID; }

    public void setCommitID(String id) { _commitID = id; }


    /**
     * @return the plugin's name as supplied in its metadata
     */
    public String getLabel() {
        if (_label == null) {
            Plugin metaData = getPlugin().getClass().getAnnotation(Plugin.class);
            _label = metaData != null ? metaData.name() : "unnamed";
        }
        return _label;
    }


    public void setLabel(String label) {
        _label = label;
        _plugin.setLabel(label);
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
    public void reset() throws Exception {
        clearOutput();
        setState(NodeState.UNSTARTED);
    }


    public String getTableID() {
        return _tableID;
    }


    /**
     * @return the output table (if any) for this node
     */
    public Table getOutput() { return _output; }


    /**
     * Sets the output table for this node
     * @param t the table to set as output
     */
    protected void setOutput(Table t) {
        _output = t;
        _tableID = t.name();
        commit(t);
    }


    public void loadOutput(String tableID) throws IOException {
        _output = Repo.getTable(_commitID, tableID);
        if (_output != null) {
            _tableID = tableID;
            _state = NodeState.COMPLETED;
        }
    }


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


    private void commit(Table t) {
        try {
            _commitID = Repo.commit(t, getCommitMessage(), "author");
        }
        catch (IOException | GitAPIException e) {
            System.out.println("Failed to commit table to repo: " + e.getMessage());
        }
    }


    private String getCommitMessage() {
        return "Node: " + getID() + "; Plugin: " + getLabel() +
                "; Plugin Class: " + getPlugin().getClass().getName();
    }


    public JSONObject asJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", getID());
        json.put("label", _label);
        if (_commitID != null) json.put("commitID", _commitID);
        if (_tableID != null) json.put("tableID", _tableID);

        json.put("plugin", _plugin.getClass().getName());
        json.put("options", _plugin.getOptions().getChangesAsJson());
        return json;
    }


    protected void announceStateChange() throws Exception {
        for (NodeStateChangeListener listener : _listeners) {
            listener.nodeStateChanged(this);
        }
    }

}
