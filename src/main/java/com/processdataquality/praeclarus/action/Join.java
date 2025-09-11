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
import com.processdataquality.praeclarus.option.ListOption;

import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 25/6/17
 */
@Plugin(name = "Join", author = "Michael Adams & Sareh Sadeghianasl", version = "1.0", synopsis = "Performs a join on a set of tables (they must have a column of the same name)")
public class Join extends AbstractAction {
	
	private ArrayList<String> joinTypes = new ArrayList<String>();

	public Join() {
		super();
		getOptions().addDefault(new ColumnNameListOption("Column"));
		joinTypes.add("Inner");
		joinTypes.add("Left Outer");
		joinTypes.add("Right Outer");
		joinTypes.add("Full Outer");
		
		getOptions().addDefault(
				new ListOption("Join type", joinTypes));
	}

	@Override
	public Table run(List<Table> inputList) throws InvalidOptionValueException {
		long startTime = System.currentTimeMillis();
		
		if (inputList.size() < 2) {
			throw new InvalidOptionValueException("This action requires at least two tables as input.");
		}
		String colName = getSelectedColumnNameValue("Column");
		String JoinType = ((ListOption) getOptions().get("Join type")).getSelected(); 
		
		for (Table t : inputList) {
			if (!t.containsColumn(colName))
				throw new InvalidOptionValueException("The selected column is not a shared column among input tables");
		}
		Table t1 = inputList.remove(0);
		Table t2 = t1.copy();
		Table result;
		if(JoinType.equals("Inner")) {
			result = t2.joinOn(colName).inner(true, inputList.toArray(new Table[] {}));
		}else if(JoinType.equals("Left Outer")) {
			result = t2.joinOn(colName).leftOuter(true, inputList.toArray(new Table[] {}));
		}else if(JoinType.equals("Right Outer")) {
			result = t2.joinOn(colName).rightOuter(true, inputList.toArray(new Table[] {}));
		}else if(JoinType.equals("Full Outer")) {
			result = t2.joinOn(colName).fullOuter(true, inputList.toArray(new Table[] {}));
		}else {
			result = t2.joinOn(colName).inner(true, inputList.toArray(new Table[] {}));
		}
		
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println("Execution time for " + JoinType + ": " + elapsedTime);
		
		return result;
	}

	@Override
	public int getMaxInputs() {
		return 2;
	}

}
