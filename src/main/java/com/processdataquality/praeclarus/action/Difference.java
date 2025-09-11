/*
 * Copyright (c) 2021-2025 Queensland University of Technology
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.deser.std.StringCollectionDeserializer;
import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionValueException;
import com.processdataquality.praeclarus.option.ColumnNameListOption;
import com.processdataquality.praeclarus.option.ListOption;
import com.processdataquality.praeclarus.option.MultiLineOption;
import com.processdataquality.praeclarus.option.Option;
import com.processdataquality.praeclarus.option.TableNameListOption;

import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.selection.Selection;

/**
 * @author Sareh Sadeghianasl
 * @date 29/7/25
 */
@Plugin(name = "Difference", author = "Sareh Sadeghianasl", version = "1.0", synopsis = "Returns the difference of a two of tables: Table 1 - Table 2. The tables must have the same schema)")
public class Difference extends AbstractAction {

	private int numberOfTimestampColumns;

	public Difference() {
		super();
		getOptions().addDefault(new TableNameListOption("Table 1"));
		getOptions().addDefault(new TableNameListOption("Table 2"));
		getOptions().addDefault("#Timestamp columns", 0);
	}

	@Override
	public Table run(List<Table> inputList) throws InvalidOptionValueException {
		long startTime = System.currentTimeMillis();

		if (inputList.size() != 2) {
			throw new InvalidOptionValueException("This action requires exactly two tables as input");
		}

		if (!sameSchema(inputList)) {
			throw new InvalidOptionValueException("Input tables do not have the same schema");
		}

		String tab1 = getSelectedTable("Table 1");
		String tab2 = getSelectedTable("Table 2");

		if (tab1.equals(tab2)) {
			throw new InvalidOptionValueException("Table 1 and Table 2 cannot be the same");
		}

		String rc1 = tab1.substring(tab1.indexOf("(") + 1, tab1.indexOf("r") - 1);
		String rc2 = tab2.substring(tab2.indexOf("(") + 1, tab2.indexOf("r") - 1);

		int r1 = Integer.parseInt(rc1);
		int r2 = Integer.parseInt(rc2);

		Table temp1 = inputList.remove(0);
		Table temp2 = inputList.remove(0);
		temp1 = convertColsToString(temp1);
		temp2 = convertColsToString(temp2);

		
		List<String> timestampColumnNames = new ArrayList<String>();
		for (int i = 1; i <= numberOfTimestampColumns; i++) {
			String colName = getSelectedColumnNameValue("Timestamp Column " + i);
			timestampColumnNames.add(colName);
		}
		temp1 = convertTimeColsToLocalDateTime(temp1,timestampColumnNames);
		temp2 = convertTimeColsToLocalDateTime(temp2,timestampColumnNames);

		Table t1, t2;
		if (temp1.rowCount() == r1) {
			t1 = temp1.sortAscendingOn(temp1.column(0).name());
			t2 = temp2;
		} else {
			t1 = temp2.sortAscendingOn(temp2.column(0).name());
			t2 = temp1;
		}
		

		List<String> colNames = new ArrayList<>(t1.columnNames());
		String[] joinKeys = colNames.toArray(new String[0]);
		
		Table t1LOT2 = t1.joinOn(joinKeys).leftOuter(t2, true, true, joinKeys);
		Table res;
		Selection missingT2 = t1LOT2.column(joinKeys.length).isMissing();
		for (int i = 1; i < joinKeys.length; i++) {
			missingT2 = missingT2
					.and(t1LOT2.column(joinKeys.length + i).isMissing());
		}
		res = t1LOT2.where(missingT2);
		res = res.selectColumns(joinKeys);
		


		
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("Execution time for Difference: " + elapsedTime);

		return res;


	}

	


	@Override
	public void optionValueChanged(Option option) {

		if (option.equals(getOptions().get("#Timestamp columns"))) {
			numberOfTimestampColumns = option.asInt();
			for (int i = 1; i <= numberOfTimestampColumns; i++) {
				getOptions().addDefault(new ColumnNameListOption("Timestamp Column " + i));
			}
		}

		super.optionValueChanged(option);

	}

	@Override
	public int getMaxInputs() {
		return 2;
	}

}
