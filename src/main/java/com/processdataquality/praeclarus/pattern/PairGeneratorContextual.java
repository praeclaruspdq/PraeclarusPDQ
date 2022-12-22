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

import java.util.ArrayList;
import java.util.Collections;

import com.processdataquality.praeclarus.annotation.Pattern;
import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionException;
import com.processdataquality.praeclarus.option.Options;
import com.processdataquality.praeclarus.support.gameelements.ActivityGraph;
import com.processdataquality.praeclarus.support.gameelements.ActivityGroup;
import com.processdataquality.praeclarus.support.logelements.Activity;

import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

/**
 * @author Sareh Sadeghianasl
 * @date 23/5/22
 */
@Plugin(name = "Pair Generator", author = "Sareh Sadeghianasl", version = "1.0", synopsis = "Generates activity pairs for the Quality Guardian Rosebud game . "
		+ "The pairs are generated based on contextual similarity of activities in the log")
@Pattern(group = PatternGroup.SYNONYMOUS_LABELS)

public class PairGeneratorContextual extends AbstractImperfectLabelContextual {

	ArrayList<ActivityGroup> questionBank;

	@Override
	protected void detect(StringColumn column, String s1, String s2) {
	}

	@Override
	public Options getOptions() {
		Options options = super.getOptions();

		// TODO define limit for each option
		options.addDefault("String Similarity Threshold", 0.5);
		options.addDefault("Control Flow Similarity Weight", 1);
		options.addDefault("Resource Similarity Weight", 1);
		options.addDefault("Data Similarity Weight", 1);
		options.addDefault("Time Similarity Weight", 1);
		options.addDefault("Duration Similarity Weight", 1);
		options.addDefault("Number of pairs", 36);
		return options;
	}

	protected void detect(Table table, StringColumn selectedColumn, String sortColName) throws InvalidOptionException {
		_detected = createResultTable();
		super.detect(table, selectedColumn, sortColName);
		ActivityGraph graph = new ActivityGraph(parser.getActivities().size());
		ArrayList<ActivityGroup> pairs = convertToList();
		Collections.sort(pairs);
		createPairs(graph, selectedColumn, pairs);
		getAuxiliaryDatasets().put("Questions", _detected);
		getAuxiliaryDatasets().put("Activities", createActivitiesTable());
	}

	private ArrayList<ActivityGroup> convertToList() {
		ArrayList<ActivityGroup> res = new ArrayList<>();
		int n = 0;
		for (int i = 0; i < grouped.length; i++) {
			for (int j = i+1; j < grouped[i].length; j++) {
				if (grouped[i][j]) {
					ArrayList<Activity> p = new ArrayList<Activity>();
					p.add(parser.getActivities().get(i));
					p.add(parser.getActivities().get(j));
					ActivityGroup g = new ActivityGroup(n, p, activityContextSimilariy[i][j]);
					n++;
					res.add(g);
				}
			}
		}
		return res;
	}

	private void createPairs(ActivityGraph graph, StringColumn selectedColumn,ArrayList<ActivityGroup> pairs) {
		int n = 0;
		this.questionBank = new ArrayList<ActivityGroup>();
		for (ActivityGroup ag : pairs) {
			if (n < getOptions().get("Number of pairs").asInt()) {
				graph.addEdge(parser.getActivities().indexOf(ag.getActs().get(0)), parser.getActivities().indexOf(ag.getActs().get(1)));
				if (graph.isCyclic()) {
					graph.removeEdge(parser.getActivities().indexOf(ag.getActs().get(0)), parser.getActivities().indexOf(ag.getActs().get(1)));
				} else {
					ag.setId(n);
					this.questionBank.add(ag);
					addGroupToResult(selectedColumn, ag);
					n++;
				}
			}
		}
	}

	private void addGroupToResult(StringColumn selectedColumn, ActivityGroup g) {
		int i = parser.getActivities().indexOf(g.getActs().get(0));
		int j = parser.getActivities().indexOf(g.getActs().get(1));
		String act1 = "(" + i + ")" + g.getActs().get(0).getName();
		String act2 = "(" + j + ")" + g.getActs().get(1).getName();
		addResult(selectedColumn,  g.getId(), act1, act2, dcfs[i][j], rs[i][j], ts[i][j],ds[i][j], eds[i][j]);

	}

	/**
	 * Creates the table that will receive the activity group generated for the
	 * game, as well as their context similarities
	 *
	 * @return the empty table
	 */
	@Override
	protected Table createResultTable() {
		Table result = Table.create("Result").addColumns(
				IntColumn.create("GID"), StringColumn.create("Activity 1"),
				StringColumn.create("Activity 2"), StringColumn.create("Control Flow Similarity"),
				StringColumn.create("Resource Similarity"), StringColumn.create("Time Similarity"),
				StringColumn.create("Data Similarity"));
		return result;
	}

	/**
	 * Adds activity groups to the results table
	 *
	 * @param column the column from the master table to count frequencies
	 * @param gid    the group id
	 * @param act1   the first activity label in the group
	 * @param act2   the second activity label in the group
	 * @param dcfs   the direct control flow similarity of act1 and act2
	 * @param rs     the resource similarity of act1 and act2
	 * @param ts     the time similarity of act1 and act2
	 * @param eds     the data attribute similarity of act1 and act2
	 */
	protected void addResult(StringColumn column, int gid, String act1, String act2, double dcfs,
			double rs, double ts, double ds, double eds) {
		_detected.intColumn(0).append(gid);
		_detected.stringColumn(1).append(act1);
		_detected.stringColumn(2).append(act2);
		_detected.stringColumn(3).append(getSimPercent(dcfs));
		_detected.stringColumn(4).append(getSimPercent(rs));
		_detected.stringColumn(5).append(getTimeAndDurationSimPrecent(ts, ds));
		_detected.stringColumn(6).append(getSimPercent(eds));
	}

	/**
	 * This plugin cannot repair a log, it only detects activity groups
	 * 
	 * @return false
	 */

	@Override
	public boolean canRepair() {
		return true;
	}

}
