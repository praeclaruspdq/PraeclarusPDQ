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
import com.processdataquality.praeclarus.support.math.EditDistanceRecursive;

/**
 * @author Sareh Sadeghianasl
 * @date 7/1/22
 */

public class StringSimilarity {

	private double[][] LS;
	ArrayList<Activity> activities;

	public StringSimilarity(ArrayList<Activity> activities) {

		this.activities = activities;
		LS = new double[activities.size()][activities.size()];
		computeLabelSimilarity();
	}

	public void computeLabelSimilarity() {
		for (int i = 0; i < activities.size(); i++) {
			for (int j = 0; j < activities.size(); j++) {
				if (j > i) {
					LS[i][j] = 1 - EditDistanceRecursive.normalizedDistance(activities.get(i).getName(),
							activities.get(j).getName());
//				LS[i][j]= 1-StringDistance.getStringDistance(activities.get(i).getLabel(), activities.get(j).getLabel());
				}else if(j == i) {
					LS[i][j] = 1;
				}
				else {
					LS[i][j] = LS[j][i];
				}
			}
		}
	}

	public double[][] getSimilarity() {
		return this.LS;
	}

}
