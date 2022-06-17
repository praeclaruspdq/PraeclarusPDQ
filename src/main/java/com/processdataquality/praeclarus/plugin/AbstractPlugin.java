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
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 18/5/2022
 */
public abstract class AbstractPlugin implements PDQPlugin, OptionValueChangeListener {

    private final Map<String, Table> auxiliaryDatasets = new HashMap<>();
    private final List<Table> inputs = new ArrayList<>();
    private final Options options = new Options();
    private String label = getName();                        // default from interface
    private String id = getId();                             // default from interface


    protected AbstractPlugin() {
        options.setValueChangeListener(this);
    }

    @Override
    public Options getOptions() { return options; }


    @Override
    public int getMaxInputs() { return 1; }

    @Override
    public int getMaxOutputs() { return 1; }


    public Map<String, Table> getAuxiliaryDatasets() { return auxiliaryDatasets; }

    public List<Table> getInputs() { return inputs; }


    // can be overridden by plugins that have to effect the value change immediately
    @Override
    public void optionValueChanged(Option option) {
        EventLogger.optionChangeEvent(id, label, option);
    }

    
    public String getLabel() { return label; }

    public void setLabel(String label) { this.label = label; }


    public String getID() { return id; }

    public void setID(String id) { this.id = id; }
    
}
