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

import java.util.ArrayList;

import com.processdataquality.praeclarus.exception.InvalidOptionException;
import com.processdataquality.praeclarus.option.ColumnNameListOption;
import com.processdataquality.praeclarus.option.Options;
import com.processdataquality.praeclarus.support.gameelements.GameUser;
import com.processdataquality.praeclarus.support.logelements.Activity;
import com.processdataquality.praeclarus.support.math.Pair;

import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

/**
 * Overrides base class to add game repair functions
 * 
 * @author Sareh Sadeghianasl
 * @date 10/2/2022
 */
public abstract class AbstractImperfectLabelGameRepair extends AbstractImperfectLabel {

	protected ArrayList<Activity> activities;
	protected ArrayList<GameUser> users;
	protected Table actTable, userTable, repairsTable;
	protected double[][] crowdWeightedViews;
	protected ArrayList<ArrayList<Pair<String, Double>>> repairsWeighted;
	protected ArrayList<Pair<String, String>> approvedRepairs;
	protected int[][] crowdViews;
	protected ArrayList<ArrayList<Pair<String, Integer>>> repairs;
	
	protected AbstractImperfectLabelGameRepair() {
		super();
		addDefaultOptions();
		repairsTable = Table.create("Repairs").addColumns(StringColumn.create("Label1"),StringColumn.create("Label2"));
	}

	protected void addDefaultOptions() {
		Options options = super.getOptions();
		options.addDefault(new ColumnNameListOption("Sort Column"));
	}


	@Override
	public Table repair(Table master) throws InvalidOptionException {
		String colName = getSelectedColumnNameValue("Column Name");
        StringColumn repaired = getSelectedColumn(master, colName);
        for (Row row : repairsTable) {
            repaired = repaired.replaceAll(
                    row.getString("Label1"), row.getString("Label2"));
        }
        repaired.setName(colName);
        master.replaceColumn(colName, repaired);
		return master;
	}

	
	protected void readActs(Table table) {
		activities = new ArrayList<Activity>();
		for(Row row: table) {
			int index = row.getInt("ID"); 
			String name = row.getString("Label");
			activities.add(new Activity(name, index));
		}
	}
	
	protected void readUsers(Table table) {
		users = new ArrayList<GameUser>();
		for(Row row: table) {
			String username = row.getString("Username");
			String role = row.getString("Role");
			String xplevel = row.getString("XPLevel");
			String knlevel = row.getString("KnLevel");
			users.add(new GameUser(username, knlevel, xplevel, role));
		}
	}
	
	protected void instantiateRepairDatasets() {
		int nDA = activities.size();
		crowdViews = new int[nDA][nDA];
		crowdWeightedViews = new double[nDA][nDA];
		repairs = new ArrayList<>();
		repairsWeighted = new ArrayList<>();
		for (int i = 0; i < nDA; i++) {
			repairs.add(new ArrayList<>());
			repairsWeighted.add(new ArrayList<>());
		}
		approvedRepairs = new ArrayList<>();
	}

	protected void populateApprovedRepairs() {
		Pair<Activity, String> repairWithMaxVote = findMaxVotedRepair();
		while (repairWithMaxVote != null && !approvedBefore(repairWithMaxVote)) {
			Pair<String, String> pair = new Pair<String, String>(repairWithMaxVote.getKey().getName(),
					repairWithMaxVote.getValue());
			approvedRepairs.add(pair);
			int index = activities.indexOf(repairWithMaxVote.getKey());
			repairsWeighted.get(index).clear();

			for (int j = 0; j < activities.size(); j++) {
				if (index != j) {
					int lowerI = j;
					int higherI = index;
					if (j > index) {
						higherI = j;
						lowerI = index;
					}
					double c = crowdWeightedViews[lowerI][higherI];
					if (c >= getOptions().get("High Vote Threshold").asDouble()) {
						Pair<Activity, String> p = new Pair<Activity, String>(activities.get(j),
								repairWithMaxVote.getValue());
						if (!approvedBefore(p) && !p.getKey().getName().equals(p.getValue())) {
							Pair<String, String> pair1 = new Pair<String, String>(p.getKey().getName(), p.getValue());
							approvedRepairs.add(pair1);
							int index1 = activities.indexOf(p.getKey());
							repairsWeighted.get(index1).clear();
						}
					} else if (c < getOptions().get("Low Vote Threshold").asDouble()) {

						for (Pair<String, Double> p : repairsWeighted.get(j)) {
							if (p.getKey().equals(repairWithMaxVote.getValue())) {
								repairsWeighted.get(j).remove(p);
								break;
							}
						}
					}
				}
			}
			repairWithMaxVote = findMaxVotedRepair();
		}
		
		int apCount = 0;
		for (Pair<String, String> p : approvedRepairs) {
			repairsTable.stringColumn(0).append(p.getKey());
			repairsTable.stringColumn(1).append(p.getValue());
			apCount++;
		}

	}
	
	protected Pair<Activity, String> findMaxVotedRepair() {
		double max = Double.MIN_VALUE;
		Activity maxActivity = null;
		String maxRepair = null;
		for (ArrayList<Pair<String, Double>> rL : repairsWeighted) {
			int index = repairsWeighted.indexOf(rL);
			for (Pair<String, Double> r : rL) {
				Activity a = activities.get(index);
				String repair = r.getKey();
				if (r.getValue() > max && !a.getName().equals(repair)
						&& !approvedBefore(new Pair<Activity, String>(a, repair))) {
					max = r.getValue();
					maxActivity = a;
					maxRepair = repair;
				}
			}
		}
		if (max >= getOptions().get("High Vote Threshold").asDouble() &&
				 maxActivity != null && maxRepair != null) {
			return new Pair<Activity, String>(maxActivity, maxRepair);
		}
		return null;
	}
	
	private boolean approvedBefore(Pair<Activity, String> p) {
		String s1 = "", s2 = "", s3 = "", s4 = "";
		for (Pair<String, String> p1 : approvedRepairs) {
			s1 = p1.getKey();
			s2 = p.getKey().getName();
			s3 = p1.getValue();
			s4 = p.getValue();
			if (s1.equals(s2)) {// && s3.equals(s4)) {
				return true;
			} else if (s3.equals(s2)) { // e.g. if exists get review 3 --> get review 1, then get review 1 --> x is not
										// acceptable
				return true;
			}
		}
		return false;
	}
	
	protected double computeWeightedContribution(String username) {
		GameUser u = getUser(username);
		if (u != null) {
			double lv = u.getKNValue();
			double rv = u.getRoleValue();
			double res = (lv + rv) / 2;
			return res;
		}
		return 1;
	}
	
	protected GameUser getUser(String username) {
		for (GameUser u : users) {
			if (u.getUsername().equals(username)) {
				return u;
			}
		}
		return null;
	}
	
	protected int getActivityIndex(String temp) {
		for (int i = 0; i < activities.size(); i++) {
			if (activities.get(i).getName().equals(temp)) {
				return i;
			}
		}
		return -1;
	}

	protected String getSortColumnName(Table table) throws InvalidOptionException {
		String colName = getSelectedColumnNameValue("Sort Column");
		if (!table.columnNames().contains(colName)) {
			throw new InvalidOptionException("No column named '" + colName + "' in table");
		}
		return colName;
	}


}
