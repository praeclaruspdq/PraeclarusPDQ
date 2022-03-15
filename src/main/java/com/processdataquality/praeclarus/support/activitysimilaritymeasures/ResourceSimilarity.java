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

import java.util.ArrayList;

import com.processdataquality.praeclarus.support.logelements.Activity;

/**
 * @author Sareh Sadeghianasl
 * @date 7/1/22
 */

public class ResourceSimilarity {
	private ArrayList<Activity> activities;
	private int nDA;
	private double[][] RS;

	
	public ResourceSimilarity(ArrayList<Activity> activities) {
		this.activities = new ArrayList<Activity>(activities);
		nDA = activities.size();
		RS = new double[nDA][nDA];
		resourceSimilarity();
	}
	
	public void resourceSimilarity() {
		boolean[] hasResource = new boolean[nDA];
		for (int i = 0; i < nDA; i++) {
			hasResource[i] = activities.get(i).hasResource();
		}
		for(int i = 0; i<nDA ; i++) {
			for(int j = 0; j<nDA ; j++) {
				if(j>i) {
					if(hasResource[i] && hasResource[j])
						RS[i][j] = resourceSimilarity(activities.get(i), activities.get(j));
					else if( (hasResource[i] && !hasResource[j]) || (!hasResource[i] && hasResource[j])) {
						RS[i][j] = 0;
					}else {
						RS[i][j] = -1;
					}
				}else if (j == i) {
					RS[i][j] = 1.0;
				}else if(i>j) {
					RS[i][j] = RS[j][i];
				}
			}
		}
	}
	
	public double resourceSimilarity(Activity a1 , Activity a2) {
		double resourceSimilarity = 0;
		double[] pdf1 = a1.getResourcePdf();
		double[] pdf2 = a2.getResourcePdf();
		double dist = computeManhattanDistance(pdf1, pdf2);
		resourceSimilarity = 1-dist;
		return resourceSimilarity;
	}
		
	private double computeManhattanDistance(double[] pdf1, double[] pdf2) {
		int n = pdf1.length;
		double dist = 0;
		for (int i = 0; i < n; i++) {
			double d = Math.abs(pdf1[i] - pdf2[i]);
			//double d = Math.abs(pdf1[i] - pdf2[i])/n;
			dist += d;
		}
		dist = dist/2;
		if(dist>1) {
			dist = 1;
		}
		return dist;
	}
	
	public boolean checkTriangle(double[][] dist) {
		int n = dist.length;
		int m = dist[0].length;
		int notHold = 0;
	
		for(int i = 0; i<n ; i++) {
			for(int j = 0; j<m; j++) {
				if(j>i) {
					double ab = dist[i][j];
					for(int k = 0 ; k<n ; k++) {
						if(k!= i && k!=j) {
							double ac = dist[i][k];
							double cb = dist[k][j];
							double sum = ac+cb;
							double diff = ab -sum;
							if(diff>0.00000001) {
								notHold++;
							}
						}
					}
				}
			}
		}
		if(notHold>0){
			return false;
		}
		return true;
	}

	
	public double[][] getSimilarity(){
		return RS;
	}
	
	public void printRS() {
		System.out.println("Resource Similarity");
		for(int i = 0; i<nDA ; i++) {
			for(int j = 0; j<nDA ; j++) {
				System.out.print(RS[i][j] + "||");
			}
			System.out.println();
		}		
	}
}