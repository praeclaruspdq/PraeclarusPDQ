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

import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * @author Sareh Sadeghianasl
 * @date 7/1/22
 */

public class ParseTable {

	private ArrayList<Activity> activities = new ArrayList<Activity>();
	public ArrayList<Trace> traces = new ArrayList<Trace>();
	private double[] availableHours;
	private double[] availableDays;
	private double[] availableMonths;
	public ArrayList<Integer> availableHoursList;
	public ArrayList<Integer> availableDaysList;
	public ArrayList<Integer> availableMonthsList;

	private Table table;
	public ArrayList<String> resources;
	int uniqueID;
	private ArrayList<String> ignoreAttrs;
	private String selectedColumnName;
	private String caseIdColumnName;

	public ParseTable(Table table, String selectedColumnName, String caseIdColumnName) {
		this.table = table.sortOn(caseIdColumnName);
		this.selectedColumnName = selectedColumnName;
		this.caseIdColumnName = caseIdColumnName;
		resources = new ArrayList<String>();
		availableHours = new double[24];
		availableDays = new double[7];
		availableMonths = new double[12];
		uniqueID = 0;
		ignoreAttrs = new ArrayList<String>();
	}

	public void parse() {
		List<String> columnNames = table.columnNames();
		String currentCaseID = "", previousCaseID = "";
		ArrayList<Event> eventsPerTrace = new ArrayList<Event>();
		ArrayList<Row> rowList = new ArrayList<Row>();
		int rIndex = -1;
		String lifecycle = null;
		String resource = null;
		for (Row row : table) {
			currentCaseID = row.getString(caseIdColumnName);
			if (!currentCaseID.equalsIgnoreCase(previousCaseID)) {
				// new trace
				if (!eventsPerTrace.isEmpty()) {
					Collections.sort(eventsPerTrace);
					Trace trace = new Trace(new ArrayList<Event>(eventsPerTrace));
					addToActivities(eventsPerTrace);
					eventsPerTrace.clear();
					traces.add(trace);
				}
			}
			if (columnNames.contains("lifecycle:transition")) {
				lifecycle = row.getString("lifecycle:transition");
			}
			if (columnNames.contains("org:resource") && row.getString("org:resource") != null) {
				resource = row.getString("org:resource");
				rIndex = addToResources(resource);
			} else {
				rIndex = addToResources("");
			}
			Event e = null;
			if (lifecycle != null && lifecycle.equalsIgnoreCase("start")) {
				rowList.add(row);
			}else if (lifecycle != null && lifecycle.equalsIgnoreCase("complete")) {
				int startIndex = findFirstStartEventInTrace(rowList, row, selectedColumnName);
				if (startIndex != -2) {
					if (startIndex != -1) { // has start event
						if(resource!=null) {
							e = new Event(row.getString(selectedColumnName), rowList.get(startIndex), row, currentCaseID, resource, rIndex, uniqueID);
						}else if(rowList.get(startIndex).getString("org:resource") != null) {
							String startResource = rowList.get(startIndex).getString("org:resource");
							e = new Event(row.getString(selectedColumnName), rowList.get(startIndex), row, currentCaseID, startResource, getResourceIndex(startResource), uniqueID);

						}else {
							e = new Event(row.getString(selectedColumnName), rowList.get(startIndex), row, currentCaseID, "", rIndex, uniqueID);
						}
						setAvailableTimes(e);
						rowList.remove(startIndex);
					}
					else { // no start event
						if(resource!=null) {
							e = new Event(row.getString(selectedColumnName), null, row, currentCaseID, resource, rIndex, uniqueID);
						}else {
							e = new Event(row.getString(selectedColumnName), null, row, currentCaseID, "", rIndex, uniqueID);
						}
						setAvailableTimes(e);
					}
					for(String attr: columnNames) {
						if (attr.startsWith("data")) { 
							ArrayList<ArrayList<String>> attributes = parseData(row.getString(attr));
							e.addCompleteAttributes(attributes, ignoreAttrs);
						}
					}
					eventsPerTrace.add(e);
					uniqueID++;
				}
			}
			else if (lifecycle == null) { // no lifecycle: we assume it as complete
				if(resource!=null) {
					e = new Event(row.getString(selectedColumnName), null, row, currentCaseID, resource, rIndex, uniqueID);
				}else {
					e = new Event(row.getString(selectedColumnName), null, row, currentCaseID, "", rIndex, uniqueID);
				}
				setAvailableTimes(e);
				for(String attr: columnNames) {
					if (attr.startsWith("data")) { 
						ArrayList<ArrayList<String>> attributes = parseData(row.getString(attr));
						e.addCompleteAttributes(attributes, ignoreAttrs);
					}
				}
				eventsPerTrace.add(e);
				uniqueID++;
			}
			previousCaseID = currentCaseID;
		}
	
		for (Activity a : activities) {	
			a.compute(resources, availableHours, availableDays, availableMonths);
		}
		
	}
	
	private ArrayList<ArrayList<String>> parseData(String data) {
		ArrayList<ArrayList<String>> res = new ArrayList();
		StringTokenizer st = new StringTokenizer(data,";");  
	     while (st.hasMoreTokens()) {  
	         ArrayList<String> attr = new ArrayList();
	         StringTokenizer st1 = new StringTokenizer(st.nextToken(),",");
	         while (st1.hasMoreTokens()) { 
	        	 attr.add(st1.nextToken());
	         }
	         res.add(attr);
	     }  
		return res;
	}

	private void addToActivities(ArrayList<Event> eventsPerTrace) {
		for(Event event: eventsPerTrace) {
			int eventIndex = eventsPerTrace.indexOf(event);
			event.setIndexInCase(eventIndex);
			if (eventIndex != 0) {
				event.setLastEvent(eventsPerTrace.get(eventIndex - 1));
			}
			if (eventIndex != eventsPerTrace.size() - 1) {
				event.setNextEvent(eventsPerTrace.get(eventIndex + 1));
			}
			int index = getAvtivityCategoryIndex(event, activities);
			if (index != -1) {
				activities.get(index).add(activities.get(index).getEventsNum(), event);
			} else {
				Activity newActivity = new Activity();
				newActivity.add(event);
				activities.add(newActivity);
			}
		}
		
	}

	private void setAvailableTimes(Event event) {
		Date date = new Date();
		LocalDateTime dt1 = event.getCompleteEvent().getDateTime("time:timestamp");
		date = Date.from(dt1.atZone(ZoneId.systemDefault()).toInstant());
		Calendar cal = Calendar.getInstance();
		if (date != null) {
			cal.setTime(date);
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int dw = cal.get(Calendar.DAY_OF_WEEK);
			int m = cal.get(Calendar.MONTH);
			if (hour >= 0 && hour < 24) {
				availableHours[hour]++;
			}
			if (dw >= 1 && dw <= 7) {
				availableDays[dw - 1]++;
			}
			if (m >= 0 && m < 12) {
				availableMonths[m]++;
			}
		}
	}

	private int addToResources(String resource) {
		for (String r : resources) {
			if (r.equals(resource)) {
				return resources.indexOf(r);
			}
		}
		resources.add(resource);
		return resources.indexOf(resource);
	}

	private int getResourceIndex(String resource) {
		for (String r : resources) {
			if (r.equals(resource)) {
				return resources.indexOf(r);
			}
		}
		return -1;
	}

	public int getAvtivityCategoryIndex(Event event, ArrayList<Activity> acts) {
		for (Activity ea : acts) {
			if (ea.getName().equals(event.getName())) {
				return acts.indexOf(ea);
			}
		}
		return -1;
	}

	public ArrayList<Activity> getActivities() {
		return activities;
	}
	
	public ArrayList<Trace> getTraces() {
		return traces;
	}

	public int getNumberOfDistinctActivities() {
		return activities.size();
	}

	public int findFirstStartEventInTrace(ArrayList<Row> events, Row row, String activityColumnName) {
		String name = "";
		name = row.getString(activityColumnName);
		if (name == null) {
			return -2;
		}
		int minTimeIndex = -1;
		Date minDate = new Date();
		minDate.setTime(Long.MAX_VALUE);
		for (Row ev : events) {
			String na = "";
			na = ev.getString(activityColumnName);
			if (na.equals(name)) {
				LocalDateTime dt = ev.getDateTime("time:timestamp");
				Date d = Date.from(dt.atZone(ZoneId.systemDefault()).toInstant());
				if (d.before(minDate)) {
					minDate = d;
					minTimeIndex = events.indexOf(ev);
				}
			}
		}
		return minTimeIndex;

	}
	
	public int getNumberOfEvents() {
		return this.uniqueID;
	}
	
	

}
