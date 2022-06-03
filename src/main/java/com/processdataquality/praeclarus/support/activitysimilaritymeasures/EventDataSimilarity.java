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
import com.processdataquality.praeclarus.support.logelements.ActivityData;
import com.processdataquality.praeclarus.support.logelements.Event;
import com.processdataquality.praeclarus.support.math.EditDistanceRecursive;
import com.processdataquality.praeclarus.support.math.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Sareh Sadeghianasl
 * @date 7/1/22
 */

public class EventDataSimilarity {

	private ArrayList<Activity> activities;
	private int nDA;
	private double[][] DaS;
	private double DANameSimThresh;

	public EventDataSimilarity(ArrayList<Activity> activities, double DANameSimThresh) {
		this.activities = new ArrayList<Activity>(activities);
		nDA = activities.size();
		DaS = new double[nDA][nDA];
		this.DANameSimThresh = DANameSimThresh;
		for (Activity a : activities) {
			a.findPredecessors();
			a.findSuccessors();
		}
		dataSimilarity();
	}

	public void dataSimilarity() {

		for (int i = 0; i < nDA; i++) {
			for (int j = 0; j < nDA; j++) {

				if (j > i) {

					boolean datai = activities.get(i).hasEventLevelData();
					boolean dataj = activities.get(j).hasEventLevelData();
					if (datai && dataj) {
						//System.out.println("i, j: " + i + ", " + j + " have data");
						DaS[i][j] = dataSimilarity(activities.get(i), activities.get(j));

					} else if ((datai && !dataj) || (!datai && dataj)) {
						DaS[i][j] = 0;
					} else {
						DaS[i][j] = -1;
					}
				} else if (j == i) {
					DaS[i][j] = 1;
				} else if (i > j) {
					DaS[i][j] = DaS[j][i];
				}
			}
		}
	}

	public double predSucDataSimilarity(Activity a1, Activity a2) {
		return Math.min(predecessorDataSimilarity(a1, a2), successorDataSimilarity(a1, a2));
	}

	public double predecessorDataSimilarity(Activity a1, Activity a2) {
		double pds = 1;
		ArrayList<String> pna1 = new ArrayList<String>(a1.getPredecessorNames());
		ArrayList<String> pna2 = new ArrayList<String>(a2.getPredecessorNames());

		ArrayList<Event> pea1 = new ArrayList<Event>(a1.getPredecessorEvents());
		ArrayList<Event> pea2 = new ArrayList<Event>(a2.getPredecessorEvents());

		if (pna1.size() == 1 && pna2.size() == 1 && pna1.get(0).equals(pna2.get(0))
				&& !pea1.get(0).getCompleleAttributes().isEmpty()) {
			//			ArrayList<ActivityData> ad1 = integrateEventDataAttributes(pea1);
			//			ArrayList<ActivityData> ad2 = integrateEventDataAttributes(pea2);
			ArrayList<ActivityData> data1 = new ArrayList<>(integrateEventDataAttributes(pea1));
			ArrayList<ActivityData> data2 = new ArrayList<>(integrateEventDataAttributes(pea2));
			pds = dataArraySimilarityPS(data1, data2);
		}
		return pds;
	}

	public double successorDataSimilarity(Activity a1, Activity a2) {
		double sds = 1;
		ArrayList<String> sna1 = new ArrayList<String>(a1.getSuccessorNames());
		ArrayList<String> sna2 = new ArrayList<String>(a2.getSuccessorNames());

		ArrayList<Event> sea1 = new ArrayList<Event>(a1.getSuccessorEvents());
		ArrayList<Event> sea2 = new ArrayList<Event>(a2.getSuccessorEvents());

		if (sna1.size() == 1 && sna2.size() == 1 && sna1.get(0).equals(sna2.get(0))
				&& !sea1.get(0).getCompleleAttributes().isEmpty()) {
			//			ArrayList<ActivityData> ad1 = integrateEventDataAttributes(sea1);
			//			ArrayList<ActivityData> ad2 = integrateEventDataAttributes(sea2);
			ArrayList<ActivityData> data1 = new ArrayList<>(integrateEventDataAttributes(sea1));
			ArrayList<ActivityData> data2 = new ArrayList<>(integrateEventDataAttributes(sea2));
			sds = dataArraySimilarityPS(data1, data2);
		}
		return sds;
	}

	private ArrayList<ActivityData> integrateEventDataAttributes(ArrayList<Event> pea1) {
		ArrayList<ActivityData> res = new ArrayList<ActivityData>();
		for (Event e : pea1) {
			Map<String, Object> data = e.getCompleleAttributes();
			for (String key : data.keySet()) {
				int index = findInEventLevelData(key, res);
				if (index == -1) {
					ActivityData newAD = new ActivityData(key, data.get(key));
					res.add(newAD);
				} else {
					res.get(index).addValue(data.get(key));
				}
			}
		}
		for (ActivityData ad : res) {
			ad.computePdf();
		}
		return res;
	}

	private int findInEventLevelData(String key, ArrayList<ActivityData> list) {
		for (ActivityData ad : list) {
			if (ad.getName().equals(key)) {
				return list.indexOf(ad);
			}
		}
		return -1;
	}

	public double dataSimilarity(Activity a1, Activity a2) {
		ArrayList<ActivityData> data1 = new ArrayList<ActivityData>(a1.getEventLevelData());
		ArrayList<ActivityData> data2 = new ArrayList<ActivityData>(a2.getEventLevelData());
		return dataArraySimilarity(data1, data2);
	}

	public double dataArraySimilarity(ArrayList<ActivityData> data1, ArrayList<ActivityData> data2) {
		double dataSimilarity = 0;
		int[] match = match(data1, data2);
		double count = 0;
		for (int i = 0; i < match.length; i++) {
			//System.out.println("match i " + i+ " is " + match[i] );
			if (match[i] != -1) {
				double temp = valuesManhattanSimilarity(data1.get(i), data2.get(match[i]));
				dataSimilarity += temp;
				count++;
			}
		}
		if (count != 0) {
			dataSimilarity = dataSimilarity / count;
		}

		return dataSimilarity;
	}

	public double dataArraySimilarityPS(ArrayList<ActivityData> data1, ArrayList<ActivityData> data2) {
		double sameSize = 0;
		double sameType = 0;
		boolean[] valuesSimilarity = new boolean[data1.size()];

		if (data1.size() == data2.size()) {
			sameSize = 1;
			ArrayList<Integer> match = matchPS(data1, data2);
			if (match.size() == data1.size()) {
				sameType = 1;
				for (int i = 0; i < valuesSimilarity.length; i++) {
					valuesSimilarity[i] = valuesSimilarityTest(data1.get(i), data2.get(match.get(i)));
					if (valuesSimilarity[i] == false) {
						return 0;
					}
				}
			}
		}
		return 1;
	}

	private boolean valuesSimilarityTest(ActivityData ad1, ActivityData ad2) {

		int size1 = ad1.getValuesSize();
		int size2 = ad2.getValuesSize();
		Object[] v1 = ad1.getUniqueValues();
		double[] p1 = ad1.getvaluesPdf();
		Object[] v2 = ad2.getUniqueValues();
		double[] p2 = ad2.getvaluesPdf();
		ArrayList<Object> unionValues = new ArrayList<>();
		ArrayList<Double> unionPdf1 = new ArrayList<>();
		ArrayList<Double> unionPdf2 = new ArrayList<>();
		int[] freq1, freq2;

		//match------------
		int[] matchValue1 = new int[v1.length];
		Arrays.fill(matchValue1, -1);
		int[] matchValue2 = new int[v2.length];
		Arrays.fill(matchValue2, -1);

		for (int i = 0; i < v1.length; i++) {
			for (int j = 0; j < v2.length; j++) {
				if (v1[i].toString().compareTo(v2[j].toString()) < 0) {
					break;
				}
				if (v1[i].toString().equals(v2[j].toString())) {
					matchValue1[i] = j;
					matchValue2[j] = i;
					break;
				}
			}
		}
		//---------------------------------------------------------
		//make union values and pdfs
		for (int i = 0; i < v1.length; i++) {
			unionValues.add(v1[i]);
			unionPdf1.add(p1[i]);
			if (matchValue1[i] != -1) {
				unionPdf2.add(p2[matchValue1[i]]);

			} else {
				unionPdf2.add(0.0);
			}
		}
		for (int i = 0; i < v2.length; i++) {
			if (matchValue2[i] == -1) {
				unionValues.add(v2[i]);
				unionPdf1.add(0.0);
				unionPdf2.add(p2[i]);
			}
		}
		//-------------------------------------------------------
		//make union frequencies
		//		freq1 = new int[unionPdf1.size()];
		//		freq2 = new int[unionPdf2.size()];
		//		for(int i = 0; i<unionPdf1.size(); i++) {
		//			freq1[i]= (int)(unionPdf1.get(i)*size1);
		//		}
		//		for(int i = 0; i<unionPdf2.size(); i++) {
		//			freq2[i]= (int)(unionPdf2.get(i)*size2);
		//		}
		//------------------------------------------------------------
		//boolean tTest = performTTest(unionValues, freq1, freq2);
		boolean KSTest = performKSTest(unionPdf1, unionPdf2, size1, size2);
		return KSTest;
	}

	private boolean performKSTest(ArrayList<Double> unionPdf1, ArrayList<Double> unionPdf2, int size1, int size2) {
		//		double[] p = new double[unionPdf1.size()];
		//		double[] q = new double[unionPdf2.size()];
		//		for(int i = 0; i<unionPdf1.size();i++) {
		//			p[i]= unionPdf1.get(i);
		//		}
		//		for(int i = 0; i<unionPdf2.size();i++) {
		//			q[i]= unionPdf2.get(i);
		//		}
		double pv = KS(unionPdf1, unionPdf2);
		int k = 0;
		double criticalP = computeCriticalP(size1, size2, 0.05);
		if (pv <= criticalP) {
			return true;
		}
		return false;
	}

	private double computeCriticalP(int n, int m, double alpha) {
		double ca = 0;

		if (alpha == 0.001)
			ca = 1.94947;

		if (alpha == 0.01)
			ca = 1.62762;

		if (alpha == 0.02)
			ca = 1.51743;

		if (alpha == 0.05)
			ca = 1.35810;

		if (alpha == 0.1)
			ca = 1.22385;

		if (alpha == 0.15)
			ca = 1.13795;

		if (alpha == 0.2)
			ca = 1.07275;
		if (ca == 0)
			ca = 1.35810;
		int k = 0;
		int i = 0;
		double md = m;
		double nd = n;
		double t = Math.sqrt((md + nd) / (md * nd));
		double res = ca * t;
		return res;
	}

	

	private Pair<ArrayList<Double>, ArrayList<Double>> makeUnionPdfs(ActivityData ad1, ActivityData ad2) {
		Object[] v1 = ad1.getUniqueValues();
		double[] p1 = ad1.getvaluesPdf();
		Object[] v2 = ad2.getUniqueValues();
		double[] p2 = ad2.getvaluesPdf();
		ArrayList<Object> unionValues = new ArrayList<>();

		//match----------------------------------------------------
		int[] matchValue1 = new int[v1.length];
		Arrays.fill(matchValue1, -1);
		for (int i = 0; i < v1.length; i++) {
			for (int j = 0; j < v2.length; j++) {
				if (v1[i].toString().equals(v2[j].toString())) {
					matchValue1[i] = j;
				}
			}
		}

		int[] matchValue2 = new int[v2.length];
		Arrays.fill(matchValue2, -1);
		for (int i = 0; i < v2.length; i++) {
			for (int j = 0; j < v1.length; j++) {
				if (v2[i].toString().equals(v1[j].toString())) {
					matchValue2[i] = j;
				}
			}
		}
		//---------------------------------------------------------
		ArrayList<Double> l1 = new ArrayList<>();
		ArrayList<Double> l2 = new ArrayList<>();
		for (int i = 0; i < v1.length; i++) {
			unionValues.add(v1[i]);
			l1.add(p1[i]);
			if (matchValue1[i] != -1) {
				l2.add(p2[matchValue1[i]]);

			} else {
				l2.add(0.0);
			}
		}
		for (int i = 0; i < v2.length; i++) {
			if (matchValue2[i] == -1) {
				unionValues.add(v2[i]);
				l1.add(0.0);
				l2.add(p2[i]);
			}
		}
		return new Pair<ArrayList<Double>, ArrayList<Double>>(l1, l2);

	}

	private double valuesEuclideanSimilarity(ActivityData ad1, ActivityData ad2) {
		Object[] v1 = ad1.getUniqueValues();
		double[] p1 = ad1.getvaluesPdf();
		Object[] v2 = ad2.getUniqueValues();
		double[] p2 = ad2.getvaluesPdf();

		//match------------
		int[] matchValue1 = new int[v1.length];
		Arrays.fill(matchValue1, -1);
		int[] matchValue2 = new int[v2.length];
		Arrays.fill(matchValue2, -1);

		for (int i = 0; i < v1.length; i++) {
			for (int j = 0; j < v2.length; j++) {
				if (v1[i].toString().compareTo(v2[j].toString()) < 0) {
					break;
				}
				if (v1[i].toString().equals(v2[j].toString())) {
					matchValue1[i] = j;
					matchValue2[j] = i;
					break;
				}
			}
		}
		//---------------------------------------------

		int count = v1.length;
		for (int i = 0; i < v2.length; i++) {
			if (matchValue2[i] == -1)
				count++;
		}
		//---------------------------------------------

		double dist = 0;
		for (int i = 0; i < v1.length; i++) {
			if (matchValue1[i] != -1) {
				double d = Math.pow((p1[i] - p2[matchValue1[i]]), 2) / count;
				dist += d;
			} else {
				double d = Math.pow((p1[i] - 0), 2) / count;
				dist += d;
			}
		}
		for (int i = 0; i < v2.length; i++) {
			if (matchValue2[i] == -1) {
				double d = Math.pow((p2[i] - 0), 2) / count;
				dist += d;
			}
		}
		dist = Math.sqrt(dist);
		double sim = 1 - dist;
		return sim;
	}

	private double valuesManhattanSimilarity(ActivityData ad1, ActivityData ad2) {
		//		if ((ad1.getType().equalsIgnoreCase("Float") && (ad2.getType().equalsIgnoreCase("Float"))) 
		//				|| (ad1.getType().equalsIgnoreCase("Integer")) && (ad2.getType().equalsIgnoreCase("Integer"))) {
		//			double[] p1 = ad1.getvaluesPdf();
		//			double[] p2 = ad2.getvaluesPdf();
		//			double dist = 0;
		//			for (int i = 0; i < p1.length && i<p2.length; i++) {
		//				double d = Math.abs(p1[i] - p2[i]);
		//				dist += d;
		//			}
		//			dist = dist / 2;
		//			double sim = 1 - dist;
		//			return sim;
		//		} else {
		Object[] v1 = ad1.getUniqueValues();
		double[] p1 = ad1.getvaluesPdf();
		Object[] v2 = ad2.getUniqueValues();
		double[] p2 = ad2.getvaluesPdf();

		//match------------
		int[] matchValue1 = new int[v1.length];
		Arrays.fill(matchValue1, -1);
		int[] matchValue2 = new int[v2.length];
		Arrays.fill(matchValue2, -1);

		for (int i = 0; i < v1.length; i++) {
			for (int j = 0; j < v2.length; j++) {
				if (v1[i].toString().compareTo(v2[j].toString()) < 0) {
					break;
				}
				if (v1[i].toString().equals(v2[j].toString())) {
					matchValue1[i] = j;
					matchValue2[j] = i;
					break;
				}
			}
		}

		//---------------------------------------------

		int count = v1.length;
		for (int i = 0; i < v2.length; i++) {
			if (matchValue2[i] == -1)
				count++;
		}
		//---------------------------------------------

		double dist = 0;
		for (int i = 0; i < v1.length; i++) {
			if (matchValue1[i] != -1) {
				double d = Math.abs(p1[i] - p2[matchValue1[i]]);
				dist += d;
			} else {
				double d = Math.abs(p1[i] - 0);
				dist += d;
			}
		}
		for (int i = 0; i < v2.length; i++) {
			if (matchValue2[i] == -1) {
				double d = Math.abs(p2[i] - 0);
				dist += d;
			}
		}
		dist = dist / 2;
		double sim = 1 - dist;
		return sim;
		//}
	}

	private int[] match(ArrayList<ActivityData> data1, ArrayList<ActivityData> data2) {
		ArrayList<ActivityData> test1 = new ArrayList<ActivityData>(data1);
		ArrayList<ActivityData> test2 = new ArrayList<ActivityData>(data2);
		int[] res = new int[data1.size()];
		Arrays.fill(res, -1);
		for (ActivityData ad1 : test1) {
			int i = test1.indexOf(ad1);
			for (ActivityData ad2 : test2) {
				int j = test2.indexOf(ad2);
				double distance = EditDistanceRecursive.normalizedDistance(ad1.getName(), ad2.getName());
				//System.out.println("data 1: " + ad1.getName() + " and data 2: " + ad2.getName() + " distance: " + distance);
				if (distance < (1 - DANameSimThresh)) {
					res[i] = j;
					break;
				}

			}
		}
		return res;
	}

	private ArrayList<Integer> matchPS(ArrayList<ActivityData> data1, ArrayList<ActivityData> data2) {
		ArrayList<ActivityData> test1 = new ArrayList<ActivityData>(data1);
		ArrayList<ActivityData> test2 = new ArrayList<ActivityData>(data2);
		ArrayList<Integer> res = new ArrayList<Integer>(data1.size());
		for (ActivityData ad1 : test1) {
			for (ActivityData ad2 : test2) {
				int j = test2.indexOf(ad2);
				if (ad1.getName().equals(ad2.getName()) && ad1.getType().equals(ad2.getType())) {
					res.add(j);
					break;
				}
			}
		}
		return res;
	}

	private double KS(ArrayList<Double> expectedPdf, ArrayList<Double> observedPdf) {
		double maxP = Double.MIN_VALUE;
		double maxN = Double.MIN_VALUE;
		double[] expectedCdf = new double[expectedPdf.size()];
		double[] observedCdf = new double[observedPdf.size()];
		for (int i = 0; i < expectedPdf.size(); i++) {
			if (i == 0) {
				expectedCdf[i] = expectedPdf.get(i);
				observedCdf[i] = observedPdf.get(i);
			} else {
				expectedCdf[i] = expectedCdf[i - 1] + expectedPdf.get(i);
				observedCdf[i] = observedCdf[i - 1] + observedPdf.get(i);
			}
		}
		for (int i = 0; i < expectedCdf.length; i++) {
			double diffP = expectedCdf[i] - observedCdf[i];
			double diffN = observedCdf[i] - expectedCdf[i];
			if (diffP > maxP) {
				maxP = diffP;
			}
			if (diffN > maxN) {
				maxN = diffN;
			}
		}
		double res = Math.max(maxP, maxN);
		return res;
	}

	public double[][] getSimilarity() {
		return DaS;
	}

	public void printDaS() {
		System.out.println("Event Data Similarity");
		for (int i = 0; i < nDA; i++) {
			for (int j = 0; j < nDA; j++) {
				System.out.print(DaS[i][j] + "||");
			}
			System.out.println();
		}
	}

}