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

package com.processdataquality.praeclarus.support.logelements;

import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;


import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * @author Sareh Sadeghianasl
 * @date 23/5/22
 */

public class Activity {
	private ArrayList<Event> events;
	private String name;
	private double[] resourcePdf;
	private double[] resourceCdf;
	private ArrayList<Double> durationsArray;
	private double[] durations;
	private double[] xdata;
	private double[] ydata;
	private double[] ydata01;
	private double binWidth;
	private double mu;
	private double sigma;
	private double yScale;
	private double resourceMean;
	private double resourceSd;
	private ArrayList<ActivityData> eventLevelData;
	private int[] hours;
	private double[] fourHours;
	private int[] dayOfWeek;
	private int[] dayOfMonth;
	private int[] month;
	private double[] fourHoursPdf;
	private double[] hoursPdf;
	private double[] dayOfWeekPdf;
	private double[] dayOfMonthPdf;
	private double[] monthPdf;
	private ChiSquareTest chis;
	private KolmogorovSmirnovTest KST;
	private boolean isHoursRandom;
	private boolean isDWRandom;
	private boolean isMRandom;
	private int mul = 2;
	private int index;
	
	private double[] availableHours, availableDays, availableMonths, availableFourHours;
	private double[] availableHoursPdf, availableDaysPdf, availableMonthsPdf, availableFourHoursPdf;
	private ArrayList<String> Resources;
	public static double[] criticalValues = { 3.841, 5.991, 7.815, 9.488, 11.070, 12.592, 14.067, 15.507, 16.919,
			18.307, 19.675, 21.026, 22.362, 23.685, 24.996, 26.296, 27.587, 28.869, 30.144, 31.410, 32.7, 33.9, 35.2,
			36.4, 37.7 };
	public static double[] criticalValuesKS = { 0.975, 0.842, 0.707, 0.624, 0.563, 0.520, 0.483, 0.454, 0.430, 0.409,
			0.391, 0.375, 0.361, 0.349, 0.338, 0.327, 0.318, 0.309, 0.301, 0.294 };
	//  critical values for alpha = 0.05
	private Set<String> predNames;
	private ArrayList<Event> predEvents;

	private Set<String> sucNames;
	private ArrayList<Event> sucEvents;

	public Activity() {
		this.events = new ArrayList<>();
		chis = new ChiSquareTest();
		KST = new KolmogorovSmirnovTest();
		eventLevelData = new ArrayList<ActivityData>();
		
	}
	
	public Activity(String name, int index) {
		this.name = name;
		this.index = index;
	}

	public void addAllEvents(ArrayList<Event> pl) {
		this.events.addAll(pl);
		if (name == null) {
			name = new String(pl.get(0).getName());
		}
	}

	public void add(int i, Event e) {
		this.events.add(i, e);
		Map<String, Object> data = e.getCompleleAttributes();
		for (String key : data.keySet()) {
			int index = findInEventLevelData(key);
			if (index == -1) {
				ActivityData newAD = new ActivityData(key, data.get(key));
				eventLevelData.add(newAD);
			} else {
				eventLevelData.get(index).addValue(data.get(key));
			}
		}

		
	}

	public void add(Event e) {
		this.events.add(e);
		if (name == null) {
			name = new String(e.getName());
		}
		Map<String, Object> data = e.getCompleleAttributes();
		for (String key : data.keySet()) {
			int index = findInEventLevelData(key);
			if (index == -1) {
				ActivityData newAD = new ActivityData(key, data.get(key));
				eventLevelData.add(newAD);
			} else {
				eventLevelData.get(index).addValue(data.get(key));
			}
		}

		
	}

	public void compute(ArrayList<String> r, double[] h, double[] d, double[] m) {
		
		this.Resources = new ArrayList<String>(r);
		this.availableHours = h;
		this.availableDays = d;
		this.availableMonths = m;
		this.availableFourHours = new double[6];
		for (int i = 0; i < availableFourHours.length; i++)
			for (int j = i * 4; j < i * 4 + 4; j++)
				availableFourHours[i] += availableHours[j];

		computeResourcePdf();
		computeResourceCdf();
		if (durationDefined()) {
			computeDurationPdf();
		}
		setDataAttributes();
		setTimes();
	}
	
	
	private void setDataAttributes() {	
		for (ActivityData ad : eventLevelData) {
			ad.computePdf();
		}
	}

	public void findPredecessors() {
		predNames = new HashSet<String>();
		predEvents = new ArrayList<Event>();

		for (Event event : events) {
			Event pe = event.getLastEvent();
			if (pe != null) {
				predEvents.add(pe);
				predNames.add(pe.getName());
			}
		}
	}

	public void findSuccessors() {
		sucNames = new HashSet<String>();
		sucEvents = new ArrayList<Event>();

		for (Event event : events) {
			Event pe = event.getNextEvent();
			if (pe != null) {
				sucEvents.add(pe);
				sucNames.add(pe.getName());
			}
		}
	}

	public Set<String> getPredecessorNames() {
		return this.predNames;
	}

	public ArrayList<Event> getPredecessorEvents() {
		return this.predEvents;
	}

	public Set<String> getSuccessorNames() {
		return this.sucNames;
	}

	public ArrayList<Event> getSuccessorEvents() {
		return this.sucEvents;
	}

	public ArrayList<ActivityData> getEventLevelData() {
		return this.eventLevelData;
	}

	public boolean hasEventLevelData() {
		if (this.eventLevelData.size() > 0) {
			return true;
		}
		return false;
	}

	private int findInEventLevelData(String key) {
		for (ActivityData ad : eventLevelData) {
			if (ad.getName().equals(key)) {
				return eventLevelData.indexOf(ad);
			}
		}
		return -1;
	}

	
	private void setTimes() {

		computeAvailableTimesPdf();
		hours = new int[24];
		dayOfWeek = new int[7];
		dayOfMonth = new int[31];
		month = new int[12];

		for (Event e : events) {
			Date date = new Date();
			LocalDateTime dt1 = e.getCompleteEvent().getDateTime("time:timestamp");
			date = Date.from(dt1.atZone(ZoneId.systemDefault()).toInstant());
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			if (hour >= 0 && hour < 24) {
				hours[hour]++;
			}
			int dw = cal.get(Calendar.DAY_OF_WEEK);
			if (dw >= 1 && dw <= 7) {

				dayOfWeek[dw - 1]++;
			}

			int dm = cal.get(Calendar.DAY_OF_MONTH);
			if (dm >= 1 && dm <= 31) {
				dayOfMonth[dm - 1]++;
			}

			int m = cal.get(Calendar.MONTH);
			if (m >= 0 && m < 12) {
				month[m]++;
			}

		}
		//------------------------
		//hours

		//int num = (int) Math.ceil(((double) availableHoursList.size()) / 4);
		fourHoursPdf = new double[6];
		fourHours = new double[6];
		double sum = 0;
		for (int i = 0; i < hours.length; i++) {
			if (i >= 0 && i < 4) {
				fourHours[0] += hours[i];

			} else if (i >= 4 && i < 8) {
				fourHours[1] += hours[i];

			} else if (i >= 8 && i < 12) {
				fourHours[2] += hours[i];

			} else if (i >= 12 && i < 16) {
				fourHours[3] += hours[i];

			} else if (i >= 16 && i < 20) {
				fourHours[4] += hours[i];

			} else if (i >= 20 && i < 24) {
				fourHours[5] += hours[i];

			}
			sum += hours[i];
		}
		for (int i = 0; i < fourHours.length; i++) {
			fourHoursPdf[i] = fourHours[i] / sum;
			//Main.out.println("Hours probability: " + fourHoursPdf[i]);

		}
		isHourRandom();

		//----------------------------------------
		//days of week
		dayOfWeekPdf = new double[dayOfWeek.length];
		double sum1 = 0;
		for (int i = 0; i < dayOfWeek.length; i++) {
			sum1 += dayOfWeek[i];
		}
		for (int i = 0; i < dayOfWeekPdf.length; i++) {
			dayOfWeekPdf[i] = dayOfWeek[i] / sum1;
			//Main.out.println("Day of week probability: " + dayOfWeekPdf[i]);
		}
		isDayOfWeekRandom();
		//----------------------------------------
		//month of year

		monthPdf = new double[12];
		double sum3 = 0;
		for (int i = 0; i < month.length; i++) {
			sum3 += month[i];
		}
		for (int i = 0; i < monthPdf.length; i++) {
			monthPdf[i] = month[i] / sum3;
			//Main.out.println("Month probability: " + monthPdf[i]);
		}
		isMonthRandom();
		//Main.out.println("---------------------------------------------------------------");
	}

	private void computeAvailableTimesPdf() {
		availableHoursPdf = new double[24];
		availableFourHoursPdf = new double[6];
		availableDaysPdf = new double[7];
		availableMonthsPdf = new double[12];

		double sumh = 0;
		for (int i = 0; i < availableHours.length; i++) {
			sumh += availableHours[i];
		}
		double sumfh = 0;
		for (int i = 0; i < availableFourHours.length; i++) {
			sumfh += availableFourHours[i];
		}
		double sumd = 0;
		for (int i = 0; i < availableDays.length; i++) {
			sumd += availableDays[i];
		}
		double summ = 0;
		for (int i = 0; i < availableMonths.length; i++) {
			summ += availableMonths[i];
		}
		if (sumh != 0) {
			for (int i = 0; i < availableHours.length; i++) {
				availableHoursPdf[i] = availableHours[i] / sumh;
			}
		}
		if (sumfh != 0) {
			for (int i = 0; i < availableFourHours.length; i++) {
				availableFourHoursPdf[i] = availableFourHours[i] / sumfh;
			}
		}
		if (sumd != 0) {
			for (int i = 0; i < availableDays.length; i++) {
				availableDaysPdf[i] = availableDays[i] / sumd;
			}
		}
		if (summ != 0) {
			for (int i = 0; i < availableMonths.length; i++) {
				availableMonthsPdf[i] = availableMonths[i] / summ;
			}
		}

	}

	private void isHourRandom() {
		long[] fourHoursObserved = new long[fourHours.length];
		double sum = 0;
		for (int i = 0; i < fourHours.length; i++) {
			fourHoursObserved[i] = (long) fourHours[i];
			sum += fourHours[i];
		}
		double[] expectedFourHoursKuiper = new double[6];
		double sumk = 0;
		for (int i = 0; i < expectedFourHoursKuiper.length; i++) {
			expectedFourHoursKuiper[i] = availableFourHours[i] - fourHours[i];
			sumk += expectedFourHoursKuiper[i];
		}
		double[] expectedFourHoursKuiperPdf = new double[6];
		for (int i = 0; i < expectedFourHoursKuiperPdf.length; i++) {
			expectedFourHoursKuiperPdf[i] = expectedFourHoursKuiper[i] / sumk;
		}
		double[] expectedFourHours = new double[6];
		for (int i = 0; i < expectedFourHours.length; i++) {
			if (expectedFourHoursKuiperPdf[i] != 0) {
				expectedFourHours[i] = expectedFourHoursKuiperPdf[i] * sum;
			} else {
				expectedFourHours[i] = 1;
			}
		}
		double hoursTest = KS(expectedFourHoursKuiperPdf, fourHoursPdf);
		isHoursRandom = false;
		double criticalValue = 1.63 / Math.sqrt(sum);
		double criticalValue1 = 1.63 / Math.sqrt((sum+sumk)/(sum*sumk));
		if (hoursTest <=  mul*criticalValue)
			isHoursRandom = true;
	}

	private void isDayOfWeekRandom() {
		long[] observedDW = new long[dayOfWeek.length];
		double sum = 0;
		for (int i = 0; i < observedDW.length; i++) {
			observedDW[i] = dayOfWeek[i];
			sum += dayOfWeek[i];
		}
		double[] expectedDWKuiper = new double[availableDays.length];
		double sumk = 0;
		for (int i = 0; i < expectedDWKuiper.length; i++) {
			expectedDWKuiper[i] = availableDays[i] - dayOfWeek[i];
			sumk += expectedDWKuiper[i];
		}
		double[] expectedDWKuiperPdf = new double[availableDays.length];
		for (int i = 0; i < expectedDWKuiperPdf.length; i++) {
			expectedDWKuiperPdf[i] = expectedDWKuiper[i] / sumk;
		}
		double[] expectedDW = new double[availableDaysPdf.length];
		for (int i = 0; i < expectedDW.length; i++) {
			if (expectedDWKuiperPdf[i] != 0) {
				expectedDW[i] = expectedDWKuiperPdf[i] * sum;
			} else {
				expectedDW[i] = 1;
			}
		}
		double DWChiSquare = KS(expectedDWKuiperPdf, dayOfWeekPdf);
		isDWRandom = false;
		double criticalValue = 1.63 / Math.sqrt(sum);
		double criticalValue1 = 1.63 / Math.sqrt((sum+sumk)/(sum*sumk));
		if (DWChiSquare <= mul*criticalValue)
			isDWRandom = true;
	}

	private void isMonthRandom() {
		long[] observedM = new long[month.length];
		double sum = 0;
		for (int i = 0; i < observedM.length; i++) {
			observedM[i] = month[i];
			sum += month[i];
		}
		double[] expectedMKuiper = new double[availableMonths.length];
		double sumk = 0;
		for (int i = 0; i < expectedMKuiper.length; i++) {
			expectedMKuiper[i] = availableMonths[i] - month[i];
			sumk += expectedMKuiper[i];
		}
		double[] expectedMKuiperPdf = new double[availableMonths.length];
		for (int i = 0; i < expectedMKuiperPdf.length; i++) {
			expectedMKuiperPdf[i] = expectedMKuiper[i] / sumk;
		}
		double[] expectedM = new double[availableMonthsPdf.length];
		for (int i = 0; i < expectedM.length; i++) {
			if (expectedMKuiperPdf[i] != 0) {
				expectedM[i] = expectedMKuiperPdf[i] * sum;
			} else {
				expectedM[i] = 1;
			}
		}
		double MChiSquare = KS(expectedMKuiperPdf, monthPdf);
		isMRandom = false;
		double criticalValue = 1.63 / Math.sqrt(sum);
		double criticalValue1 = 1.63 / Math.sqrt((sum+sumk)/(sum*sumk));
		if (MChiSquare <=  mul*criticalValue) //alpha 0.05
			isMRandom = true;
	}

	
	private double KS(double[] expectedPdf, double[] observedPdf) {
		double maxP = Double.MIN_VALUE;
		double maxN = Double.MIN_VALUE;
		double[] expectedCdf = new double[expectedPdf.length];
		double[] observedCdf = new double[observedPdf.length];
		for (int i = 0; i < expectedPdf.length; i++) {
			if (i == 0) {
				expectedCdf[i] = expectedPdf[i];
				observedCdf[i] = observedPdf[i];
			} else {
				expectedCdf[i] = expectedCdf[i - 1] + expectedPdf[i];
				observedCdf[i] = observedCdf[i - 1] + observedPdf[i];
			}
		}
		for (int i = 0; i < expectedCdf.length; i++) {
			double diffP = expectedCdf[i] - observedCdf[i];
			double diffN = observedCdf[i] - expectedCdf[i];
			if (diffP > maxP) {
				maxP = diffP;
			}
			if (diffN > maxN) {
				maxN = diffN;
			}
		}
		double res = Math.max(maxP, maxN);
		return res;
	}
	
	public boolean getHoursRandom() {
		return this.isHoursRandom;
	}

	public boolean getDWRandom() {
		return this.isDWRandom;
	}

	public boolean getMonthRandom() {
		return this.isMRandom;
	}

	public double[] getHoursPdf() {
		return this.fourHoursPdf;
	}

	public double[] getDayOfWeekPdf() {
		return this.dayOfWeekPdf;
	}

	public double[] getDayofMonthPdf() {
		return this.dayOfMonthPdf;
	}

	public double[] getMonthPdf() {
		return this.monthPdf;
	}

	public Event getEvent(int i) {
		return events.get(i);
	}

	public String getName() {
		return this.name;
	}

	public int getEventsNum() {
		return this.events.size();
	}

	public ArrayList<Event> getEvents() {
		return this.events;
	}

	public boolean hasResource() {
		int sum = 0;
		for (Event e : events) {
			if (e.hasResource()) {
				sum++;
			}
		}

		if (sum > 0)
			return true;
		return false;
	}

	private void computeResourceCdf() {

		resourceCdf = new double[Resources.size()];
		for (int i = 0; i < resourceCdf.length; i++) {
			if (i == 0) {
				resourceCdf[i] = resourcePdf[i];
			} else {
				resourceCdf[i] = resourceCdf[i - 1] + resourcePdf[i];
			}

		}
	}

	private void computeResourcePdf() {
		resourceMean = 1.0 / Resources.size();
		resourcePdf = new double[Resources.size()];
		int sum = 0;
		double sumt = 0;
		for (Event e : events) {
			if (e.getRIndex() < Resources.size()) {
				resourcePdf[e.getRIndex()]++;
				sum++;
			}
		}
		for (int i = 0; i < resourcePdf.length; i++) {
			resourcePdf[i] = resourcePdf[i] / sum;
			double t = Math.pow((resourcePdf[i] - resourceMean), 2);
			sumt += t;
		}
		resourceSd = Math.sqrt(sumt / Resources.size());

	}

	public double getResourceMean() {
		return this.resourceMean;
	}

	public double getResourceSd() {
		return this.resourceSd;
	}

	public double[] getResourceCdf() {
		return this.resourceCdf;
	}

	public double[] getResourcePdf() {
		return this.resourcePdf;
	}

	public boolean durationDefined() {
		float numDu = 0;
		for (Event pe : events) {
			if (pe.hasDuration()) {
				numDu++;
			} else {
			}
		}
		if (numDu / events.size() >= 0.2) {
			return true;
		}
		return false;
	}

	private void computeDurationPdf() {
		durationsArray = new ArrayList<Double>();
		for (Event event : events) {
			durationsArray.add(event.getDuration());
		}
		durations = new double[durationsArray.size()];
		for (int i = 0; i < durationsArray.size(); i++) {
			durations[i] = durationsArray.get(i);
		}
		bining(durations, 60);
		normalizeYData();
	}

	private void normalizeYData() {
		ydata01 = new double[ydata.length];
		double sumY = 0;
		for (int i = 0; i < ydata.length; i++) {
			sumY += ydata[i];
		}
		for (int i = 0; i < ydata.length; i++) {
			ydata01[i] = ydata[i] / sumY;
		}
	}

	private void bining(double[] data, double binWidth) {
		Arrays.sort(data);
		int n = data.length;
		double minDiff = minDifference(data);
		if (binWidth < minDiff) {
			binWidth = minDiff;
		}
		this.binWidth = binWidth;
		ArrayList<Double> xArray = new ArrayList<Double>();
		ArrayList<Double> yArray = new ArrayList<Double>();
		int currentIndex = 0;
		double currentBinStart = 0;
		double currentBinEnd = binWidth;
		double currentBinR = (currentBinStart + currentBinEnd) / 2;
		xArray.add(currentBinR);
		yArray.add(0.0);
		boolean dataBinned;
		for (int i = 0; i < n; i++) {
			dataBinned = false;
			do {
				if (currentBinStart <= data[i] && data[i] < currentBinEnd) {
					yArray.set(currentIndex, yArray.get(currentIndex) + 1);
					dataBinned = true;
				} else if (data[i] == currentBinEnd) {
					currentBinStart = currentBinStart + binWidth;
					currentBinEnd = currentBinEnd + binWidth;
					currentBinR = (currentBinStart + currentBinEnd) / 2;
					currentIndex++;
					xArray.add(currentBinR);
					yArray.add(0.0);
				} else if (data[i] > currentBinEnd) {
					currentBinStart = currentBinStart + binWidth;
					currentBinEnd = currentBinEnd + binWidth;
					currentBinR = (currentBinStart + currentBinEnd) / 2;
					currentIndex++;
					xArray.add(currentBinR);
					yArray.add(0.0);
				}
			} while (!dataBinned);

		}
		//delete Infrequent Data
		for (double y : yArray) {
			if (y / events.size() <= 0.01)
				yArray.set(yArray.indexOf(y), 0.0);
		}
		double lastY = yArray.get(yArray.size() - 1);
		while (lastY == 0) {
			yArray.remove(yArray.size() - 1);
			xArray.remove(xArray.size() - 1);
			lastY = yArray.get(yArray.size() - 1);
		}

		//--------------------
		xdata = new double[xArray.size()];
		for (int i = 0; i < xdata.length; i++) {
			xdata[i] = xArray.get(i);
		}
		ydata = new double[yArray.size()];
		for (int i = 0; i < ydata.length; i++) {
			ydata[i] = yArray.get(i);
		}
	}

	private double minDifference(double[] data) {

		int n = data.length;
		boolean allsame = true;
		for (int i = 0; i < n - 1; i++) {
			if (data[0] != data[i]) {
				allsame = false;
			}
		}
		if (allsame) {
			return 0;
		}
		double diff = Double.MAX_VALUE;
		for (int i = 0; i < n - 1; i++) {
			if (data[i + 1] != data[i])
				if (data[i + 1] - data[i] < diff)
					diff = data[i + 1] - data[i];
		}
		return diff;
	}

	public double[] getXData() {
		return this.xdata;
	}

	public double[] getYData() {
		return this.ydata;
	}

	public double[] getYDataPdf() {
		return this.ydata01;
	}

	public double getbinWidth() {
		return this.binWidth;
	}

	public double getMu() {
		return this.mu;
	}

	public double getSigma() {
		return this.sigma;
	}

	public double getYScale() {
		return this.yScale;
	}

	public boolean isEqual(Activity other) {
		String on = new String(other.getName());
		return (this.name.equals(on));
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getAbsoluteFrequency() {
		return this.events.size();
	}

	
}
