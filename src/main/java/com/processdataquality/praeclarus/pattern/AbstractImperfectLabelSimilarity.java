/*
 * Copyright (c) 2022 Queensland University of Technology
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

import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

/**
 * Overrides base class to add similarity scores
 * @author Sareh Sadeghianasl, Michael Adams
 * @date 10/2/2022
 */
public abstract class AbstractImperfectLabelSimilarity extends AbstractImperfectLabel {

	protected AbstractImperfectLabelSimilarity() {
		super();
	}


	// To be implemented by subclasses to detect distortion in the selected column of a table
	protected abstract void detect(Table table, StringColumn selectedColumn);


	@Override
	protected void detect(StringColumn column, String s1, String s2) { }


	@Override
	public Table detect(Table table) {
		detect(table, getSelectedColumn(table));
		return _detected;
	}


	/**
	 * Creates the table that will receive the imperfect values detected along with
	 * their calculated similarities.
	 *
	 * @return the empty table
	 */
	@Override
	protected Table createResultTable() {
		return Table.create("Result").addColumns(
				StringColumn.create("Label1"),
				IntColumn.create("Count1"),
				StringColumn.create("Label2"),
				IntColumn.create("Count2"),
				StringColumn.create("Overall Context Similarity"),
				StringColumn.create("String Similarity"),
				StringColumn.create("Control Flow Similarity"),
				StringColumn.create("Resource Similarity"),
				StringColumn.create("Time Similarity"),
				StringColumn.create("Duration Similarity"),
				StringColumn.create("Data Similarity"));
	}


	/**
	 * Adds distorted labels to the results table, as well as the frequency of each
	 * value as contained in the master table
	 *
	 * @param column the column from the master table to count frequencies
	 * @param s1     the label
	 * @param s2     the distorted label
	 * @param os     the overall similarity of s1 and s2 (the average of the
	 *               following context dimension similarities except the string similarity)
	 * @param ss     the string similarity of s1 and s2
	 * @param dcfs   the direct control flow similarity of s1 and s2
	 * @param rs     the resource similarity of s1 and s2
	 * @param ts     the time similarity of s1 and s2
	 * @param dus    the duration similarity of s1 and s2
	 * @param ds     the data attribute similarity of s1 and s2
	 */
	protected void addResult(StringColumn column, String s1, String s2, double os,
							 double ss, double dcfs, double rs, double ts,
							 double dus, double ds) {
		super.addResult(column, s1, s2);
		_detected.stringColumn(4).append(formatDouble(os));
		_detected.stringColumn(5).append(formatDouble(ss));
		_detected.stringColumn(6).append(formatDouble(dcfs));
		_detected.stringColumn(7).append(formatDouble(rs));
		_detected.stringColumn(8).append(formatDouble(ts));
		_detected.stringColumn(9).append(formatDouble(dus));
		_detected.stringColumn(10).append(formatDouble(ds));
	}


	/**
	 * Converts the similarity double score to a string with a well-formatted value.
	 * @param d 	the input double
	 * @return		the formatted string
	 */

	private String formatDouble(double d) {
		if(d == -1) {
			return "N/A";
		}
		if(d == 0) {
			return "0";
		}
		if(d == 1) {
			return "1";
		}
		return String.format("%.3f", d);
	}

}
