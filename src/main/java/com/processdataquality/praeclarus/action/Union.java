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

import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionValueException;
import com.processdataquality.praeclarus.option.ColumnNameListOption;
import com.processdataquality.praeclarus.option.Option;

import tech.tablesaw.api.Table;
import tech.tablesaw.selection.Selection;

/**
 * @author Sareh Sadeghianasl
 * @date 1/8/2025
 */
@Plugin(name = "Union", author = "Sareh Sadeghianasl", version = "1.0", synopsis = "Returns the union of a set of tables (they must have the same schema)")
public class Union extends AbstractAction {
	
	private int numberOfTimestampColumns;

	public Union() {
		super();
		getOptions().addDefault("#Timestamp columns", 0);
	}

	@Override
	public Table run(List<Table> inputList) throws InvalidOptionValueException {
		long startTime = System.currentTimeMillis();
		if (inputList.size() < 2) {
			throw new InvalidOptionValueException("This action requires at least two tables as input.");
		}
		if (!sameSchema(inputList)) {
			throw new InvalidOptionValueException("Input tables do not have the same schema");
		}
		
		List<String> timestampColumnNames = new ArrayList<String>();
		for (int i = 1; i <= numberOfTimestampColumns; i++) {
			String colName = getSelectedColumnNameValue("Timestamp Column " + i);
			timestampColumnNames.add(colName);
		}

		Table t1 = inputList.remove(0);
		t1 = convertColsToString(t1);
		t1 = convertTimeColsToLocalDateTime(t1,timestampColumnNames);
		
		for (Table t : inputList) {
			Table temp = t.copy();
			temp = convertColsToString(temp);
			temp = convertTimeColsToLocalDateTime(temp,timestampColumnNames);
			List<String> colNames = new ArrayList<>(t1.columnNames());
			String[] joinKeys = colNames.toArray(new String[0]);
			
			temp= temp.joinOn(joinKeys).leftOuter(t1, true, true, joinKeys);
			Selection missingT1 = temp.column(joinKeys.length).isMissing();
			for (int i = 1; i < joinKeys.length; i++) {
				missingT1 = missingT1
						.and(temp.column(joinKeys.length + i).isMissing());
			}
			Table res = temp.where(missingT1);
			t1.append(res);
		}

		
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println("Execution time for Union: " + elapsedTime);
		
		return t1;

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
