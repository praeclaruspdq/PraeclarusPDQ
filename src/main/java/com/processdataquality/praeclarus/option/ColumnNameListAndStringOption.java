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

import com.processdataquality.praeclarus.support.math.Pair;

/**
 * @author Sareh Sadeghianasl
 * @date 8/2/23
 */
public class ColumnNameListAndStringOption extends Option {

    private Pair<String,String> _selected;

    public ColumnNameListAndStringOption(String key) {
        super(key, new Pair<String,String> ("",""));        // Pair of string values by default
    }


    @Override
    public Object value() {                 
        return super.value() != null ? super.value() : new Pair<String,String> ("","");
    }


    @SuppressWarnings("unchecked")
	public Pair<String,String> getSelected() {
        if (_selected != null) return _selected;
        if (value() instanceof Pair<?,?>) return (Pair<String,String>) value();
        return new Pair<String,String> ("","") ;
    }

    public void setSelected(Pair<String,String> selected) {
        _selected = selected;
    }
}
