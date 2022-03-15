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

package com.processdataquality.praeclarus.support.math;


public class Pair<T1, T2> implements Comparable<Pair<T1, T2>> {
	
	private T1 first;
	private T2 second;
	
	public Pair(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}
	
	public T1 getKey() {
		return first;
	}
	public void setKey(T1 first) {
		this.first = first;
	}
	public T2 getValue() {
		return second;
	}
	public void setValue(T2 second) {
		this.second = second;
	}
	
	@Override
	public int compareTo(Pair<T1, T2> another) {
		double a = (double)another.first;
		double t = (double)this.first;
		if (a == t)
			return 0;
		else if(a > t)
			return 1;
		else return -1;
	}
}
