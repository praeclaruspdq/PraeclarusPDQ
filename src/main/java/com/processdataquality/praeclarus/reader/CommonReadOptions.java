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

package com.processdataquality.praeclarus.reader;

import tech.tablesaw.io.ReadOptions;
import tech.tablesaw.io.Source;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 30/3/21
 */
public class CommonReadOptions {

    public Map<String, Object> toMap() {

        // create dummy source so we can build a default ReadOptions
        Source source = new Source(new File("temp.csv"), StandardCharsets.UTF_8);
        ReadOptions options = CsvReadOptions.builder(source).build();

        Map<String, Object> map = new HashMap<>();
        map.put("Missing Value", options.missingValueIndicators());
        map.put("Date Format", options.dateFormatter());
        map.put("DateTime Format", options.dateTimeFormatter());
        map.put("Time Format", options.timeFormatter());
        map.put("Header", options.header());
        map.put("Table Name", options.tableName());
        map.put("Sample", options.sample());
        map.put("Locale", options.locale());
        return map;
    }

}
