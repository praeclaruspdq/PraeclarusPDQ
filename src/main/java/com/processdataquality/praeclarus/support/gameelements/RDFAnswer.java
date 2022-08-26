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

import com.processdataquality.praeclarus.support.logelements.Activity;

public class RDFAnswer {
	private String username;
	private Activity label1;
	private Activity label2;
	private int relation;
	//0: noRelation 1: synonym, 2: antonym,  3: hypernym, 4: hyponym, 5:cohyponym , 6: holonym,  7: meronym, 8: comeronym
	private String extraInfo;
	private String extraID;
	private int topicId;

	public RDFAnswer( String username, Activity label1,  Activity label2, int relation, String extraID, String extraInfo, int topicId) {	
		this.username = username;
		this.label1 = label1;
		this.label2 = label2;
		this.relation = relation;
		this.extraInfo = extraInfo;
		this.extraID = extraID;
		this.topicId = topicId;
			
	}
	
	public RDFAnswer(Activity label1,  Activity label2, int relation, String extraInfo) {	
		this.label1 = label1;
		this.label2 = label2;
		this.relation = relation;
		this.extraInfo = extraInfo;
	}
	
	
	public String getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}


	public Activity getLabel1() {
		return label1;
	}


	public void setLabel1(Activity label1) {
		this.label1 = label1;
	}


	public Activity getLabel2() {
		return label2;
	}


	public void setLabel2(Activity label2) {
		this.label2 = label2;
	}

	public int getRelation() {
		return relation;
	}

	public void setRelation(int relation) {
		this.relation = relation;
	}

	public int getTopicId() {
		return topicId;
	}

	public void setTopicId(int topicId) {
		this.topicId = topicId;
	}

	public String getRelationString() {
		switch(this.relation) {
		case 0:
			return "Synonym";
		case 1:
			return "Antonym";
		case 2:
			return "Hypernym";
		case 3:
			return "Hyponym";
		case 4:
			return "Cohyponym";
		case 5:
			return "Holonym";
		case 6:
			return "Meronym";
		case 7:
			return "Comeronym";
		}
		return null;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
