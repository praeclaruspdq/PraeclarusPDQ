/*
 * Copyright (c) 2021-2022 Queensland University of Technology
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

package com.processdataquality.praeclarus.plugin.uitemplate;

import tech.tablesaw.api.Table;

/**
 * @author Michael Adams
 * @date 1/11/21
 */
public class UITable implements UIComponent {

    private final Table _originalTable;
    private Table _updatedTable = null;
    private Table _selectedRows = null;
    private boolean _multiSelect = false;

    public UITable(Table table) {
        _originalTable = table;
    }

    public Table getTable() { return _originalTable; }


    public void setUpdatedTable(Table table) { _updatedTable = table; }

    public Table getUpdatedTable() {
        return _updatedTable != null ? _updatedTable : Table.create();
    }


    public void setSelectedRows(Table table) { _selectedRows = table; }

    public Table getSelectedRows() {
        return _selectedRows != null ? _selectedRows : Table.create();
    }


    public boolean isMultiSelect() { return _multiSelect; }

    public void setMultiSelect(boolean b) { _multiSelect = b; }
}
