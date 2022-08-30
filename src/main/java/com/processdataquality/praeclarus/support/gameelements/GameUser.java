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

public class GameUser {

	private String username;
	private String knowledgeLevel;
	private String XPLevel;
	private String role;
	private double KNValue;
	private double roleValue;
	private String[] KNLevels= {"Beginner","Problem Solver", "Expert", "Master", "Visionary"};
	private String[] roles= {"Nothing", "A few", "Some how", "A lot" };
	
	public GameUser(String username, String knowledgeLevel, String xPLevel, String role) {
		this.username = username;
		this.knowledgeLevel = knowledgeLevel;
		XPLevel = xPLevel;
		this.role = role;
		for(int i = 0; i< KNLevels.length ; i++) {
			if(this.knowledgeLevel.equalsIgnoreCase(KNLevels[i])) {
				KNValue = i+1;
			}
		}
		for(int i = 0; i< roles.length ; i++) {
			if(this.role.equalsIgnoreCase(roles[i])) {
				roleValue = i+1;
			}
		}
		
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getKnowledgeLevel() {
		return knowledgeLevel;
	}
	public void setKnowledgeLevel(String knowledgeLevel) {
		this.knowledgeLevel = knowledgeLevel;
	}
	public String getXPLevel() {
		return XPLevel;
	}
	public void setXPLevel(String xPLevel) {
		XPLevel = xPLevel;
	}

	public double getKNValue() {
		return this.KNValue;
	}

	public double getRoleValue() {
		return this.roleValue;
	}
}