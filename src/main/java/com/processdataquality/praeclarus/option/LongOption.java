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
public class LongOption extends Option {

    private long _max = Long.MAX_VALUE;
    private long _min = Long.MIN_VALUE;

    public LongOption(String key, Long value) {
        super(key, value);
    }


    public void setMax(long max) { _max = max; }

    public void setMin(long min) { _min = min; }

    public void setRange(long min, long max) {
        setMin(min);
        setMax(max);
    }

    public long getMax() { return _max; }

    public long getMin() { return _min; }


    public void setValue(long value) throws InvalidOptionValueException {
        if (value < _min) {
            throw new InvalidOptionValueException("Value is less than minimum limit");
        }
        if (value > _max) {
            throw new InvalidOptionValueException("Value is greater than maximum limit");
        }
        super.setValue(value);
    }

    @Override
    public Long value() {
        return (Long) super.value();
    }
    
}
