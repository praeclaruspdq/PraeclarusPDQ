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

import com.processdataquality.praeclarus.annotation.Pattern;
import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionException;
import com.processdataquality.praeclarus.option.Options;
import com.processdataquality.praeclarus.support.gameelements.ActivityGroup;
import com.processdataquality.praeclarus.support.logelements.Activity;

import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

/**
 * @author Sareh Sadeghianasl
 * @date 23/5/22
 */
@Plugin(name = "Group Generator", author = "Sareh Sadeghianasl", version = "1.0", synopsis = "Generates activity groups for the Quality Guardian Redux game . "
		+ "The gropus are generated based on contextual similarity of activities in the log")
@Pattern(group = PatternGroup.SYNONYMOUS_LABELS)

public class GroupGeneratorContextual extends AbstractImperfectLabelContextual {

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
		return options;
	}

	protected void detect(Table table, StringColumn selectedColumn, String sortColName) throws InvalidOptionException {

		super.detect(table, selectedColumn, sortColName);
		this.questionBank = createGroups();
		_detected = createResultTable();
		for (ActivityGroup group : this.questionBank) {
			addGroupToResult(selectedColumn, group);
		}
		getAuxiliaryDatasets().put("Questions", _detected);
		getAuxiliaryDatasets().put("Activities", createActivitiesTable());
	}

	private ArrayList<ActivityGroup> createGroups() {
		ArrayList<ActivityGroup> res = new ArrayList<>();
		ArrayList<ArrayList<Activity>> groups = connectedComponents();
		for (ArrayList<Activity> group : groups) {
			if (group.size() > 1) {
				ActivityGroup g = new ActivityGroup(groups.indexOf(group), group, -1);
				res.add(g);
			}
		}

		return res;
	}

	private ArrayList<ArrayList<Activity>> connectedComponents() {
		boolean[] visited = new boolean[parser.getActivities().size()];
		ArrayList<ArrayList<Activity>> res = new ArrayList<>();
		for (int v = 0; v < parser.getActivities().size(); ++v) {
			if (!visited[v]) {
				ArrayList<Activity> newGroup = new ArrayList<>();
				DFSUtil(v, visited, newGroup, parser.getActivities());
				res.add(newGroup);
			}
		}
		return res;
	}

	void DFSUtil(int v, boolean[] visited, ArrayList<Activity> newGroup, ArrayList<Activity> acts) {
		visited[v] = true;
		newGroup.add(acts.get(v));
		for (int i = 0; i < visited.length; i++) {
			if (grouped[v][i] && !visited[i]) {
				DFSUtil(i, visited, newGroup, acts);
			}
		}
	}

	private void addGroupToResult(StringColumn selectedColumn, ActivityGroup g) {
		ArrayList<String> acts = new ArrayList<String>();
		int[] indices = new int[g.getActs().size()];
		for (int i = 0; i < g.getActs().size(); i++) {
			indices[i] = parser.getActivities().indexOf(g.getActs().get(i));
			String act = "(" + indices[i] + ")" + g.getActs().get(i).getName();
			acts.add(act);
		}
		int numPairs = (int) (g.getActs().size() * (g.getActs().size() - 1)) / 2;
		String[] DCFS = new String[numPairs];
		String[] RS = new String[numPairs];
		String[] TS = new String[numPairs];
		String[] DS = new String[numPairs];
		int counter = 0;
		for (int i = 0; i < g.getActs().size(); i++) {
			for (int j = i + 1; j < g.getActs().size(); j++) {		
				DCFS[counter] =  getSimPercent(dcfs[indices[i]][indices[j]]);
				RS[counter] = getSimPercent(rs[indices[i]][indices[j]]);		
				TS[counter] = getTimeAndDurationSimPrecent(ts[indices[i]][indices[j]], ds[indices[i]][indices[j]]);
				DS[counter] = getSimPercent(eds[indices[i]][indices[j]]);
				counter++;
			}
		}
		String[][] sims = { DCFS, RS, TS, DS };
		String[] res = new String[4];
		for (int i = 0; i < res.length; i++) {
			res[i] = "[";
			for (int j = 0; j < sims[i].length; j++) {
				res[i] = res[i] + sims[i][j] + ",";
			}
			if (res[i].length() > 0 && res[i].charAt(res[i].length() - 1) == ',') {
				res[i] = res[i].substring(0, res[i].length() - 1);
			}
			res[i] = res[i] + "]";
		}
		addResult(selectedColumn, g.getId() , acts, res[0], res[1], res[2], res[3]);
	}

	/**
	 * Creates the table that will receive the activity group generated for the game
	 *
	 * @return the empty table
	 */
	@Override
	protected Table createResultTable() {
		Table result = Table.create("Result").addColumns(IntColumn.create("GID"), StringColumn.create("Activity 1"),
				StringColumn.create("Activity 2"));
		int max = getMaxGroupSize();
		for (int i = 2; i < max; i++) {
			result.addColumns(StringColumn.create("Activity " + (i + 1)));
		}
		result.addColumns(StringColumn.create("Control Flow Similarities"),
				StringColumn.create("Resource Similarities"), StringColumn.create("Time Similarities"),
				StringColumn.create("Data Similarities"), IntColumn.create("Topic"),
				IntColumn.create("Level"));
		return result;
	}

	private int getMaxGroupSize() {
		int max = Integer.MIN_VALUE;
		for (ActivityGroup g : this.questionBank) {
			if (g.getActs().size() > max) {
				max = g.getActs().size();
			}
		}
		return max;
	}

	/**
	 * Adds activity groups to the results table
	 *
	 * @param column the column from the master table to count frequencies
	 * @param gid    the group id
	 * @param acts   the list of activity labels in the group
	 */
	protected void addResult(StringColumn column, int gid, ArrayList<String> acts, String dcfs, String rs, String ts,
			String ds) {
		_detected.intColumn(0).append(gid);
		for (int i = 0; i < acts.size(); i++) {
			_detected.stringColumn(i + 1).append(acts.get(i));
		}
		for (int j = acts.size() + 1; j < _detected.columnCount() - 6; j++) {
			_detected.stringColumn(j).append("-");
		}
		_detected.stringColumn(_detected.columnCount() - 6).append(dcfs);
		_detected.stringColumn(_detected.columnCount() - 5).append(rs);
		_detected.stringColumn(_detected.columnCount() - 4).append(ts);
		_detected.stringColumn(_detected.columnCount() - 3).append(ds);
		_detected.intColumn(_detected.columnCount() - 2).append(-1);
		_detected.intColumn(_detected.columnCount() - 1).append(-1);
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
