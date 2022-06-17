package com.processdataquality.praeclarus.support.logelements;

import java.util.ArrayList;
import java.util.Random;

import com.processdataquality.praeclarus.support.math.EditDistanceRecursive;



public class ActivityGroup implements Comparable<ActivityGroup>{

	private ArrayList<Activity> acts;
	private int size;
	private int id;
	private double avgSim;

	public ActivityGroup(int id, ArrayList<Activity> acts, double avgSim) {
		this.acts = acts;
		this.size = acts.size();
		this.id = id;
		this.avgSim = avgSim;
	}

	public Activity getActivityByName(String name) {
		for(Activity a: acts) {
			if (a.getName().equals(name))
				return a;
		}
		Activity ret = getMostLabelSimilarAct(name);
		//System.out.println("Not Found Label in Question: " + name + " replaced with: " + ret.getName());
		return ret;
	}

	private Activity getMostLabelSimilarAct(String name) {
		
		Activity maxAct = null;
		double max = Double.MIN_VALUE;
		double[] sims = new double[size];
		for(int i = 0; i< acts.size(); i++) {
			sims[i] = 1 - EditDistanceRecursive.normalizedDistance(name, acts.get(i).getName());
			if(sims[i]> max) {
				max= sims[i];
				maxAct = acts.get(i);
			}
		}
		
		return maxAct;
	}

	public ArrayList<Activity> getActs() {
		return acts;
	}

	public void setActs(ArrayList<Activity> acts) {
		this.acts = acts;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public double getAvgSim() {
		return avgSim;
	}

	public void setAvgSim(double avgSim) {
		this.avgSim = avgSim;
	}
	
	@Override
	public int compareTo(ActivityGroup other) {
		if (this.avgSim > other.getAvgSim())
			return -1;
		else if (this.avgSim < other.getAvgSim())
			return 1;
		return 0;
	}

}
