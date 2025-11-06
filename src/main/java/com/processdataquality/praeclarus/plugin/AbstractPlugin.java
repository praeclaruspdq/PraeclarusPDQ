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

import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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

    protected LocalDateTime textToTime(String text) { // Add more formats
		LocalDateTime dateTime = null;
		LocalDate date = null;
		LocalTime time = null;
		ArrayList<String> formats = new ArrayList<>();
		formats.add("yyyy-MM-dd'T'HH:mm:ss");
		formats.add("yyyy-MM-dd HH:mm:ss");
		formats.add("yyyy-MM-dd'T'HH:mm:ss.SSS");
		formats.add("yyyy-MM-dd'T'HH:mm:ss.SS");
		formats.add("yyyy-MM-dd'T'HH:mm:ss.S");
		formats.add("yyyy-MM-dd HH:mm:ss.SSS");
		formats.add("yyyy-MM-dd HH:mm:ss.S");
		formats.add("yyyy-MM-dd HH:mm:ss.SS");
		formats.add("yyyy-MM-dd'T'HH:mm");
		formats.add("yyyy-MM-dd HH:mm");
		formats.add("yyyy-MM-dd");
		formats.add("HH:mm");
		formats.add("HH:mm:ss");
		formats.add("HH:mm:ss.SSS");
		formats.add("HH:mm:ss.SS");
		formats.add("HH:mm:ss.S");
		formats.add("H:mm");
		formats.add("H:mm:ss");
		formats.add("H:mm:ss.SSS");
		formats.add("H:mm:ss.SS");
		formats.add("H:mm:ss.S");
		formats.add("dd/MM/yyyy HH:mm:ss");
		formats.add("dd/MM/yyyy HH:mm:ss.SSS");
		formats.add("dd/MM/yyyy HH:mm:ss.SS");
		formats.add("dd/MM/yyyy HH:mm:ss.S");
		formats.add("dd/MM/yyyy H:mm");
		formats.add("dd/MM/yyyy H:mm:ss");
		formats.add("dd/MM/yyyy H:mm:ss.SSS");
		formats.add("dd/MM/yyyy H:mm:ss.SS");
		formats.add("dd/MM/yyyy H:mm:ss.S");
		formats.add("dd/MM/yyyy");
		formats.add("dd/MM/yyyy HH:mm");
		formats.add("yyyy/MM/dd HH:mm:ss.SSS");
		formats.add("yyyy/MM/dd HH:mm:ss.SS");
		formats.add("yyyy/MM/dd HH:mm:ss.S");
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
	
	protected List<Object> getValues(Table table, Row row, List<String> sourceColNames) {
		List<Object> res = new ArrayList<Object>();
		for (String col : sourceColNames) {
			Object val = readObject(table, row, col);
			res.add(val);
		}
		return res;
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
    
}