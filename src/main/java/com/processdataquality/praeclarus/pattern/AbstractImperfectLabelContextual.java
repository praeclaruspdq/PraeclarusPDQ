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

import com.processdataquality.praeclarus.plugin.Options;
import com.processdataquality.praeclarus.support.activitysimilaritymeasures.*;
import com.processdataquality.praeclarus.support.logelements.Activity;
import com.processdataquality.praeclarus.support.logelements.ParseTable;
import com.processdataquality.praeclarus.support.math.Pair;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.ArrayList;

/**
 * Overrides base class to add similarity scores
 * @author Sareh Sadeghianasl, Michael Adams
 * @date 10/2/2022
 */
public abstract class AbstractImperfectLabelContextual extends AbstractImperfectLabel {

	private double[][] rs, ds, ts, dcfs, eds, ls;


	protected AbstractImperfectLabelContextual() {
		super();
	}

	@Override
	public Options getOptions() {
		Options options = super.getOptions();

		// TODO define limit for each option

		if (!options.containsKey("Direct Control Flow Noise Threshold")) {
			options.addDefault("Direct Control Flow Noise Threshold", 0.05);
		}
		if (!options.containsKey("Overall Context Similarity Threshold")) {
			options.addDefault("Overall Context Similarity Threshold", 0.6);
		}

		return options;
	}

	
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
		Table result = super.createResultTable();
		result.addColumns(
				StringColumn.create("Overall Context Similarity"),
				StringColumn.create("String Similarity"),
				StringColumn.create("Control Flow Similarity"),
				StringColumn.create("Resource Similarity"),
				StringColumn.create("Time Similarity"),
				StringColumn.create("Duration Similarity"),
				StringColumn.create("Data Similarity")
		);
		return result;
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


	protected void detect(Table table, StringColumn selectedColumn) {
		ParseTable parser = new ParseTable(table, selectedColumn.name());
		parser.parse();
		rs = new ResourceSimilarity(parser.getActivities()).getSimilarity();
		ds = new DurationSimilarity(parser.getActivities()).getSimilarity();
		ts = new TimeSimilarity(parser.getActivities()).getSimilarity();
		dcfs = new ControlFlowSimilarity(parser.getActivities(), parser.getTraces(),
				getOptions().get("Direct Control Flow Noise Threshold").asDouble()).getDirectControlFlowSimilarity();
		eds = new EventDataSimilarity(parser.getActivities()).getSimilarity();
		ls = new StringSimilarity(parser.getActivities()).getSimilarity();

		ArrayList<Pair<Activity, Activity>> res = new ArrayList<Pair<Activity, Activity>>();
		double[][] grouped = new double[parser.getActivities().size()][parser.getActivities().size()];
		double[][] activityContextSimilariy = new double[parser.getActivities().size()][parser.getActivities().size()];
		for (int i = 0; i < parser.getActivities().size(); i++) {
			for (int j = 0; j < parser.getActivities().size(); j++) {
				Activity a1 = parser.getActivities().get(i);
				Activity a2 = parser.getActivities().get(j);
				if (j > i) {
					double overS = overallSimilarity(i, j);
					activityContextSimilariy[i][j] = overS;
					if (overS > getOptions().get("Overall Context Similarity Threshold").asDouble() &&
							ls[i][j]> getOptions().get("String Similarity Threshold").asDouble()) {
						res.add(new Pair<Activity, Activity>(a1, a2));
						addResult(selectedColumn, a1.getName(), a2.getName(), overS, ls[i][j], dcfs[i][j], rs[i][j], ts[i][j], ds[i][j], eds[i][j]);
						grouped[i][j] = overS;
					}
				} else if (j < i) {
					activityContextSimilariy[i][j] = activityContextSimilariy[j][i];
				} else {
					activityContextSimilariy[i][j] = 1;
				}
			}
		}
	}

	/**
	 * Calculates the average context similarity of activity labels a1 and a2 based
	 * on the dimension weights in the options
	 *
	 * @param i the index of a1
	 * @param j the index of a2
	 * @return the average context similarity
	 */

	private double overallSimilarity(int i, int j) {
		double score = 0;
		double duW = getOptions().get("Duration Similarity Weight").asInt();
		double tW = getOptions().get("Time Similarity Weight").asInt();
		double rW = getOptions().get("Resource Similarity Weight").asInt();
		double dcfW = getOptions().get("Control Flow Similarity Weight").asInt();
		double edW = getOptions().get("Data Similarity Weight").asInt();
		double duScore = ds[i][j];
		double rScore = rs[i][j];
		double dcfScore = dcfs[i][j];
		double edScore = eds[i][j];
		double tScore = ts[i][j];
		if (duScore == -1)
			duW = 0;
		if (rScore == -1)
			rW = 0;
		if (edScore == -1)
			edW = 0;
		if (tScore == -1)
			tW = 0;
		if (duW + rW + dcfW + edW + tW != 0) {
			score = ((duW * duScore) + (rW * rScore) + (dcfW * dcfScore) + (edW * edScore) + (tW * tScore))
					/ (duW + rW + dcfW + edW + tW);
		} else {
			duW = rW = edW = tW = dcfW = 1;
			score = ((duW * duScore) + (rW * rScore) + (dcfW * dcfScore) + (edW * edScore) + (tW * tScore))
					/ (duW + rW + dcfW + edW + tW);
		}
		return score;
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
