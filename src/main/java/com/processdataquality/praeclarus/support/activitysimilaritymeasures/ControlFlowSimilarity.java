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
import com.processdataquality.praeclarus.support.logelements.Event;
import com.processdataquality.praeclarus.support.logelements.Trace;

import java.util.ArrayList;

/**
 * @author Sareh Sadeghianasl
 * @date 7/1/22
 */


public class ControlFlowSimilarity {

	private ArrayList<Activity> activities;
	private int nDA;
	private int[][] Relations;
	private int[][] footprint;
	private double[][] DCFS;
	private double[][] DD;
	private double[][] ID;
	private double[][] FSupport;
	private double[][] DConfidence, IConfidence;
	private boolean[][] directlyFollows, indirectlyFollows;
	private ArrayList<Trace> traces;
	

	public ControlFlowSimilarity(ArrayList<Activity> activities, ArrayList<Trace> traces, double noiseThreshold) {
		this.activities = new ArrayList<Activity>(activities);
		this.traces = new ArrayList<Trace>(traces);
		nDA = activities.size();
		Relations = new int[nDA][nDA];
		DCFS = new double[nDA][nDA];
		footprint = new int[nDA][nDA];
		DD = new double[nDA][nDA];
		ID = new double[nDA][nDA];
		FSupport = new double[nDA][nDA];
		DConfidence = new double[nDA][nDA];
		IConfidence = new double[nDA][nDA];
		directlyFollows = new boolean[nDA][nDA];
		indirectlyFollows = new boolean[nDA][nDA];
		setAllDependencies();
		setAllRelations();
		controlFlowSimilarity();
	}

	private void setAllDependencies() {

		for (Trace pt : traces) {
			int i = 0, j = 0;
			for (Event curr : pt.getEvents()) {

				int currIndex = pt.getEvents().indexOf(curr);
				if (currIndex != pt.getEvents().size() - 1) {
					if (currIndex == 0) {
						i = findActivityIndex(curr.getName());
					} else {
						i = j;
					}
					
					Event after = pt.getEvents().get(currIndex + 1);
					j = findActivityIndex(after.getName());
					
					if (i != -1 && j != -1) {
						DD[i][j]++;
					}
				}
				//-------------------------------------------------------------------------------
								for (int k = currIndex + 2; k < pt.getEvents().size(); k++) {
									Event indirectAfter = pt.getEvents().get(k);
									int m = findActivityIndex(indirectAfter.getName());
									if (i != -1 && m != -1) {
										ID[i][m]++;
									}
								}
			}

		}

		for (int i = 0; i < nDA; i++) {
			for (int j = 0; j < nDA; j++) {
				DConfidence[i][j] = DD[i][j] / (activities.get(i).getEventsNum());//+ activities.get(j).getEventsNum());
				IConfidence[i][j] = ID[i][j] / (activities.get(i).getEventsNum());// + activities.get(j).getEventsNum());
			}
		}

		for (int i = 0; i < nDA; i++) {
			for (int j = 0; j < nDA; j++) {
			
				if (DD[i][j] > 0) {
					directlyFollows[i][j] = true;
				}
				if (ID[i][j] > 0) {
					indirectlyFollows[i][j] = true;
				}
			}
		}
	}

	private int findActivityIndex(String name) {
		for (Activity a : activities) {
			if (a.getName().equals(name))
				return activities.indexOf(a);
		}
		return -1;
	}

	public int[][] getRelations() {
		return this.Relations;
	}

	private void setAllRelations() {

		for (int i = 0; i < nDA; i++) {
			for (int j = 0; j < nDA; j++) {
				//setting all relations
				// 1:Causes  , 2: Caused by , 3: Concurrent ,  4: Exclusive 
				FSupport[i][j] = (DConfidence[i][j]+DConfidence[j][i])/2;
				if (directlyFollows[i][j] && directlyFollows[j][i]) {
					footprint[i][j] = 3;
				} else if (directlyFollows[i][j])
					footprint[i][j] = 1;
				else if (directlyFollows[j][i])
					footprint[i][j] = 2;
				else
					footprint[i][j] = 4;
		}
		}
	}


	public void controlFlowSimilarity() {

		for (int i = 0; i < nDA; i++) {
			for (int j = 0; j < nDA; j++) {
				if (i == j) {
					DCFS[i][j] = 1;
				} else {
					DCFS[i][j] = controlFlowSimilarity(i, j);
				}
			}
		}
	}

	public double controlFlowSimilarity(int a1, int a2) {

		double FSim = 0.0;
		double Fsum = 0.0;
		for (int i = 0; i < nDA; i++) {
			if (footprint[a1][i] != 4 || footprint[a2][i] != 4) {
				Fsum += 1;
				if (footprint[a1][i] == footprint[a2][i]) {
					FSim += 1;
				}
			}
		}
		if (Fsum != 0)
			FSim = FSim / Fsum;
		return FSim;

		
	}

	public double[][] getDirectControlFlowSimilarity() {
		return DCFS;
	}

	public void printDCFS() {
		System.out.println("Directly Follows Confidence");
		for (int i = 0; i < nDA; i++) {
			for (int j = 0; j < nDA; j++) {
				System.out.print(DConfidence[i][j] + "||");
			}
			System.out.println();
		}
		System.out.println();
		System.out.println("Directly Follows Frequencies");
		for (int i = 0; i < nDA; i++) {
			for (int j = 0; j < nDA; j++) {
				System.out.print(DD[i][j] + "||");
			}
			System.out.println();
		}
		System.out.println("Control Flow Footprint");
		for (int i = 0; i < nDA; i++) {
			for (int j = 0; j < nDA; j++) {
				System.out.print(footprint[i][j] + "||");
			}
			System.out.println();
		}
		System.out.println();
		System.out.println("Footprint Support");
		for (int i = 0; i < nDA; i++) {
			for (int j = 0; j < nDA; j++) {
				System.out.print(FSupport[i][j] + "||");
			}
			System.out.println();
		}
		System.out.println();
		System.out.println("Direct Control Flow Similarity");
		for (int i = 0; i < nDA; i++) {
			for (int j = 0; j < nDA; j++) {
				System.out.print(DCFS[i][j] + "||");
			}
			System.out.println();
		}
	}	
}
