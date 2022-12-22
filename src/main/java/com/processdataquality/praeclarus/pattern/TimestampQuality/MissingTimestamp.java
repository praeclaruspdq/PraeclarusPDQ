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

package com.processdataquality.praeclarus.pattern.TimestampQuality;

import java.time.LocalDateTime;

import com.processdataquality.praeclarus.annotation.Pattern;
import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.pattern.PatternGroup;

import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

/**
 * A base class for imperfect label plugins
 * @author Dominik Fischer
 * @date 19/08/22
 */

@Plugin(name = "Missing Timestamp",
        author = "Dominik Fischer",
        version = "1.0",
        synopsis = "Detects missing timestamps")
@Pattern(group = PatternGroup.UNGROUPED)

public class MissingTimestamp extends TimestampPattern {

    public MissingTimestamp() {
        super();
    }

    @Override
    public void createErrorTableModel(Table table) {
        setErrorTable(table.emptyCopy());
    }

    @Override
    public void detectErrors(Table table) {
        for (Row row : table) {
            LocalDateTime testValue = row.getDateTime(getSelectedColumnNameValue("Timestamp Column"));
            if (testValue == null) {
                _detected.append(row);
            }
        }
    }
}

// 