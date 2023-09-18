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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
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

import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

/**
 * @author Sareh Sadeghianasl
 * @date 10/2/2023
 */
@Plugin(name = "Aggregate", author = "Sareh Sadeghianasl", version = "1.0", synopsis = "Aggregates rows")
public class Aggregate extends AbstractAction {

	private int numberOfRows;
	private String[] baseFunctionNames;
	private ArrayList<String> baseFunctions = new ArrayList<String>();

	public Aggregate() {
		super();
		getOptions().addDefault(new ColumnNameListOption("CaseId Column"));
		getOptions().addDefault(new ColumnNameListOption("Main Column"));
		getOptions().addDefault(new ColumnNameListOption("Time Column"));
		getOptions().addDefault("#Values to aggregate", 0);
		getOptions().addDefault("Time interval (seconds)", 5);
		baseFunctions.add("Sum");
		baseFunctions.add("Avg");
		baseFunctions.add("Min");
		baseFunctions.add("Max");
		baseFunctions.add("Concat");
		baseFunctions.add("First");
		baseFunctions.add("Last");
		baseFunctions.add("Custom function");
		numberOfRows = 0;

	}

	@Override
	public Table run(List<Table> inputList) throws InvalidOptionValueException {
		if (inputList.size() != 1) {
			throw new IllegalArgumentException("This action requires one table as input");
		}
		Table t1 = inputList.remove(0);
		String caseidColName = getSelectedColumnNameValue("CaseId Column");
		String mainColName = getSelectedColumnNameValue("Main Column");
		String timeColName = getSelectedColumnNameValue("Time Column");
		int timeInterval = getOptions().get("Time interval (seconds)").asInt();
		if (caseidColName.equals(mainColName)) {
			throw new IllegalArgumentException("Case Id column should be different from the main column");
		}

		List<String> values = new ArrayList<String>();
		for (int i = 1; i <= numberOfRows; i++) {
			String value = getOptions().get("Value " + i).asString();
			values.add(value);
		}
		String newValue = getOptions().get("Value new").asString();
		if (values.size() < 2) {
			throw new IllegalArgumentException("This action requires more than one row");
		}
		ArrayList<ArrayList<Integer>> matchingRows = findMatchingRows(t1, values, mainColName,caseidColName,timeColName, timeInterval);
		Object[][] newRows = new Object[matchingRows.size()][t1.columnCount()];
		for (ArrayList<Integer> match : matchingRows) {
			Object[] newRow = new Object[t1.columnCount()];
			newRow[t1.columnIndex(caseidColName)] = readObject(t1, t1.row(match.get(0)), caseidColName);
			newRow[t1.columnIndex(mainColName)] = newValue;
			for (int j = 0; j < baseFunctionNames.length; j++) {
				String functionName = baseFunctionNames[j];
				if (!functionName.equals("N/A")) {
					List<Object> functionArgs = new ArrayList<Object>();
					for (int i = 0; i < match.size(); i++) {
						functionArgs.add(readObject(t1, t1.row(match.get(i)), t1.column(j).name()));
					}
					if (functionName.equals("Sum") || functionName.equals("Avg")) {
						if (!areNumericValues(functionArgs)) {
							throw new IllegalArgumentException(
									"Function " + functionName + " is only applicable on numeric values");
						}
						newRow[j] = baseFunction(functionName, functionArgs);
					} else if (functionName.equals("Min") || functionName.equals("Max")) {
						if (!areNumericValues(functionArgs) && !areTimeValues(functionArgs)) {
							throw new IllegalArgumentException(
									"Function " + functionName + " is only applicable on numeric or time values");
						}
						newRow[j] = baseFunction(functionName, functionArgs);
					} else if (functionName.equals("Concat")) {
						newRow[j] = baseFunction(functionName, functionArgs);
					} else if (functionName.equals("First")) {
						newRow[j] = functionArgs.get(0);
					} else if (functionName.equals("Last")) {
						newRow[j] = functionArgs.get(functionArgs.size() - 1);
					} else if (functionName.equals("Custom function")) {
						String functionHead = getOptions().get("JavaScript function name_" + j).asString();
						String functionBody = getOptions().get("JavaScript function body_" + j).asString();
						ScriptEngineManager manager = new ScriptEngineManager();
						ScriptEngine engine = manager.getEngineByName("JavaScript");
						String fullFunction = "function " + functionHead + "(args){" + functionBody + "}";
					
						try {
							engine.eval(fullFunction);
							Invocable inv = (Invocable) engine;
							newRow[j] = inv.invokeFunction(functionHead, functionArgs);
						} catch (ScriptException | NoSuchMethodException e) {
							throw new InvalidOptionValueException(
									"Compile error in JavaScript function " + functionHead, e.getCause());
						}
					}
				}
			}
			newRows[matchingRows.indexOf(match)] = newRow;
		}
		Table t2 = t1.copy();
		for (int i = 0; i < newRows.length; i++) {
			int rowIndex = matchingRows.get(i).get(0);
			for (int j = 0; j < newRows[i].length; j++) {
				int colIndex = j;
				updateCell(t2, rowIndex, colIndex, newRows[i][j]);
			}
		}
		ArrayList<Integer> rowsToDrop = new ArrayList<Integer>();
		for (ArrayList<Integer> match : matchingRows) {
			for (int i = 1; i < match.size(); i++) {
				rowsToDrop.add(match.get(i));
			}
		}
		
		if(rowsToDrop.size()>0) {
			return t2.dropRows(rowsToDrop.stream().mapToInt(Integer::intValue).toArray());
		}
		return t2;

	}

	private void updateCell(Table table, int rowIndex, int colIndex, Object object) {
		String colType = table.column(colIndex).type().name();
		if (colType.equalsIgnoreCase("Integer")) {
			if (object instanceof Integer) {
				table.row(rowIndex).setInt(colIndex, (Integer) object);
			}else if (object instanceof Double) {
				table.row(rowIndex).setInt(colIndex, ((Double) object).intValue());
			}else if (object instanceof Float) {
				table.row(rowIndex).setInt(colIndex, ((Float) object).intValue());
			}else if (object instanceof Short) {
				table.row(rowIndex).setInt(colIndex, ((Short) object).intValue());
			}else if (object instanceof Long) {
				table.row(rowIndex).setInt(colIndex, ((Long) object).intValue());
			}			
				
		} else if (colType.equalsIgnoreCase("Double")) {
			if (object instanceof Integer) {
				table.row(rowIndex).setDouble(colIndex, ((Integer) object).doubleValue());
			}else if (object instanceof Double) {
				table.row(rowIndex).setDouble(colIndex, ((Double) object).doubleValue());
			}else if (object instanceof Float) {
				table.row(rowIndex).setDouble(colIndex, ((Float) object).doubleValue());
			}else if (object instanceof Short) {
				table.row(rowIndex).setDouble(colIndex, ((Short) object).doubleValue());
			}else if (object instanceof Long) {
				table.row(rowIndex).setDouble(colIndex, ((Long) object).doubleValue());
			}
				
		}else if (colType.equalsIgnoreCase("Number")) {
			if (object instanceof Integer) {
				table.row(rowIndex).setDouble(colIndex, ((Integer) object).doubleValue());
			}else if (object instanceof Double) {
				table.row(rowIndex).setDouble(colIndex, ((Double) object).doubleValue());
			}else if (object instanceof Float) {
				table.row(rowIndex).setDouble(colIndex, ((Float) object).doubleValue());
			}else if (object instanceof Short) {
				table.row(rowIndex).setDouble(colIndex, ((Short) object).doubleValue());
			}else if (object instanceof Long) {
				table.row(rowIndex).setDouble(colIndex, ((Long) object).doubleValue());
			}
				
		}
		else if (colType.equalsIgnoreCase("Float")) {
			if (object instanceof Integer) {
				table.row(rowIndex).setFloat(colIndex, ((Integer) object).floatValue());
			}else if (object instanceof Double) {
				table.row(rowIndex).setFloat(colIndex, ((Double) object).floatValue());
			}else if (object instanceof Float) {
				table.row(rowIndex).setFloat(colIndex, ((Float) object).floatValue());
			}else if (object instanceof Short) {
				table.row(rowIndex).setFloat(colIndex, ((Short) object).floatValue());
			}else if (object instanceof Long) {
				table.row(rowIndex).setFloat(colIndex, ((Long) object).floatValue());
			}
			
		} else if (colType.equalsIgnoreCase("Short")) {
			if (object instanceof Integer) {
				table.row(rowIndex).setShort(colIndex, ((Integer) object).shortValue());
			}else if (object instanceof Double) {
				table.row(rowIndex).setShort(colIndex, ((Double) object).shortValue());
			}else if (object instanceof Float) {
				table.row(rowIndex).setShort(colIndex, ((Float) object).shortValue());
			}else if (object instanceof Short) {
				table.row(rowIndex).setShort(colIndex, ((Short) object).shortValue());
			}else if (object instanceof Long) {
				table.row(rowIndex).setShort(colIndex, ((Long) object).shortValue());
			}
				
		} else if (colType.equalsIgnoreCase("Long")) {
			if (object instanceof Integer) {
				table.row(rowIndex).setLong(colIndex, ((Integer) object).longValue());
			}else if (object instanceof Double) {
				table.row(rowIndex).setLong(colIndex, ((Double) object).longValue());
			}else if (object instanceof Float) {
				table.row(rowIndex).setLong(colIndex, ((Float) object).longValue());
			}else if (object instanceof Short) {
				table.row(rowIndex).setLong(colIndex, ((Short) object).longValue());
			}else if (object instanceof Long) {
				table.row(rowIndex).setLong(colIndex, ((Long) object).longValue());
			}
		} else if (colType.equalsIgnoreCase("Local_Date")) {
			if (object instanceof LocalDate)
				table.row(rowIndex).setDate(colIndex, (LocalDate) object);
			else if (object instanceof LocalDateTime)
				table.row(rowIndex).setDate(colIndex, ((LocalDateTime) object).toLocalDate());
			else if (object instanceof Instant)
				table.row(rowIndex).setDate(colIndex, LocalDate.ofInstant((Instant) object, ZoneOffset.UTC));
		} else if (colType.equalsIgnoreCase("Local_Time")) {
			if (object instanceof LocalTime)
				table.row(rowIndex).setTime(colIndex, (LocalTime) object);
			else if (object instanceof LocalDateTime)
				table.row(rowIndex).setTime(colIndex, ((LocalDateTime) object).toLocalTime());
			else if (object instanceof Instant)
				table.row(rowIndex).setTime(colIndex, LocalTime.ofInstant((Instant) object, ZoneOffset.UTC));
		} else if (colType.equalsIgnoreCase("Local_Date_Time")) {
			if (object instanceof LocalDateTime)
				table.row(rowIndex).setDateTime(colIndex, (LocalDateTime) object);
			else if (object instanceof LocalDate)
				table.row(rowIndex).setDateTime(colIndex, ((LocalDate) object).atTime(LocalTime.MIDNIGHT));
			else if (object instanceof LocalTime)
				table.row(rowIndex).setDateTime(colIndex, ((LocalTime) object).atDate(LocalDate.EPOCH));
			else if (object instanceof Instant)
				table.row(rowIndex).setDateTime(colIndex, LocalDateTime.ofInstant((Instant) object, ZoneOffset.UTC));
		} else if (colType.equalsIgnoreCase("Instant")) {
			if (object instanceof Instant)
				table.row(rowIndex).setInstant(colIndex, (Instant) object);
		} else if (colType.equalsIgnoreCase("Boolean")) {
			if (object instanceof Boolean)
				table.row(rowIndex).setBoolean(colIndex, (Boolean) object);
			else if (object instanceof String) {
				table.row(rowIndex).setBoolean(colIndex, Boolean.valueOf((String) object)); // returns false if string																						// is not a boolean.
			}
		} else { // String column
			table.row(rowIndex).setString(colIndex, object.toString());
		}
	}

	private ArrayList<ArrayList<Integer>> findMatchingRows(Table table, List<String> values, String mainColName,
			String caseidColName, String timeColName, int timeInterval) {
		ArrayList<ArrayList<Integer>> foundRows = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < table.rowCount(); i++) {
			String val = readObject(table, table.row(i), mainColName).toString();
			if (val.equals(values.get(0))) {
				String cid = readObject(table, table.row(i), caseidColName).toString();
				ArrayList<LocalDateTime> allTimes = new ArrayList<LocalDateTime>();
				allTimes.add(objectToTime(readObject(table, table.row(i), timeColName)));
				ArrayList<Integer> temp = new ArrayList<>();
				temp.add(i);
				boolean allMatch = true;
				for (int j = 1; j < values.size(); j++) {
					String nextVal = readObject(table, table.row(i+j), mainColName).toString();
					String nextCid = readObject(table, table.row(i+j), caseidColName).toString();
					if (cid.equals(nextCid) &&  nextVal.equals(values.get(j))) {
						temp.add(i + j);
						allTimes.add(objectToTime(readObject(table, table.row(i+j), timeColName)));
					} else {
						allMatch = false;
						break;
					}
				}
				if (allMatch && isWithinTimeInterval(allTimes, timeInterval)) {
					foundRows.add(temp);
				}
			}
		}
		return foundRows;
	}

	private boolean isWithinTimeInterval(ArrayList<LocalDateTime> allTimes, int timeInterval) {
		LocalDateTime max = LocalDateTime.MIN;
		LocalDateTime min = LocalDateTime.MAX;
		Boolean allNull = true;
		for(LocalDateTime time: allTimes) {
			if(time!=null) {
				allNull = false;
				if(time.isBefore(min)) {
					min = time;
				}
				if(time.isAfter(max)) {
					max = time;
				}
			}
		}
		if(allNull || Duration.between(min, max).toSeconds()<timeInterval) {
			return true;
		}
		return false;
	}

	@Override
	public void optionValueChanged(Option option) {
		if (option.equals(getOptions().get("#Values to aggregate"))) {
			numberOfRows = option.asInt();
			for (int i = 1; i <= numberOfRows; i++) {
				getOptions().addDefault("Value " + i, "");
			}
			getOptions().addDefault("Value new", "");

		} else if (option instanceof ColumnNameListOption) {
			String caseidColName = ((ColumnNameListOption) getOptions().get("CaseId Column")).getSelected();
			String mainColName = ((ColumnNameListOption) getOptions().get("Main Column")).getSelected();
			Table table = getAuxiliaryDatasets().getTable("inputTable");
			baseFunctionNames = new String[table.columnCount()];
			if (table != null && !caseidColName.isEmpty() && !mainColName.isEmpty()
					&& !caseidColName.equals(mainColName)) {
				List<String> toRemove = new ArrayList<String>();
				for (Option o : getOptions().values()) {
					if (o.key().startsWith("Function") || o.key().startsWith("JavaScript")) {
						toRemove.add(o.key());
					}
				}
				for (String key : toRemove) {
					getOptions().remove(key);
				}
				for (Column c : table.columnArray()) {
					if (!c.name().equals(caseidColName) && !c.name().equals(mainColName)) {
						getOptions().addDefault(
								new ListOption("Function_" + c.name() + "_" + table.columnIndex(c), baseFunctions));
						baseFunctionNames[table.columnIndex(c)] = baseFunctions.get(0);
					} else {
						baseFunctionNames[table.columnIndex(c)] = "N/A";
					}
				}
			}
		} else if (option instanceof ListOption) {
			String sIndex = option.key().substring(option.key().lastIndexOf("_") + 1);
			int colIndex = Integer.valueOf(sIndex);
			baseFunctionNames[colIndex] = getSelectedValue(option.key());
			if (baseFunctionNames[colIndex].equals("Custom function")) {
				getOptions().addDefault("JavaScript function name_" + colIndex, "aggregate_" + colIndex);
				getOptions().addDefault(new MultiLineOption("JavaScript function body_" + colIndex, ""));
			} else {
				getOptions().remove("JavaScript function name_" + colIndex);
				getOptions().remove("JavaScript function body_" + colIndex);
			}
		}

		super.optionValueChanged(option);

	}

	@Override
	public int getMaxInputs() {
		return 1;
	}

}
