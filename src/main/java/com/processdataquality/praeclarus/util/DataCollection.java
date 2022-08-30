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

package com.processdataquality.praeclarus.util;

import tech.tablesaw.api.Table;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 27/7/2022
 */
public class DataCollection {

    private final Map<String, Object> map = new HashMap<>();

    public DataCollection() { }

    public Object put(String k, Object v) {
        return map.put(k, v);
    }

    public void putAll(DataCollection collection) {
        map.putAll(collection.getAll());
    }


    public Object get(String k) {
        return map.get(k);
    }

    private Map<String, Object> getAll() {
        return new HashMap<>(map);
    }

    
    public Table getTable(String k) {
        Object o = get(k);
        return (o instanceof Table) ? (Table) o : null;
    }


}

