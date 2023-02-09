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
import java.util.StringTokenizer;

import com.processdataquality.praeclarus.annotation.Pattern;
import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionException;
import com.processdataquality.praeclarus.option.Options;
import com.processdataquality.praeclarus.support.gameelements.ActivityBoard;
import com.processdataquality.praeclarus.support.gameelements.GroupAnswer;
import com.processdataquality.praeclarus.support.logelements.Activity;
import com.processdataquality.praeclarus.support.math.Pair;

import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

/**
 * @author Sareh Sadeghianasl
 * @date 23/5/22
 */
@Plugin(name = "Log Repair QG2", author = "Sareh Sadeghianasl", version = "1.0", synopsis = "Repairs event log using the anwers of the Quality Gaurdian Redux game")
@Pattern(group = PatternGroup.SYNONYMOUS_LABELS)

public class LogRepairQG2 extends AbstractImperfectLabelGameRepair {

	ArrayList<ActivityBoard> boards;
	ArrayList<GroupAnswer> answerBank;
	Table qTable, ansTable;

	public LogRepairQG2() {
		super();
	}

	@Override
	protected void detect(StringColumn column, String s1, String s2) {
	}

	@Override
	public Options getOptions() {
		Options options = super.getOptions();
		// TODO define limit for each option
		options.addDefault("High Vote Threshold", 2.0);
		options.addDefault("Low Vote Threshold", -2.0);
		return options;
	}

	@Override
	public Table repair(Table master) throws InvalidOptionException {
		actTable = getAuxiliaryDatasets().getTable("Activities");
		userTable = getAuxiliaryDatasets().getTable("Users");
		qTable = getAuxiliaryDatasets().getTable("Questions");
		ansTable = getAuxiliaryDatasets().getTable("Answers2");

		readActs(actTable);
		readUsers(userTable);
		readQuestions(qTable);
		readAnswers(ansTable);

		instantiateRepairDatasets();
		
		populateMatrices();
		populateApprovedRepairs();

		return super.repair(master);
	}

	public void readQuestions(Table table) {
		boards = new ArrayList<ActivityBoard>();
		int numActs = table.columnCount() - 7;
		int currTopicID = table.rowCount() > 0 ? table.row(0).getInt("Topic") : -1;
		int prevTopicID = currTopicID;
		int currLevel = table.rowCount() > 0 ? table.row(0).getInt("Level") : -1;
		int prevLevel = currLevel;
		ActivityBoard firstBoard = new ActivityBoard(currTopicID, currLevel);
		boards.add(firstBoard);
		for (Row row : table) {
			currTopicID = row.getInt("Topic");
			currLevel = row.getInt("Level");
			int boardIndex = findBoardIndex(currTopicID, currLevel);
			if (currTopicID != prevTopicID && currLevel != prevLevel) {
				ActivityBoard ab = new ActivityBoard(currTopicID, currLevel);
				boards.add(ab);
			}
			for (int i = 0; i < numActs; i++) {
				String actTemp = row.getString(i + 1);
				if (!actTemp.startsWith("-")) {
					String actIndexTemp = actTemp.substring(0, actTemp.indexOf(")") + 1);
					String actIndex = actIndexTemp.substring(1, actIndexTemp.length() - 1);
					int index = Integer.valueOf(actIndex);
					Activity act = null;
					if (index >= 0 && index < activities.size()) {
						act = activities.get(index);
					}
					boards.get(boardIndex).addAct(act);
				}
			}

			prevTopicID = currTopicID;
			prevLevel = currLevel;
		}
	}

	private int findBoardIndex(int topicID, int level) {
		for (ActivityBoard ab : boards) {
			if (ab.getTopicId() == topicID && ab.getLevel() == level) {
				return boards.indexOf(ab);
			}
		}
		return boards.size() - 1;
	}

	public void readAnswers(Table table) {
		answerBank = new ArrayList<GroupAnswer>();
		for (Row row : table) {
			String username = row.getString("Username");
			String detectionsTemp = row.getString("Detections");
			ArrayList<String> detections = createDetectionsString(detectionsTemp);
			String repair = row.getString("Repair");
			if (repair.equals("NULL") || repair.isEmpty()) {
				repair = null;
			}
			int topicId = row.getInt("Topic");
			int level = row.getInt("Level");
			answerBank.add(new GroupAnswer(username, detections, repair, topicId, level));
		}
	}

	private ArrayList<String> createDetectionsString(String detectionsTemp) {
		ArrayList<String> res = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(detectionsTemp, ";");
		String detection;
		while (st.hasMoreTokens()) {
			String next = st.nextToken();
			detection = next.substring(1, next.length() - 1);
			res.add(detection);
		}
		return res;
	}

	private void populateMatrices() {
		ArrayList<ArrayList<GroupAnswer>> aobs = groupAnwsers();
		for (ArrayList<GroupAnswer> aob : aobs) {
			for (GroupAnswer a : aob) {
				for (int i = 0; i < a.getSimilarLabels().size(); i++) {
					String temp = a.getSimilarLabels().get(i);
					int i1 = getActivityIndex(temp);
					if (i1 != -1) {
						if (a.getRepair() != null) {
							boolean found = false;
							for (Pair<String, Integer> p : repairs.get(i1)) {
								int index = repairs.get(i1).indexOf(p);
								if (p.getKey().equals(a.getRepair())) {
									p.setValue(p.getValue() + 1);
									Pair<String, Double> wp = repairsWeighted.get(i1).get(index);
									double w = computeWeightedContribution(a.getUsername());
									wp.setValue(wp.getValue() + w);
									found = true;
									break;
								}
							}
							if (!found) {
								repairs.get(i1).add(new Pair<String, Integer>(a.getRepair(), 1));
								double w = computeWeightedContribution(a.getUsername());
								repairsWeighted.get(i1).add(new Pair<String, Double>(a.getRepair(), w));

							}
						}
						for (int j = i + 1; j < a.getSimilarLabels().size(); j++) {
							String temp1 = a.getSimilarLabels().get(j);
							int j1 = getActivityIndex(temp1);
							if (j1 != -1) {
								double w = computeWeightedContribution(a.getUsername());
								if (i1 < j1) {
									crowdViews[i1][j1]++;
									crowdWeightedViews[i1][j1] += w;
								} else if (j1 < i1) {
									crowdViews[j1][i1]++;
									crowdWeightedViews[j1][i1] += w;
								}
							}
						}
					}
				}

				ActivityBoard board = getRelatedBoard(aob);
				ArrayList<Pair<Activity, Activity>> differentPairs = getDifferentPairs(board, aob);
				if (differentPairs != null) {
					for (Pair<Activity, Activity> p : differentPairs) {
						int i = activities.indexOf(p.getKey());
						int j = activities.indexOf(p.getValue());
						double w = computeWeightedContribution(a.getUsername());
						if (i < j) {
							crowdViews[i][j]--;
							crowdWeightedViews[i][j] -= w;
						} else if (j < i) {
							crowdViews[j][i]--;
							crowdWeightedViews[j][i] -= w;
						}
					}
				}

			}
		}

	}

	private ActivityBoard getRelatedBoard(ArrayList<GroupAnswer> gas) {
		if(gas.size()>0) {
		for(ActivityBoard ab: boards) {
			if(ab.getLevel() == gas.get(0).getLevel() && ab.getTopicId() == gas.get(0).getTopicId()) {
				return ab;
			}
		}
		}
		return null;
	}

	private ArrayList<Pair<Activity, Activity>> getDifferentPairs(ActivityBoard board, ArrayList<GroupAnswer> aob) {
		ArrayList<Pair<Activity, Activity>> res = new ArrayList<>();
		if (board != null) {
			for (GroupAnswer a : aob) {
				ArrayList<Activity> exc = getExcludedActs(board, a);
				for (String s : a.getSimilarLabels()) {
					int index = getActivityIndex(s);
					if (index != -1) {
						for (Activity a1 : exc) {
							Activity a2 = activities.get(index);
							if (!containsPair(res, a2, a1)) {
								res.add(new Pair<Activity, Activity>(a1, a2));
							}
						}
					}
				}
			}
		}
		return res;
	}

	private boolean containsPair(ArrayList<Pair<Activity, Activity>> res, Activity a2, Activity a1) {
		for (Pair<Activity, Activity> p : res) {
			if ((p.getKey().isEqual(a1) && p.getValue().isEqual(a2))
					|| (p.getKey().isEqual(a2) && p.getValue().isEqual(a1))) {
				return true;
			}
		}
		return false;
	}

	private ArrayList<Activity> getExcludedActs(ActivityBoard board, GroupAnswer a) {
		ArrayList<Activity> res = new ArrayList<>(board.getActs());
		ArrayList<Activity> all = new ArrayList<>(board.getActs());
		for (Activity act : all) {
			for (String s : a.getSimilarLabels()) {
				if (s.equals(act.getName())) {
					res.remove(act);
				}
			}
		}
		return res;
	}

	private ArrayList<ArrayList<GroupAnswer>> groupAnwsers() {
		ArrayList<ArrayList<GroupAnswer>> res = new ArrayList<>();
		for (GroupAnswer a : answerBank) {
			int index = containsAnswerOnBoard(res, a.getUsername(), a.getTopicId(), a.getLevel());
			if (index == -1) {
				ArrayList<GroupAnswer> asob = new ArrayList<>();
				asob.add(a);
				res.add(asob);
			} else {
				res.get(index).add(a);
			}
		}
		return res;
	}

	private int containsAnswerOnBoard(ArrayList<ArrayList<GroupAnswer>> res, String username, int topicId, int level) {
		for (int i = 0; i < res.size(); i++) {
			ArrayList<GroupAnswer> ab = res.get(i);
			if (ab.get(0).getUsername().equals(username) && ab.get(0).getTopicId() == topicId
					&& ab.get(0).getLevel() == level) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * This plugin can repair a log.
	 * 
	 * @return true
	 */

	@Override
	public boolean canRepair() {
		return true;
	}

}
