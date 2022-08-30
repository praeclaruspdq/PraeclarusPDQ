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
import java.util.Random;

import com.processdataquality.praeclarus.support.logelements.Activity;
import com.processdataquality.praeclarus.support.math.EditDistanceRecursive;

/**
 * @author Sareh Sadeghianasl
 * @date 13/5/22
 */

public class MCQuestion {

	private Activity main;
	private ArrayList<Activity> trueOptions;
	private ArrayList<Activity> falseOptions;
	private ArrayList<Activity> allOptions;
	private int optionsNum;
	private int id;
	private String[] DCFS;
	private String[] RS;
	private String[] TS;
	private String[] DS;

	public MCQuestion(int id, Activity main, ArrayList<Activity> trueOptions, ArrayList<Activity> falseOptions,
			String[][] Sims) {
		this.main = main;
		this.trueOptions = trueOptions;
		this.falseOptions = falseOptions;
		this.id = id;
		if (Sims != null) {
			this.DCFS = Sims[0];
			this.RS = Sims[1];
			this.TS = Sims[2];
			this.DS = Sims[3];
		}
		this.allOptions = new ArrayList<>(trueOptions);
		allOptions.addAll(falseOptions);
	}

	public Activity getMain() {
		return main;
	}

	public void setMain(Activity main) {
		this.main = main;
	}

	public ArrayList<Activity> getOptions() {
		ArrayList<Activity> res = new ArrayList<>();
		ArrayList<Activity> all = new ArrayList<>(allOptions);
		Random r = new Random();
		while (!all.isEmpty()) {
			int i = r.nextInt(all.size());
			res.add(all.get(i));
			all.remove(i);
		}
		return res;
	}

	public ArrayList<Activity> getAllOptionsPlusMain() {
		ArrayList<Activity> all = new ArrayList<>(allOptions);
		all.add(main);
		return all;
	}

	public int getOptionsNum() {
		return optionsNum;
	}

	public void setOptionsNum(int optionsNum) {
		this.optionsNum = optionsNum;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Activity getActivityByName(String name) {
		if (main.getName().equals(name))
			return main;
		else {
			for (Activity a : trueOptions) {
				if (a.getName().equals(name))
					return a;
			}
			for (Activity a : falseOptions) {
				if (a.getName().equals(name))
					return a;
			}
			Activity ret = getMostLabelSimilarAct(name);
			System.out.println("Not Found Label in Question: " + name + " replaced with: " + ret.getName());

			return ret;
		}
	}

	private Activity getMostLabelSimilarAct(String name) {
		double maxSim = Double.MIN_VALUE;
		Activity maxAct = null;
		double sim = 1 - EditDistanceRecursive.normalizedDistance(name, main.getName());
		if (sim > maxSim) {
			maxSim = sim;
			maxAct = main;
		}
		for (Activity a : trueOptions) {
			sim = 1 - EditDistanceRecursive.normalizedDistance(name, a.getName());
			if (sim > maxSim) {
				maxSim = sim;
				maxAct = a;
			}
		}
		for (Activity a : falseOptions) {
			sim = 1 - EditDistanceRecursive.normalizedDistance(name, a.getName());
			if (sim > maxSim) {
				maxSim = sim;
				maxAct = a;
			}
		}

		return maxAct;
	}

	public ArrayList<Activity> getTrueOptions() {
		return trueOptions;
	}

	public void setTrueOptions(ArrayList<Activity> trueOptions) {
		this.trueOptions = trueOptions;
	}

	public ArrayList<Activity> getFalseOptions() {
		return falseOptions;
	}

	public void setFalseOptions(ArrayList<Activity> falseOptions) {
		this.falseOptions = falseOptions;
	}

	public String[] getDCFS() {
		return DCFS;
	}

	public void setDCFS(String[] dCFS) {
		DCFS = dCFS;
	}

	public String[] getRS() {
		return RS;
	}

	public void setRS(String[] rS) {
		RS = rS;
	}

	public String[] getTS() {
		return TS;
	}

	public void setTS(String[] tS) {
		TS = tS;
	}

	public String[] getDS() {
		return DS;
	}

	public void setDS(String[] dS) {
		DS = dS;
	}

	public int getRealIndexOf(Activity a) {
		return allOptions.indexOf(a);
	}
}