/*
 * Copyright (c) 2022 Queensland University of Technology
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

package com.processdataquality.praeclarus.plugin;

import com.processdataquality.praeclarus.logging.EventLogger;
import com.processdataquality.praeclarus.option.Option;
import com.processdataquality.praeclarus.option.OptionValueChangeListener;
import com.processdataquality.praeclarus.option.Options;
import com.processdataquality.praeclarus.util.DataCollection;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * The base abstract class for plugins - all plugins must extend from this class.
 * @author Michael Adams
 * @date 18/5/2022
 */
public abstract class AbstractPlugin implements PDQPlugin, OptionValueChangeListener {

    private final Options options = new Options();
    private String label = getName();                        // default from interface
    private String id = getId();                             // default from interface

    // a map that a plugin may use to store and retrieve secondary datasets (besides the
    // primary log dataset) that are passed to subsequent plugins
    private final DataCollection auxiliaryDatasets = new DataCollection();

    // a concatenated list of input tables from all immediately prior plugins
    private final List<Table> inputs = new ArrayList<>();

    /**
     * The constructor
     */
    protected AbstractPlugin() {
        options.setValueChangeListener(this);              // listen for option updates
    }


    /**
     * @return the set of options for this plugin
     */
    @Override
    public Options getOptions() { return options; }


    /**
     * @return the maximum number of plugins that may 'connect' to this one as inputs.
     * Overridden by subclasses as required.
     */
    @Override
    public int getMaxInputs() { return 1; }


    /**
     * @return the maximum number of plugins that may be 'connected' to by this one as
     * outputs. Overridden by subclasses as required.
     */
    @Override
    public int getMaxOutputs() { return 1; }


    /**
     * @return this plugin's map of secondary datasets
     */
    public DataCollection getAuxiliaryDatasets() { return auxiliaryDatasets; }


    /**
     * @return the concatenated list of primary datasets (i.e. outputs) from all prior
     * plugins that are connected to this one
     */
    public List<Table> getInputs() { return inputs; }


    // can be overridden by plugins that have to effect the value change immediately
    @Override
    public void optionValueChanged(Option option) {
        EventLogger.optionChangeEvent(id, label, option);            // log the change
    }

    
    public String getLabel() { return label; }

    public void setLabel(String label) { this.label = label; }


    public String getID() { return id; }

    public void setID(String id) { this.id = id; }
    
}