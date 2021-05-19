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
import java.util.List;

/**
 * @author Michael Adams
 * @date 12/5/21
 */
public abstract class Node {

    private List<Table> _inputs;
    private Table _output;
    private PDQPlugin _plugin;
    private int _allowedInputs = 0;
    private int _allowedOutputs = 0;
    private boolean _pauseFlag = false;

    protected Node(PDQPlugin plugin) { setPlugin(plugin); }

    public abstract Table run();

    public void setPlugin(PDQPlugin plugin) { _plugin = plugin; }

    public PDQPlugin getPlugin() { return _plugin; }

    public String getName() {
        PluginMetaData metaData = getPlugin().getClass().getAnnotation(PluginMetaData.class);
        return metaData.name();
    }
    

    public void addInput(Table t) {
        if (_allowedInputs <= 0) {
            throw new IllegalArgumentException("This node does not accept inputs");
        }
        if (_inputs == null) _inputs = new ArrayList<>();
        if (_inputs.size() == _allowedInputs) {
            throw new IllegalArgumentException("Maximum number of inputs already met");
        }
        _inputs.add(t) ;
    }


    public void addInputList(List<Table> list) {
        if (list.size() > _allowedInputs) {
            throw new IllegalArgumentException("Number of inputs exceeds threshold");
        }
        _inputs = list;
    }


    public void clearInputs() { if (_inputs != null) _inputs.clear(); }

    public void clearInput(Table table) {
        if (_inputs != null) {
            _inputs.remove(table);
        }
    }

    public Table getInput() { return getInput(0); }

    public Table getInput(int index) {
        if (_inputs == null || _inputs.size() <= index) {
            throw new IllegalArgumentException("Inputs list is empty");
        }
        return _inputs.get(index);
    }

    public List<Table> getInputs() { return _inputs; }

    public int getInputCount() { return _inputs == null ? 0 : _inputs.size(); }


    public Table getOutput() { return _output; }

    protected void setOutput(Table t) { _output = t; }

    public Table clearOutput() {
        Table table = _output;
        _output = null;
        return table;
    }


    protected void setAllowedInputs(int allowed) { _allowedInputs = allowed; }

    protected void setAllowedOutputs(int allowed) { _allowedOutputs = allowed; }


    public boolean shouldPause() { return _pauseFlag; }

    protected void setPause(boolean b) { _pauseFlag = b; }
    
}
