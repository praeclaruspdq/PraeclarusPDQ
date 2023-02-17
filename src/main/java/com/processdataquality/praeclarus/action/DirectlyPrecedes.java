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

import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionValueException;
import com.processdataquality.praeclarus.option.ColumnNameListOption;

import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.expression.spel.ast.Selection;

/**
 * @author Sareh Sadeghianasl
 * @date 10/2/2023
 */
@Plugin(name = "Directly Precedes", author = "Sareh Sadeghianasl", version = "1.0", synopsis = "Applies direclty precedes operator to a log")
public class DirectlyPrecedes extends AbstractAction {

	public DirectlyPrecedes() {
		super();
		getOptions().addDefault(new ColumnNameListOption("CaseId Column"));
		getOptions().addDefault(new ColumnNameListOption("Timestamp Column"));
	}

	@Override
	public Table run(List<Table> inputList) throws InvalidOptionValueException {
		if (inputList.size() != 1) {
			throw new IllegalArgumentException("This action requires one table as input.");
		}
		String caseidColName = getSelectedColumnNameValue("CaseId Column");
		String timeColName = getSelectedColumnNameValue("Timestamp Column");
		Table t1 = inputList.remove(0);
		int numColumns = t1.columnCount();
		Table res = Table.create("Result");
		for (int i = 0; i < numColumns; i++) {
			res.addColumns(t1.column(i).copy());
		}
		res.clear();
		for (int i = 0; i < numColumns; i++) {
			res.addColumns(res.column(i).copy().setName("p_" + res.column(i).name()));
		}

		for (Row r : t1) {
			Row maxBefore = getMaxBefore(t1, r, caseidColName, timeColName);
			if (maxBefore != null) {
				for (int i = 0; i < numColumns; i++) {
					res.column(i).appendObj(readObject(t1, r, t1.column(i).name()));
					res.column(numColumns + i).appendObj(readObject(t1, maxBefore, t1.column(i).name()));
				}
			}
		}
		return res;

	}

	private Row getMaxBefore(Table table, Row row, String caseidColName, String timeColName) {
		List<Row> allBefore = new ArrayList<Row>();

		for (int i = 0; i<table.rowCount();i++) {
			if (readObject(table, row, caseidColName).toString().equals(readObject(table, table.row(i), caseidColName).toString())) {
				if (isBefore(readObject(table, table.row(i), timeColName), readObject(table, row, timeColName))) {		
					allBefore.add(table.row(i));
				}
			}
		}
		return findMaxTime(table, allBefore, timeColName);
	}

	private Row findMaxTime(Table table, List<Row> rows, String timeColName) {
		if (rows.isEmpty()) {
			return null;
		}
		Object o = readObject(table,rows.get(0),timeColName);
		Row maxRow = null;
		if (o instanceof LocalDate) {
			LocalDate max = LocalDate.MIN;
			for (Row r : rows) {
				LocalDate t = (LocalDate) readObject(table,r,timeColName);
				if (t.isAfter(max)) {
					max = t;
					maxRow = r;
				}
			}
		} else if (o instanceof LocalDateTime) {
			LocalDateTime max = LocalDateTime.MIN;
			for (Row r : rows) {
				LocalDateTime t = (LocalDateTime) readObject(table,r,timeColName);
				if (t.isAfter(max)) {
					max = t;
					maxRow = r;
				}
			}
		} else if (o instanceof LocalTime) {
			LocalTime max = LocalTime.MIN;
			for (Row r : rows) {
				LocalTime t = (LocalTime) readObject(table,r,timeColName);
				if (t.isAfter(max)) {
					max = t;
					maxRow = r;
				}
			}
		} else if (o instanceof Instant) {
			Instant max = Instant.MIN;
			for (Row r : rows) {
				Instant t = (Instant) readObject(table,r,timeColName);
				if (t.isAfter(max)) {
					max = t;
					maxRow = r;
				}
			}
		} else if (o instanceof String) {
			LocalDateTime max = LocalDateTime.MIN;
			for (Row r : rows) {
				LocalDateTime t = textToTime((String) readObject(table,r,timeColName));
				if (t!= null && t.isAfter(max)) {
					max = t;
					maxRow = r;
				}
			}
		}
		return maxRow;
	}

	private boolean isBefore(Object t1, Object t2) {
		if (t1 instanceof LocalDate && t2 instanceof LocalDate) {
			return ((LocalDate) t1).isBefore(((LocalDate) t2));
		} else if (t1 instanceof LocalDateTime && t2 instanceof LocalDateTime) {
			return ((LocalDateTime) t1).isBefore(((LocalDateTime) t2));
		} else if (t1 instanceof LocalTime && t2 instanceof LocalTime) {
			return ((LocalTime) t1).isBefore(((LocalTime) t2));
		} else if (t1 instanceof Instant && t2 instanceof Instant) {
			return ((Instant) t1).isBefore(((Instant) t2));
		} else if (t1 instanceof String && t2 instanceof String) {
			LocalDateTime time1 = textToTime((String) t1);
			LocalDateTime time2 = textToTime((String) t2);
			if (time1 != null && time2 != null) {
				return time1.isBefore(time2);
			}
		}
		return false;
	}

	

	@Override
	public int getMaxInputs() {
		return 1;
	}

}
