/*
 * Copyright (c) 2021-2022 Queensland University of Technology
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

import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionException;
import com.processdataquality.praeclarus.exception.OptionException;
import com.processdataquality.praeclarus.option.ColumnNameListOption;
import com.processdataquality.praeclarus.plugin.uitemplate.*;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * A base class for imperfect label plugins
 * 
 * @author Michael Adams
 * @date 11/5/21
 */
public abstract class AbstractImperfectLabel extends AbstractDataPattern {

	// The table that will contain the results of the pattern detection
	protected Table _detected;

	protected AbstractImperfectLabel() {
		super();
		getOptions().addDefault(new ColumnNameListOption("Column Name"));
		// _detected = createResultTable();
	}

	// To be implemented by subclasses to detect distortion between two strings
	abstract void detect(StringColumn column, String s1, String s2);

	/**
	 * Basic implementation of the interface method
	 * 
	 * @param table a table containing values to check for the pattern
	 * @return a table where each row contains values detected using the pattern
	 */
	@Override
	public Table detect(Table table) throws OptionException {
		_detected = createResultTable();
		StringColumn column = getSelectedColumn(table);
		List<String> tested = new ArrayList<>(); // cache of strings already tested
		List<String> compared = new ArrayList<>(); // cache of strings already compared to
		for (int i = 0; i < column.size(); i++) {
			String testValue = column.getString(i);
			if (testValue == null || tested.contains(testValue))
				continue;
			tested.add(testValue);
			compared.clear();
			for (int j = 0; j < column.size(); j++) {
				if (i != j) {
					String compValue = column.getString(j);
					if (compValue == null || compared.contains(compValue))
						continue;
					compared.add(compValue);
					detect(column, testValue, compValue); // call method in subclass
				}
			}
		}
		return _detected;
	}

	/**
	 * Repair instances of an imperfection pattern found within a table
	 * 
	 * @param master the original table containing pattern instances
	 * @return a table of the original data with the repairs done
	 */
	@Override
	public Table repair(Table master) throws InvalidOptionException {
		String colName = getSelectedColumnNameValue("Column Name");
		StringColumn repaired = getSelectedColumn(master, colName);
		for (Row row : getRepairs()) {
			repaired = repaired.replaceAll(row.getString("Label2"), row.getString("Label1"));
		}
		repaired.setName(colName);
		master.replaceColumn(colName, repaired);
		return master;
	}

	/**
	 * Gets the column specified in the plugins parameters
	 * 
	 * @param table the table containing columns of data
	 * @return the specified column
	 */
	protected StringColumn getSelectedColumn(Table table, String selectedColName) throws InvalidOptionException {
		if (table.columnNames().contains(selectedColName)) {
			return (StringColumn) table.column(selectedColName);
		}
		throw new InvalidOptionException("No column named '" + selectedColName + "' in input table");
	}

	protected StringColumn getSelectedColumn(Table table) throws InvalidOptionException {
		return getSelectedColumn(table, getSelectedColumnNameValue("Column Name"));
	}

	protected String getSelectedColumnNameValue(String name) {
		return ((ColumnNameListOption) getOptions().get(name)).getSelected();
	}

	/**
	 * Creates the table that will receive the imperfect values detected
	 * 
	 * @return the empty table
	 */
	protected Table createResultTable() {
		return Table.create("Result").addColumns(StringColumn.create("Label1"), IntColumn.create("Count1"),
				StringColumn.create("Label2"), IntColumn.create("Count2"));
	}

	/**
	 * Adds a key-value pair to the results table, as well as the frequency of each
	 * value as contained in the master table
	 * 
	 * @param column the column from the master table to count frequencies
	 * @param s1     the label
	 * @param s2     the distorted label
	 */
	protected void addResult(StringColumn column, String s1, String s2) {
		int c1 = column.countOccurrences(s1);
		int c2 = column.countOccurrences(s2);
		_detected.stringColumn(0).append(s1);
		_detected.intColumn(1).append(c1);
		_detected.stringColumn(2).append(s2);
		_detected.intColumn(3).append(c2);
	}

	/**
	 * Gets the table with the repair rows (to be performed)
	 */
	public Table getRepairs() {
		List<UITable> tables = _ui.extractTables();

		// only one UITable component for this ui
		return tables.get(0).getSelectedRows();
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

}
