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

package com.processdataquality.praeclarus.pattern.TimestampQuality;

import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import com.processdataquality.praeclarus.exception.InvalidOptionException;
import com.processdataquality.praeclarus.exception.OptionException;
import com.processdataquality.praeclarus.option.ColumnNameListOption;
import com.processdataquality.praeclarus.option.Options;
import com.processdataquality.praeclarus.pattern.AbstractDataPattern;
import com.processdataquality.praeclarus.plugin.uitemplate.PluginUI;

import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.Table;

/**
 * A base class for imperfect label plugins
 * @author Dominik Fischer
 * @date 17/08/22
 */

public abstract class TimestampPattern extends AbstractDataPattern {

    protected PluginUI _ui;
    protected Options options;
	private  LinkedHashMap<Error, List<Error>> errorList = new LinkedHashMap<Error, List<Error>>();
	private double score;
	private String qualityLevel;
	private String eventLogLevel;
	private String dimension;
	private String name;
	private DefaultTableModel errorTableModel;
	private DefaultTableModel errorTableModelLayerTwo;
	private String details;
	private int scoreWeight = 1;
	private boolean used = true;
	private double relativeWeight = 1;
	protected Table _detected;

    protected TimestampPattern() {
        super();
		getOptions().addDefault(new ColumnNameListOption("Timestamp Column"));
    }

	@Override
    public boolean canRepair() {
        return false;
    }

	public Table detect(Table table) throws OptionException {
		createErrorTableModel(table);
		detectErrors(table);
		return _detected;
	}

	@Override
    public Table repair(Table master) throws OptionException {
        // TODO Auto-generated method stub
        return null;
    }

	public DateTimeColumn getSelectedColumn(Table table, String selectedColName) throws InvalidOptionException {
        if (table.columnNames().contains(selectedColName)) {
            return (DateTimeColumn) table.column(selectedColName);
        }
        throw new InvalidOptionException("No column named '" + selectedColName + "' in input table");
    }

    public String getSelectedColumnNameValue(String name) {
        return ((ColumnNameListOption) getOptions().get(name)).getSelected();
    }

	public void calculatePreliminaryAttributeScore(Table _detected) {
		// TODO Auto-generated method stub
	}

	public void calculateScore() {
		// TODO Auto-generated method stub
		
	}

	public void detectErrors(Table table) {
		// TODO Auto-generated method stub
		
	}

	public void calculateDimensionScore(List<TimestampPattern> metricsList) {
		double scoreSum = 0;
		double weightSum = 0;
		for (TimestampPattern metric : metricsList) {
			if (metric.isUsed()) {
				scoreSum = scoreSum + metric.relativeWeight*metric.getScore();
				weightSum = weightSum + metric.relativeWeight;
			}
		}
		setScore(scoreSum/weightSum);
		setQualityLevel(score);
	
	}
	
	public void setQualityLevel(double score) {
		if (score > (double) 3/4) {
			this.qualityLevel = "High";
		}
		else if (score > (double) 1/4) {
			this.qualityLevel = "Medium";
		} 
		else {
			this.qualityLevel = "Low";
		}
	}
	
	public void createErrorTableModel(Table table) {
		
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		if (score > 1) {
			this.score= 1;
		}
		else if (score < 0) {
			this.score = 0;
		} 
		else {
			this.score = (double) Math.round(Math.pow(score, scoreWeight) * 1000000d)/1000000d;
		}
		setQualityLevel(this.score);
	}
	
	public DefaultTableModel createErrorTableModelLayerTwo(Error error) {
		return errorTableModelLayerTwo;
	}
	
	public DefaultTableModel getErrorTableModelLayerTwo() {
		return errorTableModelLayerTwo;
	}

	public void setErrorTableModelLayerTwo(DefaultTableModel errorTableModelLayerTwo) {
		this.errorTableModelLayerTwo = errorTableModelLayerTwo;
	}

	public String getQualityLevel() {
		return qualityLevel;
	}

	public Table getErrorTable() {
		return _detected;
	}

	public void setErrorTable(Table table) {
		this._detected = table;
	}

	public int getScoreWeight() {
		return scoreWeight;
	}

	public void setScoreWeight(int scoreWeight) {
		this.scoreWeight = scoreWeight;
	}

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

	public double getRelativeWeight() {
		return relativeWeight;
	}

	public void setRelativeWeight(double relativeWeight) {
		this.relativeWeight = relativeWeight;
	}

	public void setQualityLevel(String qualityLevel) {
		this.qualityLevel = qualityLevel;
	}

	public String getEventLogLevel() {
		return eventLogLevel;
	}

	public void setEventLogLevel(String eventLogLevel) {
		this.eventLogLevel = eventLogLevel;
	}

	public String getDimension() {
		return dimension;
	}

	public void setDimension(String dimension) {
		this.dimension = dimension;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DefaultTableModel getErrorTableModel() {
		return errorTableModel;
	}

	public void setErrorTableModel(DefaultTableModel errorTableModel) {
		this.errorTableModel = errorTableModel;
	}
	
	public LinkedHashMap<Error, List<Error>> getErrorList() {
		return errorList;
	}

	public void setErrorList(LinkedHashMap<Error, List<Error>> errorList) {
		this.errorList = errorList;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public class Error {
		private Object errorObject;
		private boolean isWhitelisted = false;
		
		public Error(Object errorObject, boolean isWhitelisted) {
			super();
			this.setErrorObject(errorObject);
			this.setWhitelisted(isWhitelisted);
		}

		public Object getErrorObject() {
			return errorObject;
		}

		public void setErrorObject(Object errorObject) {
			this.errorObject = errorObject;
		}

		public boolean isWhitelisted() {
			return isWhitelisted;
		}

		public void setWhitelisted(boolean isWhitelisted) {
			this.isWhitelisted = isWhitelisted;
		}
	}	


}