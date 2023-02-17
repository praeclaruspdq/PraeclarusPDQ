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

import org.apache.commons.lang3.StringUtils;

import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionValueException;
import com.processdataquality.praeclarus.option.ColumnNameListAndStringOption;
import com.processdataquality.praeclarus.option.Option;

import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

/**
 * @author Sareh Sadeghianasl
 * @date 8/2/23
 */
@Plugin(
        name = "Project",
        author = "Sareh Sadeghianasl",
        version = "1.0",
        synopsis = "Projects a number of columns from a table"
)
public class Project extends AbstractAction {
	
	private int numberOfColumns;

    public Project() {
        super();
        getOptions().addDefault("#Columns", 0);
        numberOfColumns = 0;
    }

    @Override
    public Table run(List<Table> inputList) throws InvalidOptionValueException {
        if (inputList.size() != 1) {
            throw new IllegalArgumentException("This action requires one table as input.");
        }
        Table t1 = inputList.remove(0);
        List<String> colNames = new ArrayList<String> ();
        List<String> newNames = new ArrayList<String> ();
        for(int i = 1; i <=numberOfColumns; i++) {
        	String colName = getSelectedColumn("Column " + i);
        	String newName = getNewName("Column " + i);
        	colNames.add(colName);
        	newNames.add(newName);
        	
        }
        Table t2 = t1.selectColumns(colNames.stream().toArray(String[]::new));
        for (Column c: t2.columns()) {
        	int index = t2.columns().indexOf(c);
        	if(index < newNames.size() && !StringUtils.isEmpty(newNames.get(index))) {
        		c.setName(newNames.get(index));
        	}
        }
        return t2;
        
    }
    
    
    @Override
    public void optionValueChanged(Option option) {
    	if(option.equals(getOptions().get("#Columns"))) {	
    		numberOfColumns = option.asInt();
    		for(int i = 1; i <=numberOfColumns; i++) {
    			getOptions().addDefault(new ColumnNameListAndStringOption("Column " + i));
    		}
    		
    	}
    	super.optionValueChanged(option);
        
    }
    

	@Override
    public int getMaxInputs() {
        return 1;
    }

}
