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
import com.processdataquality.praeclarus.option.ListOption;
import com.processdataquality.praeclarus.option.MultiLineOption;
import com.processdataquality.praeclarus.option.Option;

import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

/**
 * @author Sareh Sadeghianasl
 * @date 10/2/2023
 */
@Plugin(name = "Transform", author = "Sareh Sadeghianasl", version = "1.0", synopsis = "Transforms a number of source columns to a destination column")
public class Transform extends AbstractAction {

	private int numberOfColumns;
	private String baseFunctionName;
	private ArrayList<String> baseFunctions = new ArrayList<String>();

	public Transform() {
		super();
		getOptions().addDefault("#Source columns", 0);
		getOptions().addDefault("Destination column", "");
		baseFunctions.add("Sum");
		baseFunctions.add("Avg");
		baseFunctions.add("Min");
		baseFunctions.add("Max");
		baseFunctions.add("Concat");
		baseFunctions.add("Custom function");
		getOptions().addDefault(new ListOption("Function", baseFunctions));
		numberOfColumns = 0;
		baseFunctionName = baseFunctions.get(0);
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
		String destColName = getOptions().get("Destination column").asString();
		StringColumn destCol = StringColumn.create(destColName);
		if (baseFunctionName.equals("Custom function")) {
			String functionName = getOptions().get("JavaScript function name").asString();
			String functionBody = getOptions().get("JavaScript function body").asString();

			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("JavaScript");
			String fullFunction = "function " + functionName + "(args){" + functionBody + "}";

			try {
				engine.eval(fullFunction);
				Invocable inv = (Invocable) engine;

				for (Row row : t1) {
					List<Object> sourceValues = getSourceValues(t1, row, sourceColNames);

					String destValue = inv.invokeFunction(functionName, sourceValues).toString();
					destCol = destCol.append(destValue);
				}
			} catch (ScriptException | NoSuchMethodException e) {
				throw new InvalidOptionValueException("Compile error in JavaScript function", e.getCause());
			}
		} else if (baseFunctionName.equals("Sum") || baseFunctionName.equals("Avg")) {

			for (Row row : t1) {
				List<Object> sourceValues = getSourceValues(t1, row, sourceColNames);
				if (!areNumericValues(sourceValues)) {
					throw new IllegalArgumentException(
							"Function " + baseFunctionName + " is only applicable on numeric values.");
				}
				String destValue = String.valueOf(baseFunction(baseFunctionName, sourceValues));
				destCol = destCol.append(destValue);
			}
		}

		else if (baseFunctionName.equals("Min") || baseFunctionName.equals("Max")) {
			for (Row row : t1) {
				List<Object> sourceValues = getSourceValues(t1, row, sourceColNames);
				if (!areNumericValues(sourceValues) && !areTimeValues(sourceValues)) {
					throw new IllegalArgumentException(
							"Function " + baseFunctionName + " is only applicable on numeric or time values.");
				}
				String destValue = String.valueOf(baseFunction(baseFunctionName, sourceValues));
				destCol = destCol.append(destValue);
			}
		} else if (baseFunctionName.equals("Concat")) {
			for (Row row : t1) {
				List<Object> sourceValues = getSourceValues(t1, row, sourceColNames);
				String destValue = String.valueOf(baseFunction(baseFunctionName, sourceValues));
				destCol = destCol.append(destValue);
			}
		}
		Table t2 = t1.copy();
		if (t1.columnNames().contains(destColName)) {
			return t2.replaceColumn(destCol);
		} else {
			return t2.addColumns(destCol);
		}

	}

	@Override
	public void optionValueChanged(Option option) {
		if (option.equals(getOptions().get("#Source columns"))) {
			numberOfColumns = option.asInt();
			for (int i = 1; i <= numberOfColumns; i++) {
				int j = i - 1;
				getOptions().addDefault(new ColumnNameListOption("Column " + i + " (args[" + j + "])"));
			}

		}
		if (option.equals(getOptions().get("Function"))) {
			baseFunctionName = getSelectedValue("Function");
			if (baseFunctionName.equals("Custom function")) {
				getOptions().addDefault("JavaScript function name", "transform");
				getOptions().addDefault(new MultiLineOption("JavaScript function body", ""));
			} else {
				getOptions().remove("JavaScript function name");
				getOptions().remove("JavaScript function body");
			}
		}
		super.optionValueChanged(option);

	}

	@Override
	public int getMaxInputs() {
		return 1;
	}

}
