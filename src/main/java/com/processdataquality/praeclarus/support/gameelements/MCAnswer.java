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

package com.processdataquality.praeclarus.support.gameelements;

import java.util.ArrayList;

import com.processdataquality.praeclarus.support.logelements.Activity;
import com.processdataquality.praeclarus.support.math.Pair;


/**
 * @author Sareh Sadeghianasl
 * @date 13/5/22
 */

public class MCAnswer {

	private MCQuestion question;
	private String username;
	private ArrayList<Activity> similar;
	private ArrayList<Activity> different;
	private ArrayList<Pair<Activity, Activity>> differentPairs;
	private String repair;

	public MCAnswer(MCQuestion question, String username, ArrayList<Activity> similar, String repair) {
		this.question = question;
		this.username = username;
		this.similar = similar;
		this.repair = repair;
		FindDifferent();
		FindDifferentPairs();
	}

	public MCAnswer(MCQuestion question, String username, ArrayList<Activity> similar, ArrayList<Activity> different,
			ArrayList<Pair<Activity, Activity>> differentPairs, String repair) {
		this.question = question;
		this.username = username;
		this.similar = similar;
		this.repair = repair;
		this.different = different;
		this.differentPairs = differentPairs;
	}

	private void FindDifferent() {
		different = new ArrayList<>();
		for (Activity a : question.getAllOptionsPlusMain()) {
			if (!similar.contains(a)) {
				different.add(a);
			}
		}
	}

	private void FindDifferentPairs() {
		differentPairs = new ArrayList<>();
		for (Activity a1 : similar)
			for (Activity a2 : different)
				differentPairs.add(new Pair<Activity, Activity>(a1, a2));

	}

	public MCQuestion getQuestion() {
		return question;
	}

	public void setQuestion(MCQuestion question) {
		this.question = question;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRepair() {
		return repair;
	}

	public void setRepair(String repair) {
		this.repair = repair;
	}

	public ArrayList<Activity> getSimilar() {
		return similar;
	}

	public void setSimilar(ArrayList<Activity> similar) {
		this.similar = similar;
	}

	public ArrayList<Activity> getDifferent() {
		return different;
	}

	public void setDifferent(ArrayList<Activity> different) {
		this.different = different;
	}

	public ArrayList<Pair<Activity, Activity>> getDifferentPairs() {
		return differentPairs;
	}

	public void setDifferentPairs(ArrayList<Pair<Activity, Activity>> differentPairs) {
		this.differentPairs = differentPairs;
	}

	public void print() {
		System.out.println("Answer for Q: "+ question.getId()+ " similars: ");
		for(Activity a: similar) {
			System.out.print(a.getName() + ", ");
		}
		System.out.println("Differents: ");
		for(Activity a: different) {
			System.out.print(a.getName() + ", ");
		}
		System.out.println("Different Pairs: ");
		for(Pair<Activity, Activity> p: differentPairs) {
			System.out.println(p.getKey().getName() + ", " + p.getValue().getName());
		}
	}
}