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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.model.XAttributeTimestamp;

/**
 * @author Sareh Sadeghianasl
 * @date 7/1/22
 */

public class Event implements Comparable<Event> {

	private Row se;
	private Row ce;
	private double duration;
	private boolean hasDuration;
	private String cid;
	private String name;
	private String resource;
	private int rIndex;
	private Map<String, Object> completeAttributes;
	private Map<String, Object> startAttributes;
	private int indexInCase;
	private int totalIndex;
	private Event le;
	private Event ne;

	public Event(String selectedColumnName, Row se, Row ce, String cid, String resource, int rIndex, int totalIndex) {
		setStartEvent(se);
		setCompleteEvent(ce);
		setDuration();
		setCid(cid);
		name = selectedColumnName;
		this.resource = new String(resource);
		this.rIndex = rIndex;
		completeAttributes = new HashMap<String, Object>();
		this.totalIndex = totalIndex;

	}

	public Event(Event other) {
		this(other.name, other.se, other.ce, other.cid, other.resource, other.rIndex, other.totalIndex);
		this.setLastEvent(other.le);
		this.setNextEvent(other.ne);

		this.startAttributes.putAll(other.startAttributes);
		this.completeAttributes.putAll(other.completeAttributes);
	}

	public void setName(String n) {
		this.name = n;
	}

	public int getID() {
		return this.totalIndex;
	}

	@Override
	public int compareTo(Event otherEvent) {
		Date c1 = new Date();
		Date c2 = new Date();
		Date s1 = new Date();
		Date s2 = new Date();
		LocalDateTime dt1 = ce.getDateTime("time:timestamp");
		c1 = Date.from(dt1.atZone(ZoneId.systemDefault()).toInstant());
		LocalDateTime dt2 = otherEvent.getCompleteEvent().getDateTime("time:timestamp");
		c2 = Date.from(dt2.atZone(ZoneId.systemDefault()).toInstant());

		if (this.se == null) {
			if (otherEvent.getStartEvent() == null) {
				if (c1.before(c2))
					return -1;
				else if (c1.after(c2))
					return 1;
				else
					return 0;
			} else {
				LocalDateTime dt3 = otherEvent.getStartEvent().getDateTime("time:timestamp");
				s2 = Date.from(dt3.atZone(ZoneId.systemDefault()).toInstant());
				if (c1.before(s2))
					return -1;
				else if (c1.after(c2))
					return 1;
				else
					return 0;
			}
		} else {
			LocalDateTime dt4 = se.getDateTime("time:timestamp");
			s1 = Date.from(dt4.atZone(ZoneId.systemDefault()).toInstant());
			if (otherEvent.getStartEvent() == null) {
				if (c2.before(s1))
					return 1;
				else if (c2.after(c1))
					return -1;
				else
					return 0;
			} else {
				LocalDateTime dt3 = otherEvent.getStartEvent().getDateTime("time:timestamp");
				s2 = Date.from(dt3.atZone(ZoneId.systemDefault()).toInstant());
				if (c1.before(s2))
					return -1;
				else if (c2.before(s1))
					return 1;
				else
					return 0;
			}
		}
	}

	public void setIndexInCase(int i) {
		this.indexInCase = i;
	}

	public int getIndexInCase() {
		return this.indexInCase;
	}

	public void setStartEvent(Row se) {
		this.se = se;
	}

	public Row getStartEvent() {
		return se;
	}

	public void setCompleteEvent(Row ce) {
		this.ce = ce;
	}

	public Row getCompleteEvent() {
		return ce;
	}

	public void addCompleteAttribute(String key, Object value) {
		this.completeAttributes.put(key, value);
		
	}

	public void addCompleteAttributes(ArrayList<ArrayList<String>> attributes, ArrayList<String> ignoreAttrs) {
		for (ArrayList<String> atrr : attributes) {
			if (atrr.size() == 3) {
				String type = atrr.get(0);
				String key = atrr.get(1);
				String value = atrr.get(2);
				switch (type) {
					case "date":
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-ddTHH:mm:ss"); 
						LocalDateTime dateTime = LocalDateTime.parse(value, formatter);
						Date d = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
						this.completeAttributes.put(key, d);
						break;
					case "long":
						int in = Integer.parseInt(value);
						this.completeAttributes.put(key, in);
						break;
					case "double":
						double dou = Double.parseDouble(value);
						this.completeAttributes.put(key, dou);
						break;
					case "boolean":
						boolean bool = Boolean.parseBoolean(value);
						this.completeAttributes.put(key, bool);
						break;	
					default: //string
						this.completeAttributes.put(key, value);
						break;
				}
			}
		}
		
	}

	public Map<String, Object> getCompleleAttributes() {
		return this.completeAttributes;
	}

	public Map<String, Object> getStartAttributes() {
		return this.startAttributes;
	}

	public void setLastEvent(Event le) {
		this.le = le;
	}

	public Event getLastEvent() {
		return le;
	}

	public void setNextEvent(Event ne) {
		this.ne = ne;
	}

	public Event getNextEvent() {
		return ne;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getCid() {
		return cid;
	}

	public void setDuration() {
		if (se == null) {
			duration = 0;
			hasDuration = false;
		} else {
			Date s = new Date();
			LocalDateTime dt1 = se.getDateTime("time:timestamp");
			s = Date.from(dt1.atZone(ZoneId.systemDefault()).toInstant());
			Date c = new Date();
			LocalDateTime dt2 = ce.getDateTime("time:timestamp");
			c = Date.from(dt2.atZone(ZoneId.systemDefault()).toInstant());
			duration = Math.abs(s.getTime() - c.getTime());
			duration = duration / 1000;
			if (duration == 0)
				hasDuration = false;
			else
				hasDuration = true;
		}
	}

	public double getDuration() {
		return duration;
	}

	public boolean hasDuration() {
		return hasDuration;
	}

	public String getName() {
		return this.name;
	}

	public String getResource() {
		return this.resource;
	}

	public boolean hasResource() {
		if (this.resource.equals(""))
			return false;
		return true;
	}

	public int getRIndex() {
		return this.rIndex;
	}

	public boolean isEqual(Event other) {
		String on = new String(other.getName());
		return (this.name.equals(on));
	}

	public boolean isEqualID(Event other) {
		return (this.totalIndex == other.totalIndex);
	}

}
