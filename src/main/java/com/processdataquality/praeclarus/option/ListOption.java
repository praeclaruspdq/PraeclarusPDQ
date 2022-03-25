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
 * @author Michael Adams
 * @date 25/3/2022
 */
public class ListOption<T> extends Option {

    private T _selected;
    private boolean _readOnly;

    public ListOption(String key, List<T> value) {
        super(key, value);
    }


    public T getSelected() { return _selected; }

    public void setSelected(T s) { _selected = s; }


    public boolean isReadOnly() { return _readOnly; }

    public void setReadOnly(boolean ro) { _readOnly = ro; }


    @SuppressWarnings("unchecked")
    public List<T> value() {
        return (List<T>) super.value();
    }
    
}
