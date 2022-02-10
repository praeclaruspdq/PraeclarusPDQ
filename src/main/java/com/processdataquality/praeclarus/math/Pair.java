package com.processdataquality.praeclarus.math;


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
