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

package com.processdataquality.praeclarus.pattern;

import java.util.List;
import java.util.logging.Logger;

import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionException;
import com.processdataquality.praeclarus.exception.OptionException;
import com.processdataquality.praeclarus.plugin.uitemplate.ButtonAction;
import com.processdataquality.praeclarus.plugin.uitemplate.PluginUI;
import com.processdataquality.praeclarus.plugin.uitemplate.UIButton;
import com.processdataquality.praeclarus.plugin.uitemplate.UIContainer;
import com.processdataquality.praeclarus.plugin.uitemplate.UITable;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

public abstract class AbstractAnomalousTrace extends AbstractDataPattern {
	
	protected Table _detected;
	protected Logger _log;

    protected AbstractAnomalousTrace() {
        super();
        _detected = createResultTable();
        _log = Logger.getLogger(this.getName());
       
    }
    

    @Override
	public Table detect(Table table) throws OptionException {
		// TODO Auto-generated method stub
		return _detected;
	}

	
	
	/**
     * Repair instances of an anomalous trace by removing the traces corresponding to
     * the anomalous case ids detected
     * @param master the original input table
     * @return        a table where anomalous traces are removed
     */
    @Override
    public Table repair(Table master) throws InvalidOptionException {
    	
    	
    	_log.info("Starting repairing...");
    	
    	Table filtered = null;
    	
    	filtered = master.where(
      		 master.stringColumn("caseid-anomalous-trace-plugin").isNotIn(getRepairs().stringColumn("Case ID")));
        		 
    	_log.info("Number of cases in this repaired log: "+filtered.stringColumn("caseid-anomalous-trace-plugin").countUnique());
    	
    	
        return filtered;
        
    }
    
    
    /**
     * Gets the table with the repair rows (to be performed)
     */
    public Table getRepairs() {
        List<UITable> tables = _ui.extractTables();

        // only one UITable component for this ui
        return tables.get(0).getTable();
    }
	
	/**
     * Creates the table that will receive the imperfect values detected
     * @return the empty table
     */
    protected Table createResultTable() {
        return Table.create("Result").addColumns(
                StringColumn.create("Case ID"),
                DoubleColumn.create("Anomaly Score")
               
        );
    }
	
    /**
     * Adds a key-value pair to the results table, as well as the frequency of each value
     * as contained in the master table
     * @param caseId teh id of an anomalous case
     * @param ascore the anomaly score of the case 
     */
    protected void addResult(String caseId, double ascore) {
    	
    	if (! _detected.stringColumn(0).contains(caseId)) {
      
    		_detected.stringColumn(0).append(caseId);
    		_detected.doubleColumn(1).append(ascore);
    	}
        
    }
    
    
    
    @Override
    public PluginUI getUI() {
        if (_ui == null) {
            String title = getClass().getAnnotation(Plugin.class).name() + " - Detected";
            _ui = new PluginUI(title);

            UITable table = new UITable(_detected);
            table.setMultiSelect(true);
            UIContainer tableLayout = new UIContainer();
            tableLayout.add(table);
            _ui.add(tableLayout);

            UIContainer buttonLayout = new UIContainer(UIContainer.Orientation.HORIZONTAL);
            buttonLayout.add(new UIButton(ButtonAction.CANCEL));
            buttonLayout.add(new UIButton(ButtonAction.REPAIR));
            _ui.add(buttonLayout);
        }
        return _ui;
   }
	

}
