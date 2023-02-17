/*
 * Copyright (c) 2021-2023 Queensland University of Technology
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

package com.processdataquality.praeclarus.action;

import java.util.ArrayList;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionValueException;
import com.processdataquality.praeclarus.option.ColumnNameListOption;
import com.processdataquality.praeclarus.option.MultiLineOption;
import com.processdataquality.praeclarus.option.Option;

import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import tech.tablesaw.selection.Selection;

/**
 * @author Sareh Sadeghianasl
 * @date 8/2/23
 */
@Plugin(name = "Select", author = "Sareh Sadeghianasl", version = "1.0", synopsis = "Selects rows from a table that satisfy a condition")
public class Select extends AbstractAction {

	private int numberOfColumns;
	
	public Select() {
		super();
		getOptions().addDefault("#Columns involved in condition", 0);
		getOptions().addDefault(new MultiLineOption("JavaScript condition", ""));
		numberOfColumns = 0;
	}

	@Override
	public Table run(List<Table> inputList) throws InvalidOptionValueException {
		if (inputList.size() != 1) {
			throw new IllegalArgumentException("This action requires one table as input.");
		}
		Table t1 = inputList.remove(0);
		List<String> sourceColNames = new ArrayList<String>();
		for (int i = 1; i <= numberOfColumns; i++) {
			int j = i - 1;
			String colName = getSelectedColumnNameValue("Column " + i + " (args[" + j + "])");
			sourceColNames.add(colName);
		}

		String condition = getOptions().get("JavaScript condition").asString();

		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		String fullFunction = "function select(args){ return " + condition + ";}";
		ArrayList<Integer> selectedRows = new ArrayList<Integer>();

		try {
			engine.eval(fullFunction);
			Invocable inv = (Invocable) engine;

			for (Row row : t1) {
				List<Object> sourceValues = getSourceValues(t1, row, sourceColNames);

				String result = inv.invokeFunction("select", sourceValues).toString();
				if (result.equalsIgnoreCase("true")) {
					selectedRows.add(row.getRowNumber());
				}
			}
		} catch (ScriptException | NoSuchMethodException e) {
			throw new InvalidOptionValueException("Compile error in JavaScript condition", e.getCause());
		}
		int[] rowIndices = new int[selectedRows.size()];
		for(int i = 0; i<selectedRows.size() ; i++) {
			rowIndices[i] = selectedRows.get(i);
		}
		Selection isSelected= Selection.with(rowIndices);
		Table t2 = t1.where(isSelected);	
		return t2;

	}


	@Override
	public void optionValueChanged(Option option) {
		if (option.equals(getOptions().get("#Columns involved in condition"))) {
			numberOfColumns = option.asInt();
			for (int i = 1; i <= numberOfColumns; i++) {
				int j = i - 1;
				getOptions().addDefault(new ColumnNameListOption("Column " + i + " (args[" + j + "])"));
			}

		}
		super.optionValueChanged(option);

	}

	@Override
	public int getMaxInputs() {
		return 1;
	}

}
