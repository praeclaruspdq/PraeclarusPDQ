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

import java.util.List;

/**
 * @author Sareh Sadeghianasl
 * @date 8/2/23
 */
public class ListOption extends Option {

    private String _selected;

    public ListOption(String key, List<?> value) {
        super(key, value);                               // String value by default
    }


    @Override
    public Object value() {                 // value can be String or List<String>
        return super.value() != null ? super.value() : "";
    }


    public String getSelected() {
        if (_selected != null) return _selected;
        if (value() instanceof String) return (String) value();
        return "" ;
    }

    public void setSelected(String selected) {
        _selected = selected;  
    }
}
