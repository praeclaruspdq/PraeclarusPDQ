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

package com.processdataquality.praeclarus.option;

import com.processdataquality.praeclarus.exception.InvalidOptionValueException;

/**
 * @author Michael Adams
 * @date 29/3/2022
 */
public class Range<T extends Number & Comparable<T>> {

    private T _min;
    private T _max;
    private boolean _minSet = false;
    private boolean _maxSet = false;


    public Range() { }

    public Range(T min, T max) {
        setMin(min);
        setMax(max);
    }


    public void setMin(T min) {
        _min = min;
        _minSet = true;
    }


    public void setMax(T max) {
        _max = max;
        _maxSet = true;
    }


    public void check(T val) {
        if (_minSet && compare(val, _min) < 0) {
            throw new InvalidOptionValueException("Invalid value: less than lower constraint");
        }
        if (_maxSet && compare(val, _max) > 0) {
            throw new InvalidOptionValueException("Invalid value: exceeds upper constraint");
        }
    }


    private int compare(T n1, T n2) {
        return n1.compareTo(n2);
    }


    public static void main(String[] args) {
        Range<Double> r = new Range<>(0.05, 4.0);
        r.check(4.0);
    }

}
