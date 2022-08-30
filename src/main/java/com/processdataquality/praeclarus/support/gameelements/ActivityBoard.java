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



public class ActivityBoard{

	ArrayList<Activity> acts;
	private int topicId;
	private int level;
	
	public ActivityBoard(ArrayList<Activity> acts, int topicId, int level) {
		this.acts = acts;
		this.topicId = topicId;
		this.level = level;
	}
	
	public ActivityBoard( int topicId, int level) {
		this.topicId = topicId;
		this.level = level;
		this.acts = new ArrayList<Activity>();
	}
	
	public ActivityBoard() {
		
	}

	public ArrayList<Activity> getActs() {
		return acts;
	}

	public void setActs(ArrayList<Activity> acts) {
		this.acts = acts;
	}
	
	public int getTopicId() {
		return topicId;
	}

	public void setTopicId(int topicId) {
		this.topicId = topicId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	public void addActs(ArrayList<Activity> acts) {
		this.acts.addAll(acts);
	}
	
	public void addAct(Activity act) {
		this.acts.add(act);
	}
	

}
