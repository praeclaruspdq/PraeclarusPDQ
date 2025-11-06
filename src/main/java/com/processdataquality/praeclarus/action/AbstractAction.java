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
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import com.processdataquality.praeclarus.exception.InvalidOptionValueException;
import com.processdataquality.praeclarus.option.ColumnNameListAndStringOption;
import com.processdataquality.praeclarus.option.ColumnNameListOption;
import com.processdataquality.praeclarus.option.ListOption;
import com.processdataquality.praeclarus.option.TableNameListOption;
import com.processdataquality.praeclarus.plugin.AbstractPlugin;

import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

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

	protected String getSelectedTable(String name) {
		return ((TableNameListOption) getOptions().get(name)).getSelected();
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
		}else if (functionName.equals("Copy")) {
			Object o = sourceValues.get(0);
			if(isNumericValue(o)) {
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
				res = val;
			}else if(isTimeValue(o)) {
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
				if (val != null) {
					res = val;
				}
			}
			
		}
		return res;
	}

	protected boolean areNumericValues(List<Object> functionArgs) {
		for (Object o : functionArgs) {
			if (!isNumericValue(o)) {
				return false;
			}
		}
		return true;
	}

	protected boolean isNumericValue(Object o) {
		if (o instanceof String) {
			String text = (String) o;
			if (text.isEmpty()) {
				return true;
			}
		}
		if (!(o instanceof Integer) && !(o instanceof Double) && !(o instanceof Float) && !(o instanceof Number)
				&& !(o instanceof Short) && !(o instanceof Long)
				&& !(o instanceof String && !Double.isNaN(textToNumber((String) o)))) {
			return false;
		}
		return true;
	}

	protected boolean areTimeValues(List<Object> functionArgs) {
		for (Object o : functionArgs) {
			if (!isTimeValue(o)) {
				return false;
			}
		}
		return true;
	}

	protected boolean isTimeValue(Object o) {

		if (o instanceof String) {
			String text = (String) o;
			if (text.isEmpty()) {
				return true;
			}
		}
		if (!(o instanceof LocalDate) && !(o instanceof LocalTime) && !(o instanceof LocalDateTime)
				&& !(o instanceof Instant) && !(o instanceof String && textToTime((String) o) != null)) {
			return false;
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
				if(!(new HashSet<>(first.columnNames()).equals(new HashSet<>(t.columnNames())))) {
					return false;
				}
			}
		}
		return true;
	}

	protected Table convertTimeColsToLocalDateTime(Table table, List<String> timestampColumnNames) {
		for (String col : timestampColumnNames) {
			Column<String> originalColumn = table.stringColumn(col);
			int index = table.columnIndex(col);
			String originalName = originalColumn.name();
			LocalDateTime[] dates = new LocalDateTime[originalColumn.size()];
			int i = 0;
			for (String StringTime : originalColumn) {
				dates[i] = textToTime(StringTime);
				i++;
			}
			DateTimeColumn dateTimeColumn = DateTimeColumn.create(originalName, dates);
			table.replaceColumn(index, dateTimeColumn);
		}
		return table;
	}

	protected Table convertColsToString(Table table) {
		for (int i = 0; i < table.columnCount(); i++) {
			Column<?> originalColumn = table.column(i);
			String originalName = originalColumn.name();
			StringColumn stringColumn = originalColumn.asStringColumn();
			stringColumn.setName(originalName);
			table.replaceColumn(i, stringColumn);
		}
		return table;
	}

}
