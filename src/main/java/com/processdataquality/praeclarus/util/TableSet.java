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
import tech.tablesaw.columns.Column;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 3/3/2022
 */
public class TableSet extends Table {

    private Set<Table> children = new HashSet<>();

    public TableSet(String name, Column<?>... columns) {
        super(name, columns);
    }

    public TableSet(String name, Collection<Column<?>> columns) {
        super(name, columns);
    }


    public void addChild(Table table) { children.add(table); }

    public boolean removeChild(Table table) { return children.remove(table); }

    public Set<Table> getChildren() { return children; }

    public boolean hasChildren() { return ! children.isEmpty(); }
    
}
