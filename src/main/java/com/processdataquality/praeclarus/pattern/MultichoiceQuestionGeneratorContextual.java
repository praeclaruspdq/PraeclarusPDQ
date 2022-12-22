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
import com.processdataquality.praeclarus.support.gameelements.MCQuestion;
import com.processdataquality.praeclarus.support.logelements.Activity;
import com.processdataquality.praeclarus.support.math.Pair;

import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

/**
 * @author Sareh Sadeghianasl
 * @date 23/5/22
 */
@Plugin(name = "Multi-choice Question Generator", author = "Sareh Sadeghianasl", version = "1.0", synopsis = "Generates multi-choice questions for the Quality Guardian game . "
		+ "The questions and their choices consist of activity labels and are calculated based on "
		+ "thier contextual similarity in the log")
@Pattern(group = PatternGroup.SYNONYMOUS_LABELS)

public class MultichoiceQuestionGeneratorContextual extends AbstractImperfectLabelContextual {

	ArrayList<MCQuestion> questionBank;

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
		options.addDefault("Number of Questions", 8);
		options.addDefault("Number of Options", 9);
		return options;
	}
	@Override 
	protected void detect(Table table, StringColumn selectedColumn, String sortColName) throws InvalidOptionException {
		_detected = createResultTable();
		super.detect(table, selectedColumn, sortColName);
		double[] certainty = computeCertainties();
		this.questionBank = createQuestions(selectedColumn, certainty);
		getAuxiliaryDatasets().put("Questions", _detected);
		getAuxiliaryDatasets().put("Activities", createActivitiesTable());
		
	}

	private double[] computeCertainties() {
		double[] certainty = new double[parser.getActivities().size()];
		for (int i = 0; i < parser.getActivities().size(); i++) {
			double maxSim = getMaxSimTo(i);
			double avgSimSD = getAvgSimSD(i, maxSim);
			certainty[i] = ((2 * (1 - maxSim)) + avgSimSD) / 2;
		}
		return certainty;
	}

	private double getMaxSimTo(int i) {
		double res = Double.MIN_VALUE;
		for (int j = 0; j < parser.getActivities().size(); j++) {
			double sim = activityContextSimilariy[j][i];
			if (sim > res)
				res = sim;
		}
		return res;
	}

	private double getAvgSimSD(int i, double maxsim) {
		double res = 0;
		int count = 0;
		double avg = 0;
		for (int j = 0; j < parser.getActivities().size(); j++) {
			if (i != j) {
				double sim = activityContextSimilariy[j][i];
				if (sim != -1) {
					count++;
					res += Math.abs(sim - maxsim);
				}
			}
		}
		if (count != 0) {
			avg = res / count;
		}
		return avg;
	}

	private ArrayList<MCQuestion> createQuestions(StringColumn selectedColumn, double[] certainty) {
		ArrayList<MCQuestion> res = new ArrayList<>();
		boolean noMoreQuestionsCanBeMade = false;
		try {
			boolean[] visited = new boolean[certainty.length];
			int n = 0;
			while (n < getOptions().get("Number of Questions").asInt() && !allActsVisited(visited)) {
				double[] uncertainty = new double[certainty.length];
				double sum = 0;
				for (int i = 0; i < certainty.length; i++) {
					if (!visited[i])
						uncertainty[i] = 1 - certainty[i];
					else
						uncertainty[i] = 0;
					sum += uncertainty[i];
				}
				ArrayList<Pair<Double, Integer>> sims1 = new ArrayList<>();
				int selectedIndex = -1;
				for (int j = 0; j < parser.getActivities().size(); j++) {
					if (uncertainty[j] != 0) {
						Pair<Double, Integer> p1 = new Pair<Double, Integer>(uncertainty[j], j);
						sims1.add(p1);
					}
				}
				Collections.sort(sims1);
				selectedIndex = sims1.get(0).getValue();
				visited[selectedIndex] = true;

				ArrayList<Activity> trueOptions = new ArrayList<>();
				ArrayList<Activity> falseOptions = new ArrayList<>();
				ArrayList<Pair<Double, Integer>> sims = new ArrayList<>();
				for (int j = 0; j < parser.getActivities().size(); j++) {
					if (j != selectedIndex) {
						Pair<Double, Integer> p1 = new Pair<Double, Integer>(activityContextSimilariy[selectedIndex][j], j);
						sims.add(p1);
					}
				}

				Collections.sort(sims);

				int falseNum, trueNum;
				trueNum = findSuitableTrueNum(sims, visited);
				falseNum = (int) getOptions().get("Number of Options").asInt() - trueNum;
				if (trueNum > 0) {

					for (int i = 0; i < falseNum; i++) {

						int indexFromEnd = sims.size() - 1 - i;
						int actIndex = sims.get(indexFromEnd).getValue();
						falseOptions.add(parser.getActivities().get(actIndex));
					}
					int i = 0;
					int j = 0;
					while (i < trueNum) {
						if (j < sims.size()) {
							int actIndex = sims.get(j).getValue();
							if (!visited[actIndex]) {
								Activity a = parser.getActivities().get(actIndex);
								a.setIndex(j);
								trueOptions.add(a);
								visited[actIndex] = true;
								i++;
							} else if (allActsVisited(visited)) {
								noMoreQuestionsCanBeMade = true;
								break;
							}
							j++;
						}
					}

					if (!noMoreQuestionsCanBeMade) {
						Activity main = parser.getActivities().get(selectedIndex);
						String[][] similarities = getSimilarities( main, trueOptions, falseOptions);

						MCQuestion q = new MCQuestion(n, main, trueOptions, falseOptions, similarities);
						res.add(q);
						addQuestionToResult(selectedColumn, q, similarities);
						n++;
					} else {
						break;
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

	private boolean allActsVisited(boolean[] visited) {
		for (int i = 0; i < visited.length; i++) {
			if (visited[i] == false)
				return false;
		}
		return true;
	}

	private int findSuitableTrueNum(ArrayList<Pair<Double, Integer>> sims, boolean[] visited) {
		int res = 0;
		for (Pair<Double, Integer> p : sims) {
			if (p.getKey() > getOptions().get("Overall Context Similarity Threshold").asDouble()
					&& !visited[p.getValue()])
				res++;
		}
		if (res > (int) getOptions().get("Number of Options").asInt() / 2 + 1) {
			res = (int) getOptions().get("Number of Options").asInt() / 2 + 1;
		}
		return res;
	}

	private String[][] getSimilarities(Activity main, ArrayList<Activity> trueOptions,
			ArrayList<Activity> falseOptions) throws Exception {
		ArrayList<Activity> all = new ArrayList<>(trueOptions);
		all.addAll(falseOptions);
		String[][] res = new String[4][all.size()];
		int mainIndex = parser.getActivities().indexOf(main);
		int iCounter = 0;
		if (mainIndex != -1) {
			for (Activity t : all) {
				int index = parser.getActivities().indexOf(t);
				if (index != -1) {
					res[0][iCounter] = getSimPercent(dcfs[mainIndex][index]);
					res[1][iCounter] = getSimPercent(rs[mainIndex][index]);
					res[2][iCounter] = getTimeAndDurationSimPrecent(ts[mainIndex][index], ds[mainIndex][index]);			
					res[3][iCounter] = getSimPercent(eds[mainIndex][index]);
					iCounter++;
				}
			}
		}
		return res;
	}

	private void addQuestionToResult(StringColumn selectedColumn, MCQuestion q, String[][] sims) {

		String main = "(" + parser.getActivities().indexOf(q.getMain()) + ")" + q.getMain().getName();
		ArrayList<String> options = new ArrayList<>();
		for (int i = 0; i < q.getTrueOptions().size(); i++) {
			String cell = "(" + parser.getActivities().indexOf(q.getTrueOptions().get(i)) + ")t" + q.getTrueOptions().get(i).getName();
			options.add(cell);
		}
		for (int i = 0; i < q.getFalseOptions().size(); i++) {
			String cell = "(" + parser.getActivities().indexOf(q.getFalseOptions().get(i)) + ")f" + q.getFalseOptions().get(i).getName();
			options.add(cell);
		}
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
		addResult(selectedColumn, q.getId() , main, options, res[0], res[1], res[2], res[3]);

	}

	/**
	 * Creates the table that will receive the multi-choice questions generated
	 * along with the calculated similarities between the main label and the labels
	 * in the options.
	 *
	 * @return the empty table
	 */
	@Override
	protected Table createResultTable() {
		Table result = Table.create("Result").addColumns(IntColumn.create("QID"),
				StringColumn.create("Main Activity"));
		for (int i = 0; i < getOptions().get("Number of Options").asInt(); i++) {
			String temp = "Option"+String.valueOf(i);
			result.addColumns(StringColumn.create(temp));
		}
		result.addColumns(StringColumn.create("Control Flow Similarities"),
				StringColumn.create("Resource Similarities"), StringColumn.create("Time Similarities"),
				StringColumn.create("Data Similarities"));
		return result;
	}

	/**
	 * Adds multi-choice questions to the results table, as well as their context
	 * similarities
	 *
	 * @param column  the column from the master table to count frequencies
	 * @param qid     the question id
	 * @param main    the main label in the question
	 * @param options the options in the question
	 * @param dcfs    the direct control flow similarity of main and each of the
	 *                options
	 * @param rs      the resource similarity of main and each of the options
	 * @param ts      the time similarity of main and each of the options
	 * @param ds      the data attribute similarity of main and each of the options
	 */
	protected void addResult(StringColumn column, int qid, String main, ArrayList<String> options, String dcfs,
			String rs, String ts, String ds) {	
		_detected.intColumn(0).append(qid);
		_detected.stringColumn(1).append(main);
		for (int i = 0; i < options.size(); i++) {
			_detected.stringColumn(2 + i).append(options.get(i));
		}
		_detected.stringColumn(2 + options.size()).append(dcfs);
		_detected.stringColumn(3 + options.size()).append(rs);
		_detected.stringColumn(4 + options.size()).append(ts);
		_detected.stringColumn(5 + options.size()).append(ds);
	}

	/**
	 * This plugin cannot repair a log, it only detects distorted labels
	 * 
	 * @return false
	 */

	@Override
	public boolean canRepair() {
		return true;
	}

}
