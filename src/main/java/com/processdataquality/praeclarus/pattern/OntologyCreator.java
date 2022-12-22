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

import com.processdataquality.praeclarus.annotation.Pattern;
import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionException;
import com.processdataquality.praeclarus.option.Options;
import com.processdataquality.praeclarus.support.gameelements.ActivityGroup;
import com.processdataquality.praeclarus.support.gameelements.ActivityOntology;
import com.processdataquality.praeclarus.support.gameelements.RDFAnswer;
import com.processdataquality.praeclarus.support.logelements.Activity;
import com.processdataquality.praeclarus.support.math.Pair;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.ArrayList;

/**
 * @author Sareh Sadeghianasl
 * @date 23/5/22
 */
@Plugin(name = "Ontology Creator", author = "Sareh Sadeghianasl", version = "1.0",
		synopsis = "Creates an activity ontology using the answers of the Quality Guardian Rosebud game")
@Pattern(group = PatternGroup.SYNONYMOUS_LABELS)

public class OntologyCreator extends AbstractImperfectLabelGameRepair {

	ArrayList<ActivityGroup> questionBank;
	ArrayList<RDFAnswer> answerBank;
	Table qTable, ansTable;
	ArrayList<RDFAnswer> approvedPairs;

	public OntologyCreator() {
		super();
	}

	@Override
	protected void detect(StringColumn column, String s1, String s2) {
	}

	@Override
	public Options getOptions() {
		Options options = super.getOptions();
		// TODO define limit for each option
		options.addDefault("Vote Threshold", 2.0);
		options.addDefault("IRI", "http://www.semanticweb.org/<username>/ontologies/2021/3/PAO");
		return options;
	}

	@Override
	public Table repair(Table master) throws InvalidOptionException {
		actTable = getAuxiliaryDatasets().getTable("Activities");
		userTable = getAuxiliaryDatasets().getTable("Users");
		qTable = getAuxiliaryDatasets().getTable("Questions");
		ansTable = getAuxiliaryDatasets().getTable("Answers3");

		readActs(actTable);
		readUsers(userTable);
		readQuestions(qTable);
		readAnswers(ansTable);

		crowdWeightedViews = new double[questionBank.size()][9];
		approvedPairs = new ArrayList<RDFAnswer>();

		populateMatrices();
		populateApprovedRDFs();
		
		ActivityOntology pao = new ActivityOntology(getOptions().get("IRI").asString());
		pao.createSchema();
		pao.addActivities(activities, approvedPairs);
		getAuxiliaryDatasets().put("ontology", pao.getOntology());
		
		return super.repair(master);
	}

	public void readQuestions(Table table) {
		questionBank = new ArrayList<ActivityGroup>();
		for (Row row : table) {
			int gid = row.getInt("GID");

			String acttemp1 = row.getString("Activity 1");
			String actIndexTemp1 = acttemp1.substring(0, acttemp1.indexOf(")") + 1);
			String actIndex1 = actIndexTemp1.substring(1, actIndexTemp1.length() - 1);
			int index1 = Integer.valueOf(actIndex1);
			Activity act1 = null;
			if (index1 >= 0 && index1 < activities.size()) {
				act1 = activities.get(index1);
			}

			String acttemp2 = row.getString("Activity 2");
			String actIndexTemp2 = acttemp2.substring(0, acttemp2.indexOf(")") + 1);
			String actIndex2 = actIndexTemp2.substring(1, actIndexTemp2.length() - 1);
			int index2 = Integer.valueOf(actIndex2);
			Activity act2 = null;
			if (index2 >= 0 && index2 < activities.size()) {
				act2 = activities.get(index2);
			}
			ArrayList<Activity> acts = new ArrayList<Activity>();
			acts.add(act1);
			acts.add(act2);
			questionBank.add(new ActivityGroup(gid, acts, -1));
		}
	}

	public void readAnswers(Table table) {
		answerBank = new ArrayList<RDFAnswer>();
		for (Row row : table) {
			String username = row.getString("Username");
			int labelId1 = row.getInt("Label1");
			int labelId2 = row.getInt("Label2");
			int relation = row.getInt("Relation");
			String extraId = row.getString("Extra ID");
			String extraLabel = row.getString("Extra Label");
			if (extraId.equals("NULL")) {
				extraId = null;
			}
			if (extraLabel.equals("NULL")) {
				extraLabel = null;
			}

			Activity act1 = null;
			if (labelId1 >= 0 && labelId1 < activities.size()) {
				act1 = activities.get(labelId1);
			}
			Activity act2 = null;
			if (labelId2 >= 0 && labelId2 < activities.size()) {
				act2 = activities.get(labelId2);
			}

			int topicId = row.getInt("Topic");
			answerBank.add(new RDFAnswer(username, act1, act2, relation, extraId, extraLabel, topicId));
		}
	}

	private void populateMatrices() {
		for (int i = 0; i < questionBank.size(); i++) {
			ActivityGroup ag = questionBank.get(i);
			for (RDFAnswer ra : answerBank) {
				System.out.println("answer: " + ra.getLabel1().getName() + ", "+ ra.getLabel2().getName()+ ", relation: "+ ra.getRelation());
				if (ag.getActs().get(0).isEqual(ra.getLabel1()) && ag.getActs().get(1).isEqual(ra.getLabel2())) {
					int relIndex = ra.getRelation();
					if (relIndex != -1) {
						double temp = computeWeightedContribution(ra.getUsername());
						crowdWeightedViews[i][relIndex] += temp;
					}
				}
			}
		}
	}

	public void populateApprovedRDFs() {
		for (int i = 0; i < questionBank.size(); i++) {
			ActivityGroup lp = questionBank.get(i);
			Pair<Integer, Double> maxRel = getMaxRelation(crowdWeightedViews[i]);
			System.out.println("MaxRel: " + maxRel.getValue());
			if (maxRel.getValue() >= getOptions().get("Vote Threshold").asDouble()) {
				String extraInfo = findBestExtraInfo(lp, maxRel.getKey());
				RDFAnswer ra = new RDFAnswer(lp.getActs().get(0), lp.getActs().get(1), maxRel.getKey(), extraInfo);
				approvedPairs.add(ra);
			}
		}

	}

	private Pair<Integer, Double> getMaxRelation(double[] relationWeights) {

		double max = Double.MIN_VALUE;
		int maxIndex = -1;
		for (int i = 0; i < relationWeights.length; i++) {
			if (relationWeights[i] > max) {
				max = relationWeights[i];
				maxIndex = i;
			}
		}
		return new Pair<Integer, Double>(maxIndex, max);
	}

	private String findBestExtraInfo(ActivityGroup ag, Integer rel) {
		Integer relation = rel;
		if (relation == 1 || relation == 5 || relation == 8) {
			ArrayList<Pair<String, Double>> allRepairs = new ArrayList<>();
			for (RDFAnswer ra : answerBank) {
				if (ag.getActs().get(1).isEqual(ra.getLabel1()) && ag.getActs().get(2).isEqual(ra.getLabel2())
						&& ra.getRelation() == relation) {
					if (ra.getExtraInfo() != null) {
						int index = getRepairIndex(ra.getExtraInfo(), allRepairs);
						if (index == -1) {
							allRepairs.add(new Pair<String, Double>(ra.getExtraInfo(),
									computeWeightedContribution(ra.getUsername())));
						} else {
							allRepairs.set(index, (new Pair<String, Double>(allRepairs.get(index).getKey(),
									allRepairs.get(index).getValue() + computeWeightedContribution(ra.getUsername()))));
						}
					}

				}
			}
			double max = Double.MIN_VALUE;
			String maxRepair = "NULL";
			for (Pair<String, Double> repair : allRepairs) {
				if (!repair.getKey().equals("NULL") && repair.getValue() > max) {
					max = repair.getValue();
					maxRepair = repair.getKey();
				}
			}
			return maxRepair;
		}
		return "NULL";
	}

	private int getRepairIndex(String extraInfo, ArrayList<Pair<String, Double>> allRepairs) {
		for (Pair<String, Double> repair : allRepairs) {
			if (repair.getKey().equals(extraInfo)) {
				return allRepairs.indexOf(repair);
			}
		}
		return -1;
	}

	/**
	 * This plugin cannot repair the log, it only creates an activity ontology which can be later used for repairing a log.
	 * 
	 * @return true
	 */

	@Override
	public boolean canRepair() {
		return false;
	}

}
