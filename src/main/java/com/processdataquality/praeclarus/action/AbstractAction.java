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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.processdataquality.praeclarus.exception.InvalidOptionValueException;
import com.processdataquality.praeclarus.option.ColumnNameListAndStringOption;
import com.processdataquality.praeclarus.option.ColumnNameListOption;
import com.processdataquality.praeclarus.option.ListOption;
import com.processdataquality.praeclarus.plugin.AbstractPlugin;

import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

/**
 * @author Michael Adams
 * @date 21/5/21
 */
public abstract class AbstractAction extends AbstractPlugin {

	protected AbstractAction() {
		super();
	}

	public abstract Table run(List<Table> inputSet) throws InvalidOptionValueException;

	protected String getSelectedColumnNameValue(String name) {
		return ((ColumnNameListOption) getOptions().get(name)).getSelected();
	}

	protected String getSelectedValue(String name) {
		return ((ListOption) getOptions().get(name)).getSelected();
	}

	protected String getSelectedColumn(String name) {
		return ((ColumnNameListAndStringOption) getOptions().get(name)).getSelected().getKey();
	}

	protected String getNewName(String name) {
		return ((ColumnNameListAndStringOption) getOptions().get(name)).getSelected().getValue();
	}

	protected List<String> tokenize(String str, String delim) {
		List<String> tokens = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(str, delim);
		while (st.hasMoreTokens()) {
			tokens.add(st.nextToken());
		}
		return tokens;
	}

	protected String readStringValue(Row row, String colName) { // add other types
		String res = "";
		String colTypeName = row.getColumnType(colName).name();
		switch (colTypeName) {
		case "STRING":
			res = row.getString(colName);
			break;
		case "INTEGER":
			res = String.valueOf(row.getInt(colName));
			break;
		case "DOUBLE":
			res = String.valueOf(row.getDouble(colName));
			break;
		case "NUMBER":
			res = String.valueOf(row.getDouble(colName));
			break;	
		case "BOOLEAN":
			res = String.valueOf(row.getBoolean(colName));
			break;
		case "LOCAL_DATE":
			res = String.valueOf(row.getDate(colName));
			break;
		case "LOCAL_TIME":
			res = String.valueOf(row.getTime(colName));
			break;
		case "LOCAL_DATE_TIME":
				res = String.valueOf(row.getDateTime(colName));
				break;	
		default:
			res = row.getString(colName);
			break;
		}

		return res;
	}

	protected Object baseFunction(String functionName, List<Object> sourceValues) {
		Object res = null;
		if (functionName.equals("Sum")) {
			double sum = 0;
			for (Object o : sourceValues) {
				if (o instanceof Short) {
					sum += (Short) o;
				} else if (o instanceof Integer) {
					sum += (Integer) o;
				} else if (o instanceof Long) {
					sum += (Long) o;
				} else if (o instanceof Float) {
					sum += (Float) o;
				} else if (o instanceof Double) {
					sum += (Double) o;
				} else if (o instanceof String) {
					Double d = textToNumber((String) o);
					if (!Double.isNaN(d)) {
						sum += d;
					}
				}
			}
			res = sum;
		} else if (functionName.equals("Avg")) {
			double sum = 0;
			for (Object o : sourceValues) {
				if (o instanceof Short) {
					sum += (Short) o;
				} else if (o instanceof Integer) {
					sum += (Integer) o;
				} else if (o instanceof Long) {
					sum += (Long) o;
				} else if (o instanceof Float) {
					sum += (Float) o;
				} else if (o instanceof Double) {
					sum += (Double) o;
				} else if (o instanceof String) {
					Double d = textToNumber((String) o);
					if (!Double.isNaN(d)) {
						sum += d;
					}
				}
			}
			double avg = 0;
			if (!sourceValues.isEmpty()) {
				avg = sum / sourceValues.size();
			}
			res = avg;
		} else if (functionName.equals("Min")) {
			if (areNumericValues(sourceValues)) {
				double min = Double.MAX_VALUE;
				for (Object o : sourceValues) {
					double val = 0;

					if (o instanceof Short) {
						val = (Short) o;
					} else if (o instanceof Integer) {
						val = (Integer) o;
					} else if (o instanceof Long) {
						val = (Long) o;
					} else if (o instanceof Float) {
						val = (Float) o;
					} else if (o instanceof Double) {
						val = (Double) o;
					} else if (o instanceof String) {
						Double d = textToNumber((String) o);
						if (!Double.isNaN(d)) {
							val = d;
						}
					}
					if (val < min)
						min = val;
				}
				res = min;
			} else if (areTimeValues(sourceValues)) {
				LocalDateTime minTime = LocalDateTime.MAX;
				for (Object o : sourceValues) {
					LocalDateTime val = null;
					if (o instanceof LocalDateTime) {
						val = (LocalDateTime) o;
					} else if (o instanceof LocalTime) {
						val = ((LocalTime) o).atDate(LocalDate.EPOCH);
					} else if (o instanceof LocalDate) {
						val = ((LocalDate) o).atTime(LocalTime.MIDNIGHT);
					} else if (o instanceof Instant) {
						val = LocalDateTime.ofInstant((Instant) o, ZoneOffset.UTC);
					} else if (o instanceof String) {
						val = textToTime((String) o);
					}
					if (val != null && val.isBefore(minTime))
						minTime = val;
				}
				res = minTime;
			}

		} else if (functionName.equals("Max")) {
			if (areNumericValues(sourceValues)) {
				double max = Double.MIN_VALUE;
				for (Object o : sourceValues) {
					double val = 0;
					if (o instanceof Short) {
						val = (Short) o;
					} else if (o instanceof Integer) {
						val = (Integer) o;
					} else if (o instanceof Long) {
						val = (Long) o;
					} else if (o instanceof Float) {
						val = (Float) o;
					} else if (o instanceof Double) {
						val = (Double) o;
					} else if (o instanceof String) {
						Double d = textToNumber((String) o);
						if (!Double.isNaN(d)) {
							val = d;
						}
					}
					if (val > max)
						max = val;
				}
				res = max;
			} else if (areTimeValues(sourceValues)) {
				LocalDateTime maxTime = LocalDateTime.MIN;
				for (Object o : sourceValues) {
					LocalDateTime val = null;
					if (o instanceof LocalDateTime) {
						val = (LocalDateTime) o;
					} else if (o instanceof LocalTime) {
						val = ((LocalTime) o).atDate(LocalDate.EPOCH);
					} else if (o instanceof LocalDate) {
						val = ((LocalDate) o).atTime(LocalTime.MIDNIGHT);
					} else if (o instanceof Instant) {
						val = LocalDateTime.ofInstant((Instant) o, ZoneOffset.UTC);
					} else if (o instanceof String) {
						val = textToTime((String) o);
					}
					if (val != null && maxTime.isBefore(val))
						maxTime = val;
				}
				res = maxTime;
			}
		} else if (functionName.equals("Concat")) {
			String conc = "";
			for (Object o : sourceValues) {
				conc = conc + o.toString() + " ";
			}
			res = conc;
		}
		return res;
	}

	protected LocalDateTime textToTime(String text) { // Add more formats
		LocalDateTime dateTime = null;
		LocalDate date = null;
		LocalTime time = null;
		ArrayList<String> formats = new ArrayList<>();
		formats.add("yyyy-MM-dd'T'HH:mm:ss");
		formats.add("yyyy-MM-dd HH:mm:ss");
		formats.add("yyyy-MM-dd'T'HH:mm:ss.SSS");
		formats.add("yyyy-MM-dd HH:mm:ss.SSS");
		formats.add("yyyy-MM-dd'T'HH:mm");
		formats.add("yyyy-MM-dd HH:mm");
		formats.add("yyyy-MM-dd");
		formats.add("HH:mm");
		formats.add("HH:mm:ss");
		formats.add("HH:mm:ss.SSS");
		formats.add("H:mm");
		formats.add("H:mm:ss");
		formats.add("H:mm:ss.SSS");
		for (String f : formats) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(f);
			try {
				dateTime = LocalDateTime.parse(text, formatter);
				if (dateTime != null) {
					return dateTime;
				}

			} catch (Exception e) {
				try {
					time = LocalTime.parse(text, formatter);
					if (time != null) {
						dateTime = time.atDate(LocalDate.EPOCH);
						return dateTime;
					}
				} catch (Exception e1) {
					try {
						date = LocalDate.parse(text, formatter);
						if (date != null) {
							dateTime = date.atTime(LocalTime.MIDNIGHT);
							return dateTime;
						}
					} catch (Exception e2) {
						continue;
					}
				}
			}
		}
		return dateTime;
	}

	protected Object readObject(Table table, Row row, String colName) {
		String colType = table.column(colName).type().name();
		Object res = null;
		if (colType.equalsIgnoreCase("Integer")) {
			res = row.getInt(colName);
		} else if (colType.equalsIgnoreCase("Double")) {
			res = row.getDouble(colName);
		}else if (colType.equalsIgnoreCase("Number")) {
			res = row.getDouble(colName);	
		} else if (colType.equalsIgnoreCase("Float")) {
			res = row.getFloat(colName);
		} else if (colType.equalsIgnoreCase("Short")) {
			res = row.getShort(colName);
		} else if (colType.equalsIgnoreCase("Long")) {
			res = row.getLong(colName);
		} else if (colType.equalsIgnoreCase("Local_Date")) {
			res = row.getDate(colName);
		} else if (colType.equalsIgnoreCase("Local_Time")) {
			res = row.getTime(colName);
		} else if (colType.equalsIgnoreCase("Local_Date_Time")) {
			res = row.getDateTime(colName);
		} else if (colType.equalsIgnoreCase("Instant")) {
			res = row.getInstant(colName);
		} else if (colType.equalsIgnoreCase("Boolean")) {
			res = row.getBoolean(colName);
		} else { // String column
			res = row.getString(colName);
		}
		return res;
	}

	protected List<Object> getSourceValues(Table table, Row row, List<String> sourceColNames) {
		List<Object> res = new ArrayList<Object>();
		for (String col : sourceColNames) {
			Object val = readObject(table, row, col);
			res.add(val);
		}
		return res;
	}

	protected boolean areNumericValues(List<Object> functionArgs) {
		for (Object o : functionArgs) {
			if (!(o instanceof Integer) && !(o instanceof Double) && !(o instanceof Float) && !(o instanceof Number)
					&& !(o instanceof Short) && !(o instanceof Long)
					&& !(o instanceof String && !Double.isNaN(textToNumber((String) o)))) {
				return false;
			}
		}
		return true;
	}

	protected Double textToNumber(String s) {
		double res = Double.NaN;
		try {
			res = Double.parseDouble(s);
		} catch (Exception e) {
			return res;
		}
		return res;
	}

	protected Double objectToNumber(Object o) {
		double res = Double.NaN;
		if (o instanceof Integer) {
			res = ((Integer) o).doubleValue();
		} else if (o instanceof Double) {
			res = (Double) o;
		} else if (o instanceof Float) {
			res = ((Float) o).doubleValue();
		} else if (o instanceof Short) {
			res = ((Short) o).doubleValue();
		} else if (o instanceof Long) {
			res = ((Long) o).doubleValue();
		} else if (o instanceof Number) {
			res = ((Number) o).doubleValue();
		} else if (o instanceof String) {
			res = textToNumber((String) o);
		}
		return res;
	}

	protected LocalDateTime objectToTime(Object o) {
		LocalDateTime res = null;
		if (o instanceof LocalDate) {
			res = ((LocalDate) o).atTime(LocalTime.MIDNIGHT);
		} else if (o instanceof LocalTime) {
			res = ((LocalTime) o).atDate(LocalDate.EPOCH);
		} else if (o instanceof LocalDateTime) {
			res = (LocalDateTime) o;
		} else if (o instanceof Instant) {
			res = LocalDateTime.ofInstant((Instant) o, ZoneOffset.UTC);
		} else if (o instanceof String) {
			res = textToTime((String) o);
		}
		return res;
	}

	protected boolean areTimeValues(List<Object> functionArgs) {
		for (Object o : functionArgs) {
			if(o instanceof String) {
			}
			if (!(o instanceof LocalDate) && !(o instanceof LocalTime) && !(o instanceof LocalDateTime)
					&& !(o instanceof Instant) && !(o instanceof String && textToTime((String) o) != null)) {
				return false;
				
			}
		}
		return true;
	}

	protected boolean sameSchema(List<Table> inputList) {
		if (inputList.isEmpty()) {
			return false;
		}
		Table first = inputList.get(0);
		for (Table t : inputList) {
			if (inputList.indexOf(t) > 0) {
				if (first.columnCount() != t.columnCount()) {
					return false;
				}
				for (int i = 0; i < t.columns().size(); i++) {
					if (!t.columns().get(i).name().equals(first.columns().get(i).name())) {
						return false;
					}
					if (!t.columns().get(i).type().name().equals(first.columns().get(i).type().name())) {
						return false;
					}
				}
			}
		}
		return true;
	}

	protected boolean equalCells(Object o1, Object o2) {
		if (o1 instanceof Integer && o2 instanceof Integer) {
			return Integer.compare(((Integer) o1).intValue(), ((Integer) o2).intValue()) == 0;
		} else if (o1 instanceof Double && o2 instanceof Double) {
			return Double.compare(((Double) o1).doubleValue(), ((Double) o2).doubleValue()) == 0;
		} else if (o1 instanceof Float && o2 instanceof Float) {
			return Float.compare(((Float) o1).floatValue(), ((Float) o2).floatValue()) == 0;
		} else if (o1 instanceof Long && o2 instanceof Long) {
			return Long.compare(((Long) o1).longValue(), ((Long) o2).longValue()) == 0;
		} else if (o1 instanceof Short && o2 instanceof Short) {
			return Short.compare(((Short) o1).shortValue(), ((Short) o2).shortValue()) == 0;
		} else if (o1 instanceof LocalDate && o2 instanceof LocalDate) {
			return ((LocalDate) o1).compareTo((LocalDate) o2) == 0;
		} else if (o1 instanceof LocalTime && o2 instanceof LocalTime) {
			return ((LocalTime) o1).compareTo((LocalTime) o2) == 0;
		} else if (o1 instanceof LocalDateTime && o2 instanceof LocalDateTime) {
			return ((LocalDateTime) o1).compareTo((LocalDateTime) o2) == 0;
		} else if (o1 instanceof Instant && o2 instanceof Instant) {
			return ((Instant) o1).compareTo((Instant) o2) == 0;
		} else if (o1 instanceof Boolean && o2 instanceof Boolean) {
			return ((Boolean) o1).compareTo((Boolean) o2) == 0;
		} else if (o1 instanceof String && o2 instanceof String) {
			return ((String) o1).equals((String) o2);
		}
		return false;
	}

}
