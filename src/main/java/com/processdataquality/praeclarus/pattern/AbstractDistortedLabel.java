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

package com.processdataquality.praeclarus.pattern;

import com.processdataquality.praeclarus.annotations.Plugin;
import com.processdataquality.praeclarus.plugin.Option;
import com.processdataquality.praeclarus.plugin.Options;
import com.processdataquality.praeclarus.plugin.uitemplate.*;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * A base class for distorted label plugins
 * @author Michael Adams
 * @date 11/5/21
 */
public abstract class AbstractDistortedLabel implements ImperfectionPattern {

    // The table that will contain the results of the pattern detection
    private final Table _detected = createResultTable();

    // The set of parameters used by this plugin
    private Options _options;

    // The UI template used for the front-end interactions
    private PluginUI _ui;


    protected AbstractDistortedLabel() { }

    // To be implemented by subclasses to detect distortion between two strings
    protected abstract void detect(StringColumn column, String s1, String s2);


    /**
     * Basic implementation of the interface method
     * @param table a table containing values to check for the pattern
     * @return a table where each row contains values detected using the pattern
     */
    @Override
    public Table detect(Table table) {
        StringColumn column = getSelectedColumn(table);
        List<String> tested = new ArrayList<>();    // cache of strings already tested
        List<String> compared = new ArrayList<>();  // cache of strings already compared to
        for (int i = 0; i < column.size(); i++) {
            String testValue = column.getString(i);
            if (testValue == null || tested.contains(testValue)) continue;
            tested.add(testValue);
            compared.clear();
            for (int j = i+1; j < column.size(); j++) {
                String compValue = column.getString(j);
                if (compValue == null || compared.contains(compValue)) continue;
                compared.add(compValue);
                detect(column, testValue, compValue);    // call method in subclass
            }
        }
        return _detected;
    }


    /**
     * Repair instances of an imperfection pattern found within a table
     * @param master the original table containing pattern instances
     * @param changes a table of two columns containing rows of keys (strings to find)
     *                and values (strings to replace them with) to use to make
     *                the necessary changes to repair the pattern instances found
     * @return        a table of the original data with the repairs done
     */
    @Override
    public Table repair(Table master, Table changes) {
        String colName = _options.get("Column Name").asString();
        StringColumn repaired = (StringColumn) master.column(colName);
        for (Row row : changes) {
            repaired = repaired.replaceAll(
                    row.getString("Label2"), row.getString("Label1"));
        }
        repaired.setName(colName);
        master.replaceColumn(colName, repaired);
        return master;
    }


    /**
     * By default subclasses can detect, but they can override this as required
     * @return true (this plugin can detect an imperfection pattern)
     */
    @Override
    public boolean canDetect() {
        return true;
    }

    
    /**
     * By default subclasses can repair, but they can override this as required
     * @return true (this plugin can repair a log)
     */
    @Override
    public boolean canRepair() {
        return true;
    }


    @Override
    public int getMaxInputs() { return 1; }

    @Override
    public int getMaxOutputs() { return 1; }



    /**
     * Gets or creates a set of options for the plugin
     * @return
     */
    @Override
    public Options getOptions() {
        if (_options == null) {
            _options = new Options();
            _options.addDefault("Column Name", "");
        }
        return _options;
    }


    /**
     * Sets the options for this plugin, overwriting existing values
     * @param options a map of configuration keys and values
     */
    public void setOptions(Options options) {
        this._options = options;
    }


    /**
     * Gets the column specified in the plugins parameters
     * @param table the table containing columns of data
     * @return the specified column
     */
    protected StringColumn getSelectedColumn(Table table) {
        Option option = _options.get("Column Name");
        if (option != null) {
            String colName = option.asString();
            if (colName != null) {
                return (StringColumn) table.column(colName);
            }
        }
        throw new IllegalArgumentException("A value must be provided for the 'Column Name' property");
    }


    /**
     * Creates the table that will receive the imperfect values detected
     * @return the empty table
     */
    private Table createResultTable() {
        return Table.create("Result").addColumns(
                StringColumn.create("Label1"),
                IntColumn.create("Count1"),
                StringColumn.create("Label2"),
                IntColumn.create("Count2")
        );
    }

    /**
     * Adds a key-value pair to the results table, as well as the frequency of each value
     * as contained in the master table
     * @param column the column from the master table to count frequencies
     * @param s1 the label
     * @param s2 the distorted label
     */
    protected void addResult(StringColumn column, String s1, String s2) {
        int c1 = column.countOccurrences(s1);
        int c2 = column.countOccurrences(s2);
        _detected.stringColumn(0).append(s1);
        _detected.intColumn(1).append(c1);
        _detected.stringColumn(2).append(s2);
        _detected.intColumn(3).append(c2);
    }

    @Override
    public PluginUI getUI() {
        if (_ui == null) {
            String title = getClass().getAnnotation(Plugin.class).name() + " - Detected";
            _ui = new PluginUI(title);

            UITable table = new UITable(_detected);
            table.setMultiSelect(true);
            UIContainer tableLayout = new UIContainer();
            tableLayout.add(table);
            _ui.add(tableLayout);

            UIContainer buttonLayout = new UIContainer(UIContainer.Orientation.HORIZONTAL);
            buttonLayout.add(new UIButton(ButtonAction.CANCEL));
            buttonLayout.add(new UIButton(ButtonAction.REPAIR));
            _ui.add(buttonLayout);
        }
        return _ui;
   }


    @Override
    public void updateUI(PluginUI ui) {
        _ui = ui;
    }
}
