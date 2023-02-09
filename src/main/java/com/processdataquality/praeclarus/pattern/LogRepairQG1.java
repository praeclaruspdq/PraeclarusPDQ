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
import com.processdataquality.praeclarus.support.gameelements.MCAnswer;
import com.processdataquality.praeclarus.support.gameelements.MCQuestion;
import com.processdataquality.praeclarus.support.logelements.Activity;
import com.processdataquality.praeclarus.support.math.Pair;

import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

/**
 * @author Sareh Sadeghianasl
 * @date 23/5/22
 */
@Plugin(name = "Log Repair QG1", author = "Sareh Sadeghianasl", version = "1.0", synopsis = "Repairs event log using the anwers of the Quality Gaurdian game")
@Pattern(group = PatternGroup.SYNONYMOUS_LABELS)

public class LogRepairQG1 extends AbstractImperfectLabelGameRepair {

	ArrayList<MCQuestion> questionBank;
	ArrayList<MCAnswer> answerBank;
	Table qTable, ansTable;

	public LogRepairQG1() {
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
		ansTable = getAuxiliaryDatasets().getTable("Answers1");
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
		questionBank = new ArrayList<MCQuestion>();
		int numOptions = table.columnCount() - 6;
		for (Row row : table) {
			int qid = row.getInt("QID");
			String maintemp = row.getString("Main Activity");
			String mainIndexTemp = maintemp.substring(0, maintemp.indexOf(")") + 1);
			String mainIndex = mainIndexTemp.substring(1, mainIndexTemp.length() - 1);
			int index = Integer.valueOf(mainIndex);
			Activity mainAct = null;
			if (index >= 0 && index < activities.size()) {
				mainAct = activities.get(index);
			}

			ArrayList<Activity> trueOptions = new ArrayList<>();
			ArrayList<Activity> falseOptions = new ArrayList<>();

			for (int i = 0; i < numOptions; i++) {
				String optionTemp = row.getString(i + 2);
				String optionIndexTemp = optionTemp.substring(0, optionTemp.indexOf(")") + 1);
				String optionIndex = optionIndexTemp.substring(1, optionIndexTemp.length() - 1);
				int oIndex = Integer.valueOf(optionIndex);
				Activity optionAct = null;
				if (oIndex >= 0 && oIndex < activities.size()) {
					optionAct = activities.get(oIndex);
				}
				char type = optionTemp.charAt(optionTemp.indexOf(")") + 1);
				if (type == 't') {
					trueOptions.add(optionAct);
				} else if (type == 'f') {
					falseOptions.add(optionAct);
				}
			}
			questionBank.add(new MCQuestion(qid, mainAct, trueOptions, falseOptions, null));
		}
	}

	public void readAnswers(Table table) {
		answerBank = new ArrayList<MCAnswer>();
		for (Row row : table) {
			String username = row.getString("Username");
			int qid = row.getInt("QID");
			MCQuestion question = getQuestion(qid);
			String detectionsTemp = row.getString("Detections");
			ArrayList<Activity> detections = createDetections(detectionsTemp);
			String repair = row.getString("Repair");
			if (repair.equals("NULL") || repair.isEmpty()) {
				repair = null;
			}
			answerBank.add(new MCAnswer(question, username, detections, repair));
		}
	}

	private MCQuestion getQuestion(int qId) {
		for (MCQuestion q : questionBank) {
			if (q.getId() == qId) {
				return q;
			}
		}
		return null;
	}

	private ArrayList<Activity> createDetections(String detectionsTemp) {
		ArrayList<Activity> result = new ArrayList<>();
		try {
			ArrayList<String> res = new ArrayList<>();
			StringTokenizer st = new StringTokenizer(detectionsTemp, ";");
			String detection;
			while (st.hasMoreTokens()) {
				String next = st.nextToken();
				detection = next.substring(1, next.length() - 1);
				res.add(detection);
				if (!detection.isEmpty()) {
					int index = Integer.valueOf(detection);
					Activity act = activities.get(index);
					result.add(act);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private void populateMatrices() {
		for (MCAnswer a : answerBank) {
			//a.print();
			for (int i = 0; i < a.getSimilar().size(); i++) {
				int i1 = activities.indexOf(a.getSimilar().get(i));
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
				for (int j = i + 1; j < a.getSimilar().size(); j++) {
					int j1 = activities.indexOf(a.getSimilar().get(j));
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

			if (a.getDifferentPairs() != null) {
				for (Pair<Activity, Activity> p : a.getDifferentPairs()) {
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
