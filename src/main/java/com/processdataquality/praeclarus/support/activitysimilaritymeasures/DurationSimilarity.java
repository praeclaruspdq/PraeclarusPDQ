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
import com.processdataquality.praeclarus.support.math.ErrorFunction;

import java.util.ArrayList;

/**
 * @author Sareh Sadeghianasl
 * @date 7/1/22
 */

public class DurationSimilarity {

	private ArrayList<Activity> activities;
	private int nDA;
	private double[][] DS;

	public DurationSimilarity(ArrayList<Activity> activities) {
		this.activities = new ArrayList<Activity>(activities);
		nDA = activities.size();
		DS = new double[nDA][nDA];
		durationSimilarity();
	}

	private void durationSimilarity() {
		boolean[] hasDuration = new boolean[nDA];
		for (int i = 0; i < nDA; i++) {
			hasDuration[i] = activities.get(i).durationDefined();
		}
		for (int i = 0; i < nDA; i++) {
			for (int j = 0; j < nDA; j++) {
				if (i == j) {
					DS[i][j] = 1;
				} else if (hasDuration[i] && hasDuration[j]) {
					if (j > i) {
						DS[i][j] = durationSimilarity(activities.get(i), activities.get(j));
					} else if (i > j) {
						DS[i][j] = DS[j][i];
					}
				} else if ((hasDuration[i] && !hasDuration[j]) || (hasDuration[j] && !hasDuration[i])) {
					DS[i][j] = 0;
				} else if (!hasDuration[i] && !hasDuration[j]) {
					DS[i][j] = -1; // neutral
				}
			}
		}
	}

	private double durationSimilarity(Activity ad1, Activity ad2) {

		double durationLimit = findMaxDuration(ad1, ad2);
		//double diff = integrate(0, durationLimit, 10000, ad1, ad2);
		//double drationSimilarity = 1 - (diff / durationLimit);
		double diff = manhattanDurationDistance(ad1, ad2);
		double drationSimilarity = 1 - diff;
		//System.out.println(durationLimit + " " +diff + "  " + drationSimilarity);
		return drationSimilarity;
	}

	private double cdf(double x, double mu, double sigma) {
		double ans = (0.5) * (1 + ErrorFunction.erf((Math.log(x) - mu) / (sigma * Math.sqrt(2))));
		return ans;
	}

	private double dCdf(double x, Activity ad1, Activity ad2) {
		double ans = 0;
		ans = Math.abs(cdf(x, ad1.getMu(), ad1.getSigma()) - cdf(x, ad2.getMu(), ad2.getSigma()));
		return ans;

	}

	private double integrate(double a, double b, int N, Activity ad1, Activity ad2) {
		double h = (b - a) / N; // step size
		double sum = 0.5 * (dCdf(a, ad1, ad2) + dCdf(b, ad1, ad2)); // area
		for (int i = 1; i < N; i++) {
			double x = a + h * i;
			sum = sum + dCdf(x, ad1, ad2);
		}
		return sum * h;
	}

	private double manhattanDurationDistance(Activity a1, Activity a2) {

		double[] xdata1 = a1.getXData();
		double[] pdf1 = a1.getYDataPdf();
		double bin1 = a1.getbinWidth();
		double[] xdata2 = a2.getXData();
		double[] pdf2 = a2.getYDataPdf();
		double bin2 = a2.getbinWidth();

		double[] unionX;
		double[] unionPdf1;
		double[] unionPdf2;

		double dist = 0;

		if (bin1 == bin2) {
			unionX = new double[Math.max(xdata1.length, xdata2.length)];
			unionPdf1 = new double[Math.max(xdata1.length, xdata2.length)];
			unionPdf2 = new double[Math.max(xdata1.length, xdata2.length)];
			if (xdata1.length == xdata2.length) {
				unionX = xdata1;
				unionPdf1 = pdf1;
				unionPdf2 = pdf2;
			} else if (xdata1.length > xdata2.length) {
				unionX = xdata1;
				unionPdf1 = pdf1;
				for (int i = 0; i < unionPdf2.length; i++) {
					if (i < pdf2.length) {
						unionPdf2[i] = pdf2[i];
					} else {
						unionPdf2[i] = 0;
					}
				}
			} else {
				unionX = xdata2;
				unionPdf2 = pdf2;
				for (int i = 0; i < unionPdf1.length; i++) {
					if (i < pdf1.length) {
						unionPdf1[i] = pdf1[i];
					} else {
						unionPdf1[i] = 0;
					}
				}
			}

			dist = computeManhattanDistance(unionPdf1, unionPdf2);
		} else if (bin1 > bin2) {
			double m = bin1 / bin2;
			int xCount1 = (int) (xdata1.length * m);
			int uCount = (int) (Math.ceil(Math.max(xCount1, xdata2.length) / m));
			unionX = new double[uCount];
			unionPdf1 = new double[uCount];
			unionPdf2 = new double[uCount];

			unionX = xdata1;
			for (int i = 0; i < unionPdf1.length; i++) {
				if (i < pdf1.length)
					unionPdf1[i] = pdf1[i];
				else
					unionPdf1[i] = 0;
			}
			for (int i = 0; i < unionPdf2.length; i++) {
				unionPdf2[i] = 0;
				for (int j = 0; j < m; j++) {
					if (((int) (m * i) + j) < pdf2.length)
						unionPdf2[i] += pdf2[(int) (m * i) + j];
				}
			}
			dist = computeManhattanDistance(unionPdf1, unionPdf2);
		} else {
			double m = bin2 / bin1;
			int xCount2 = (int) (xdata2.length * m);
			int uCount = (int) (Math.ceil(Math.max(xCount2, xdata1.length) / m));
			unionX = new double[uCount];
			unionPdf1 = new double[uCount];
			unionPdf2 = new double[uCount];

			unionX = xdata2;
			for (int i = 0; i < unionPdf2.length; i++) {
				if (i < pdf2.length)
					unionPdf2[i] = pdf2[i];
				else
					unionPdf2[i] = 0;
			}
			for (int i = 0; i < unionPdf1.length; i++) {
				unionPdf1[i] = 0;
				for (int j = 0; j < m; j++) {
					if (((int) (m * i) + j) < pdf1.length)
						unionPdf1[i] += pdf1[(int) (m * i) + j];
				}
			}
			dist = computeManhattanDistance(unionPdf1, unionPdf2);
		}

		return dist;
	}

	private double computeManhattanDistance(double[] pdf1, double[] pdf2) {
		int n = pdf1.length;

		double dist = 0;
		for (int i = 0; i < n; i++) {

			double d = 0;
			d = Math.abs(pdf1[i] - pdf2[i]);
			
			//double d = Math.abs(pdf1[i] - pdf2[i])/n;
			dist += d;
		}
		dist = dist / 2;
		return dist;
	}

	private double findMaxDuration(Activity ad1, Activity ad2) {
		double[] xdata1 = ad1.getXData();
		double[] xdata2 = ad2.getXData();
		return Math.max(xdata1[xdata1.length - 1], xdata2[xdata2.length - 1]);
	}

	public double[][] getSimilarity() {
		return DS;
	}

	public void printDS() {
		System.out.println("Duration Similarity");
		for (int i = 0; i < nDA; i++) {
			for (int j = 0; j < nDA; j++) {
				System.out.print(DS[i][j] + "||");
			}
			System.out.println();
		}
	}

}
