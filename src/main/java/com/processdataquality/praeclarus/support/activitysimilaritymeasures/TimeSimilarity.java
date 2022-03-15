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

package com.processdataquality.praeclarus.support.activitysimilaritymeasures;

import com.processdataquality.praeclarus.support.logelements.Activity;

import java.util.ArrayList;

/**
 * @author Sareh Sadeghianasl
 * @date 7/1/22
 */

public class TimeSimilarity {

	private ArrayList<Activity> activities;
	private int nDA;
	private double[][] TS;

	public TimeSimilarity(ArrayList<Activity> activities) {
		this.activities = new ArrayList<Activity>(activities);
		nDA = activities.size();
		TS = new double[nDA][nDA];
		timeSimilarity();
	}

	public void timeSimilarity() {
		for (int i = 0; i < nDA; i++) {
			for (int j = 0; j < nDA; j++) {
				if (i == j) {
					TS[i][j] = 1;
				} else if (j > i) {
					TS[i][j] = timeSimilarity(activities.get(i), activities.get(j));
				} else if (i > j) {
					TS[i][j] = TS[j][i];
				}
			}
		}
	}

	private double timeSimilarity(Activity ad1, Activity ad2) {
		double sim = 0;
		double hourSim = 0;
		double dayOfWeekSim = 0;
		double monthSim = 0;
	
		if (!ad1.getHoursRandom() && !ad2.getHoursRandom()) {
			hourSim = computeHourSimilarity(ad1, ad2);
		}else if ((!ad1.getHoursRandom() && ad2.getHoursRandom()) || (ad1.getHoursRandom() && !ad2.getHoursRandom())) {
			hourSim = 0;
		}else {
			hourSim = -1;
		}
		if (!ad1.getDWRandom() && !ad2.getDWRandom()) {
			dayOfWeekSim = computeDayOfWeekSimilarity(ad1, ad2);
		}else if ((!ad1.getDWRandom() && ad2.getDWRandom()) || (ad1.getDWRandom() && !ad2.getDWRandom())) {
			dayOfWeekSim = 0;
		}else {
			dayOfWeekSim = -1;
		}
		if (!ad1.getMonthRandom() && !ad2.getMonthRandom()) {
			monthSim = computeMonthSimilarity(ad1, ad2);
		}else if ((!ad1.getMonthRandom() && ad2.getMonthRandom()) || (ad1.getMonthRandom() && !ad2.getMonthRandom())) {
			monthSim = 0;
		}else {
			monthSim = -1;
		}
		sim = Math.max(Math.max(hourSim, dayOfWeekSim), monthSim);
		return sim;

	}

	private double computeMonthSimilarity(Activity ad1, Activity ad2) {
		double[] monthPdf1 = ad1.getMonthPdf();
		double[] monthPdf2 = ad2.getMonthPdf();
		double dist = computeEuclideanDistance(monthPdf1, monthPdf2);
		double sim = 1 - dist;
		return sim;
	}

	private double computeDayOfWeekSimilarity(Activity ad1, Activity ad2) {
		double[] daysPdf1 = ad1.getDayOfWeekPdf();
		double[] daysPdf2 = ad2.getDayOfWeekPdf();
		double dist = computeManhattanDistance(daysPdf1, daysPdf2);
		double sim = 1 - dist;
		return sim;
	}

	private double computeHourSimilarity(Activity ad1, Activity ad2) {
		double[] hoursPdf1 = ad1.getHoursPdf();
		double[] hoursPdf2 = ad2.getHoursPdf();
		double dist = computeManhattanDistance(hoursPdf1, hoursPdf2);
		double sim = 1 - dist;
		return sim;
	}

	private double computeEuclideanDistance(double[] pdf1, double[] pdf2) {
		int n = pdf1.length;
		double dist = 0;
		for (int i = 0; i < n; i++) {
			double d = Math.pow((pdf1[i] - pdf2[i]), 2) / n;
			dist += d;
		}
		dist = Math.sqrt(dist);
		return dist;
	}
	
	private double computeManhattanDistance(double[] pdf1, double[] pdf2) {
		int n = pdf1.length;
		double dist = 0;
		for (int i = 0; i < n; i++) {
			double d = Math.abs(pdf1[i] - pdf2[i]);
			dist += d;
		}
		dist = dist/2;
		return dist;
	}

	public double[][] getSimilarity() {
		return TS;
	}

	public void printTS() {
		System.out.println("Time Similarity");
		for (int i = 0; i < nDA; i++) {
			for (int j = 0; j < nDA; j++) {
				System.out.print(TS[i][j] + "||");
			}
			System.out.println();
		}
	}

}
