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
 * @date 25/3/2022
 */
public class DoubleOption extends Option {

    private double _max = Double.MAX_VALUE;
    private double _min = Double.MIN_VALUE;

    public DoubleOption(String key, Double value) {
        super(key, value);
    }


    public void setMax(double max) { _max = max; }

    public void setMin(double min) { _min = min; }

    public void setRange(double min, double max) {
        setMin(min);
        setMax(max);
    }

    public double getMax() { return _max; }

    public double getMin() { return _min; }


    public void setValue(double value) throws InvalidOptionValueException {
        if (value < _min) {
            throw new InvalidOptionValueException("Value is less than minimum limit");
        }
        if (value > _max) {
            throw new InvalidOptionValueException("Value is greater than maximum limit");
        }
        super.setValue(value);
    }


    public Double value() { return (Double) super.value(); }
    

}
