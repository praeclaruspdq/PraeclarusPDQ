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

import java.util.ArrayList;
import java.util.Date;

import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;

/**
 * @author Sareh Sadeghianasl
 * @date 7/1/22
 */

public class ActivityData {
	private String name;
	private String type;
	private ArrayList<Object> values;
	private ArrayList<Object> UniqueValuesArray;
	private ArrayList<Integer> quantities;
	private double[] valuesPdf;
	private Object[] UniqueValues;
	private double vmr;

	public ActivityData(String name, Object value) {
		this.name = name;
		this.values = new ArrayList<Object>();
		this.type = new String();
		addValue(value);
		setType();
	}

	public String getName() {
		return this.name;
	}

	private void setType() {
		Object o = values.get(0);
		if (o instanceof String)
			this.type = "String";
		else if (o instanceof Boolean)
			this.type = "Boolean";
		else if (o instanceof Integer)
			this.type = "Integer";
		else if (o instanceof Double)
			this.type = "Float";
		else if (o instanceof Date)
			this.type = "Date";
		else
			this.type = "String";
		//		if (this.type.equalsIgnoreCase("String")) {
		//			if (isInteger(o.toString())) {
		//				this.type = "Integer";
		//			} else if (isFloat(o.toString())) {
		//				this.type = "Float";
		//			} else if (isDouble(o.toString())) {
		//				this.type = "Double";
		//			} else if (isBoolean(o.toString())) {
		//				this.type = "Boolean";
		//			}
		//		}
	}

	public void computePdf() {
		UniqueValuesArray = new ArrayList<>();
			quantities = new ArrayList<>();
			for (Object o : values) {
				int index = findValueIndex(o);
				if (index == -1) {
					int sIndex = findSortedIndex(o);
					if (sIndex == -1) {
						UniqueValuesArray.add(o);
						quantities.add(1);
					} else {
						UniqueValuesArray.add(sIndex, o);
						quantities.add(sIndex, 1);
					}
				} else {
					quantities.set(index, quantities.get(index) + 1);
				}
			}
			//Main.out.println("Number of Unique values: " + UniqueValuesArray.size());
			valuesPdf = new double[UniqueValuesArray.size()];
			UniqueValues = new Object[UniqueValuesArray.size()];
			for (int i = 0; i < valuesPdf.length; i++) {
				double temp = quantities.get(i);
				valuesPdf[i] = temp / values.size();
				UniqueValues[i] = UniqueValuesArray.get(i);
				//Main.out.println("Unique value: " + UniqueValues[i] + ", Quantity: "
				//		+ quantities.get(i) + ", Probability: " + valuesPdf[i]);
			}
	}

	private int findValueIndex(Object o) {
		for (Object o1 : UniqueValuesArray) {
			if (o.toString().equals(o1.toString())) {
				return UniqueValuesArray.indexOf(o1);
			}
		}
		return -1;
	}

	private int findSortedIndex(Object o) {
		for (Object o1 : UniqueValuesArray) {
			int i = UniqueValuesArray.indexOf(o1);
			if (i != UniqueValuesArray.size() - 1) {
				int j = i + 1;
				Object o2 = UniqueValuesArray.get(j);
				if (o1.toString().compareTo(o.toString()) < 0
						&& o2.toString().compareTo(o.toString()) > 0) {
					return j;
				}
			}
		}
		return -1;
	}

	public ArrayList<Object> getValues() {
		return this.values;
	}

	public Object[] getUniqueValues() {
		return this.UniqueValues;
	}

	public double[] getvaluesPdf() {
		return this.valuesPdf;
	}

	public String getType() {
		return this.type;
	}

	boolean isInteger(String strNum) {
		try {
			int d = Integer.parseInt(strNum);
		} catch (NumberFormatException | NullPointerException nfe) {
			return false;
		}
		return true;
	}

	boolean isFloat(String strNum) {
		try {
			float d = Float.parseFloat(strNum);
		} catch (NumberFormatException | NullPointerException nfe) {
			return false;
		}
		return true;
	}

	boolean isDouble(String strNum) {
		try {
			double d = Double.parseDouble(strNum);
		} catch (NumberFormatException | NullPointerException nfe) {
			return false;
		}
		return true;
	}

	boolean isBoolean(String strNum) {
		if (strNum.equalsIgnoreCase("true") || strNum.equalsIgnoreCase("false")) {
			return true;
		}
		return false;
	}

	public void addValue(Object value) {
		this.values.add(value);
	}

	public int getValuesSize() {
		return this.values.size();
	}
}
