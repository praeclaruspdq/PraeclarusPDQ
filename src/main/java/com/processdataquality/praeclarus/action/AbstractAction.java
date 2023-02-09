/*
 * Copyright (c) 2021-2022 Queensland University of Technology
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

import com.processdataquality.praeclarus.exception.InvalidOptionValueException;
import com.processdataquality.praeclarus.option.ColumnNameListAndStringOption;
import com.processdataquality.praeclarus.option.ColumnNameListOption;
import com.processdataquality.praeclarus.option.ListOption;
import com.processdataquality.praeclarus.plugin.AbstractPlugin;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
		case "BOOLEAN":
			res = String.valueOf(row.getBoolean(colName));
			break;
		case "DATE":
			res = String.valueOf(row.getDate(colName));
			break;
		default:
			res = row.getString(colName);
			break;
		}

		return res;
	}

}
