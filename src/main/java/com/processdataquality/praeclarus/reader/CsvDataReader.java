/*
 * Copyright (c) 2021 Queensland University of Technology
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


import com.processdataquality.praeclarus.annotations.PluginMetaData;
import com.processdataquality.praeclarus.plugin.Options;
import tech.tablesaw.io.csv.CsvReadOptions;

/**
 * @author Michael Adams
 * @date 29/3/21
 */
@PluginMetaData(
        name = "CSV Reader",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Loads a log file consisting of lines of comma separated values."
)
public class CsvDataReader extends AbstractDataReader {

    @Override
    public Options getOptions() {
        Options options = super.getOptions();
        if (! options.containsKey("Separator")) {
            options.put("Separator", ',');
        }
        return options;
    }


    protected CsvReadOptions getReadOptions() {
        String fileName = (String) _options.get("Source");
        return CsvReadOptions.builder(fileName)
                .separator((char) _options.get("Separator"))
                .missingValueIndicator((String) _options.get("Missing Value"))
//                .dateFormat(DateTimeFormatter.ofPattern((String) _options.get("Date Format")))
//                .timeFormat(DateTimeFormatter.ofPattern((String) _options.get("Time Format")))
//                .dateTimeFormat(DateTimeFormatter.ofPattern((String) _options.get("DateTime Format")))
                .header((boolean) _options.get("Header"))
                .tableName((String) _options.get("Table Name"))
//                .sampleSize((int) _options.get("Sample"))
//                .sample(((int) _options.get("Sample")) > 0)
                .build();
    }

}
