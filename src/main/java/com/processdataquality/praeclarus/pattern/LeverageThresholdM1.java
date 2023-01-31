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

import java.util.UUID;    

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import com.processdataquality.praeclarus.annotation.Pattern;
import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionException;
import com.processdataquality.praeclarus.exception.OptionException;
import com.processdataquality.praeclarus.option.ColumnNameListOption;
import com.processdataquality.praeclarus.option.Options;
import com.processdataquality.praeclarus.pattern.PatternGroup;
import com.processdataquality.praeclarus.plugin.uitemplate.PluginUI;

import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.selection.Selection;



/**
 * @author Marco Comuzzi
 * @date 20/1/23
 */
@Plugin(
        name = "Leverage M1 (User Threshold)",
        author = "Marco Comuzzi, Jonghyeon Ko",
        version = "0.1",
        synopsis = "Identifies and removes anomalous traces using the leverage method"
        		+ "and using user-specified leverage threshold"
        		+ "see: https://doi.org/10.1016/j.ins.2020.11.017"
)
@Pattern(group = PatternGroup.ANOMALOUS_TRACES)

public class LeverageThresholdM1 extends AbstractAnomalousTrace {
	
	public LeverageThresholdM1() {
        super();
        getOptions().addDefault(new ColumnNameListOption("Case id column"));
        getOptions().addDefault(new ColumnNameListOption("Activity column"));
        getOptions().addDefault("Lev. Threshold", 0.1);
        
    }
	
	protected StringColumn getCaseIdColumn(Table table) throws InvalidOptionException {
        return getSelectedColumn(table,getSelectedColumnNameValue("Case id column"));
    }
	
	protected String getCaseIdColumnLabel(Table table) throws InvalidOptionException{
		return (String) getSelectedColumnNameValue("Case id column");
	}
	
	protected String getActivityColumnLabel(Table table) throws InvalidOptionException{
		return (String) getSelectedColumnNameValue("Activity column");
	}
	
	protected StringColumn getActivity(Table table) throws InvalidOptionException {
        return getSelectedColumn(table,getSelectedColumnNameValue("Activity Column"));
    }
	
	protected String getSelectedColumnNameValue(String name) {
        return ((ColumnNameListOption) getOptions().get(name)).getSelected();
   }
	
	protected StringColumn getSelectedColumn(Table table, String selectedColName)
            throws InvalidOptionException {
        if (table.columnNames().contains(selectedColName)) {
            return (StringColumn) table.column(selectedColName);
        }
        throw new InvalidOptionException("No column named '" + selectedColName + "' in input table");
    }

	
	protected StringColumn getCaseIdColumnAsString(Table table, String caseIdLabel) {
		if (table.column(caseIdLabel).type() == ColumnType.INTEGER) {
        	return table.intColumn(caseIdLabel).asStringColumn();
        	
        }
		else {
			return table.stringColumn(caseIdLabel);
		}
	}
	

    @Override
    public Table detect(Table table) throws InvalidOptionException{
    	
    	_detected = createResultTable();
    	
    	double threshold = getOptions().get("Lev. Threshold").asDouble();
    	
    	
    	
    	Table tb = table;
    	String caseIdLabel = getCaseIdColumnLabel(tb);
        String activityLabel = getActivityColumnLabel(tb);
        
        // create new caseid column of type StringColumn
        
        String caseIdLabelUn = "caseid-anomalous-trace-plugin";
           
        if (table.column(caseIdLabel).type() == ColumnType.INTEGER) {
        	//_log.info("Creating string case id column (from int) ....");
        	StringColumn caseIdColumn = table.column(caseIdLabel).asStringColumn();
        	caseIdColumn.setName(caseIdLabelUn);
        	table.addColumns(caseIdColumn);
        }
        else {
        	//_log.info("Creating string case id column (from string) ....");
        	StringColumn caseIdColumn = (StringColumn) table.column(caseIdLabel).copy();
        	caseIdColumn.setName(caseIdLabelUn);
        	table.addColumns(caseIdColumn);
        }
        
        //_log.info("Number of cases in this log: "+tb.stringColumn(caseIdLabelUn).countUnique());
        
        
        // ONE-HOT ENCODING & ZERO-PADDING 
        //_log.info("Starting one hot encoding and zero padding ....");
        
        List<String> UniqueActivity = tb.stringColumn(activityLabel).unique().asList();
        List<String> UniqueCase = getCaseIdColumnAsString(tb, caseIdLabelUn).unique().asList();
        
   
        Table lenTrace = null;
          
        lenTrace = tb.countBy(tb.stringColumn(caseIdLabelUn));
        
        int maxTrace = (int) lenTrace.intColumn("Count").max();
        //_log.info("DONE");

        long startTime = System.currentTimeMillis();
        
        List<List<Integer>> onehotmatrix = new ArrayList<List<Integer>>();
        int n = UniqueCase.size();
    
        for (int i = 0; i < n; i++) {
            Table Case1 = tb.where(  getCaseIdColumnAsString(tb, caseIdLabelUn).isEqualTo(UniqueCase.get(i)));
        	List<String> Trace1 = Case1.stringColumn(activityLabel).asList();
            
        	
            int size_e = UniqueActivity.size();
            List<Integer> onehot = new ArrayList<Integer>();
            for(String s : Trace1) {
                for(String a : UniqueActivity) {
                    onehot.add(s.equals(a) ? 1 : 0 );
                }
            } 
            int size_z = maxTrace - Trace1.size();
            if(size_z>0){
                List<Integer> zero = Collections.nCopies(size_e*size_z, 0);
                onehot.addAll(zero);
            }
            onehotmatrix.add(onehot);
        }

        double[][] onehotmatrix_o = new double[onehotmatrix.size()][];
        int j=0;
        for (List<Integer> row : onehotmatrix){
            onehotmatrix_o[j++] = row.stream().mapToDouble(Integer::doubleValue).toArray() ;
        }
        
        
        //_log.info("Calculating leverage....");
        // Leverage
        double[][] onehotmatrix_t = IntStream.range(0, onehotmatrix_o[0].length)
            .mapToObj(i -> Stream.of(onehotmatrix_o).mapToDouble(row -> row[i]).toArray())
            .toArray(double[][]::new);
        RealMatrix m_o = MatrixUtils.createRealMatrix(onehotmatrix_o);
        RealMatrix m_t = new Array2DRowRealMatrix(onehotmatrix_t);

        RealMatrix A = m_t.multiply(m_o);
        RealMatrix A_Inverse = new SingularValueDecomposition(A).getSolver().getInverse();
        RealMatrix H_matrix = m_o.multiply(A_Inverse).multiply(m_t);

        List<Double> leverage = new ArrayList<Double>();
        for (int i = 0; i < n; i++) {
            leverage.add(H_matrix.getEntry(i, i) ); // diagonals of H_matrix
        } 


        // Adjusted Leverage
        List<Integer> length = lenTrace.intColumn("Count").asList();

        double mean = 0.0;
        for (int i = 0; i < length.size(); i++) {
            mean += length.get(i);
        }
        mean /= length.size();

        double variance = 0;
        for (int i = 0; i < length.size(); i++) {
            variance += Math.pow(length.get(i) - mean, 2);
        }
        variance /= length.size();
        double std = Math.sqrt(variance);

        List<Double> length_norm = new ArrayList<Double>();
        for (int i = 0; i < length.size(); i++) {
            length_norm.add( 1/(1+Math.exp(-1*(length.get(i)- mean)/std)) );
        }

        List<Double> adj_leverage = new ArrayList<Double>();
        if(-2.2822+ Math.pow(maxTrace,0.3422) <0 | length.stream().distinct().collect(Collectors.toList()).size() ==1 ){
            adj_leverage = leverage;
        }else{
            for (int i = 0; i < length.size(); i++) {
            	// TODO hardcode a treshold andwrite only if adj leverage value aboev threshold
                adj_leverage.add( leverage.get(i)*Math.pow(1-length_norm.get(i), -2.2822+ Math.pow(maxTrace,0.3422)));
            }
        }

        long stopTime = System.currentTimeMillis();
        //_log.info("It took "+Long.toString(stopTime- startTime)+" ms");
        //_log.info("DONE");

       
        // Create output
        StringColumn Case_ID = StringColumn.create("Case ID", UniqueCase);
        // DoubleColumn Score = DoubleColumn.create("Score" , leverage);
        DoubleColumn Adjusted_Score = DoubleColumn.create("Score" , adj_leverage);
        
        // Table output = Table.create("leverage", Case_ID, Adjusted_Score);
       

        
        Table output = Table.create("leverage", Case_ID, Adjusted_Score);
        
        _detected = output;
        
        
        //_log.info("Assembling detected table...");
     
        
        _detected = _detected.where( Adjusted_Score.isGreaterThan(threshold) );
        
        //_log.info("Number of anomalies in this log: "+_detected.rowCount());
        //_log.info("Detect completed");
    	
    	
        return _detected;
    }


	
	
	

}
