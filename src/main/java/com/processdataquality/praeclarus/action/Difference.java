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

import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

/**
 * @author Sareh Sadeghianasl
 * @date 21/5/21
 */
@Plugin(name = "Difference", author = "Sareh Sadeghianasl", version = "1.0", synopsis = "Returns the difference of a two of tables (they must have the same schema)")
public class Difference extends AbstractAction {

	public Difference() {
		super();
	}

	@Override
	public Table run(List<Table> inputList) throws InvalidOptionValueException {
		if (inputList.size() != 2) {
			throw new InvalidOptionValueException("This action requires exactly two tables as input.");
		}
		if (!sameSchema(inputList)) {
			throw new InvalidOptionValueException("Input tables do not have the same schema");
		}
		ArrayList<Integer> duplicateRows = new ArrayList<Integer>();
		Table t1 = inputList.remove(0);
		Table t2 = inputList.remove(0);

		for (Row r : t1) {
			if (containsRow(t2, r)) {
				duplicateRows.add(r.getRowNumber());
			}
		}
		int[] rowIndices = new int[duplicateRows.size()];
		for (int i = 0; i < duplicateRows.size(); i++) {
			rowIndices[i] = duplicateRows.get(i);
		}
		Table res = t1.dropRows(rowIndices);

		return res;

	}

	private boolean containsRow(Table table, Row row) {
		for (Row r : table) {
			if (sameRow(table, r, row)) {
				return true;
			}
		}
		return false;
	}

	private boolean sameRow(Table table, Row r1, Row r2) {
		for (int i = 0; i < table.columnCount(); i++) {
			Object o1 = readObject(table, r1, table.column(i).name());
			Object o2 = readObject(table, r2, table.column(i).name());
			if(!equalCells(o1,o2)) {
				return false;
			}
		}
		return true;
	}

	

	@Override
	public int getMaxInputs() {
		return 2;
	}

}
