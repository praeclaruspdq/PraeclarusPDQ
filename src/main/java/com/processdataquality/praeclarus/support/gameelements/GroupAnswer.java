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

public class GroupAnswer {

	private String username;	
	private ArrayList<String> similarLabels;
	private String repair;
	private int topicId;
	private int level;
	private int relation;
	//0: noRelation 1: synonym, 2: antonym,  3: hypernym, 4: hyponym, 5:cohyponym , 6: holonym,  7: meronym, 8: comeronym
	private String extraId;
	
	public GroupAnswer(String username, ArrayList<String> labels, String repair, int topicId, int level) {
		this.username = username;
		this.similarLabels = labels;
		this.repair = repair;
		this.topicId = topicId;
		this.level = level;
	}

	public GroupAnswer(String username, ArrayList<String> labels, String repair, String tid, int level) {
		this.username = username;
		this.similarLabels = labels;
		this.repair = repair;
		this.level = level;
		if (tid.equalsIgnoreCase("1")) {
			this.topicId = 1;
		} else if (tid.equalsIgnoreCase("2")) {
			this.topicId = 2;
		} else if (tid.equalsIgnoreCase("3")) {
			this.topicId = 3;
		} else {
			this.topicId = -1;
		}
		
	}
	
	public GroupAnswer(String username, ArrayList<String> labels, int relation, String extraId, String repair, int topicId, int level) {
		this.username = username;
		this.similarLabels = labels;
		this.repair = repair;
		this.topicId = topicId;
		this.level = level;
		this.relation = relation;
		this.extraId = extraId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public ArrayList<String> getSimilarLabels() {
		return similarLabels;
	}

	public void setSimilarLabels(ArrayList<String> similarLabels) {
		this.similarLabels = similarLabels;
	}

	public String getRepair() {
		return repair;
	}

	public void setRepair(String repair) {
		this.repair = repair;
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
}