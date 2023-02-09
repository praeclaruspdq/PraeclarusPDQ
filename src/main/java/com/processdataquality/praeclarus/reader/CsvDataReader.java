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


import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionValueException;
import tech.tablesaw.io.csv.CsvReadOptions;

/**
 * @author Michael Adams
 * @date 29/3/21
 */
@Plugin(
        name = "CSV Reader",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Loads a log file consisting of lines of comma separated values.",
        fileDescriptors = "text/csv;text/plain;.csv"
)
public class CsvDataReader extends AbstractDataReader {

    public CsvDataReader() {
        super();
        getOptions().addDefault("Separator", ',');
    }


    protected CsvReadOptions getReadOptions() throws InvalidOptionValueException {
        CsvReadOptions.Builder builder = CsvReadOptions.builder(getSource());
        for (String key : getOptions().getChanges().keySet()) {
            switch (key) {
                case "Separator":
                    builder.separator(getOptions().get("Separator").asChar());
                    break;
                case "Missing Value":
                    builder.missingValueIndicator(getOptions().get("Missing Value").asString());
                    break;
                case "Header":
                    builder.header(getOptions().get("Header").asBoolean());
                    break;
                case "Table Name":
                    builder.tableName(getOptions().get("Table Name").asString());
                    break;
            }
        }
        return builder.build();
    }
        
//        return CsvReadOptions.builder(fileName)
//                .separator(_options.get("Separator").asChar())
//                .missingValueIndicator(_options.get("Missing Value").asString())
////                .dateFormat(DateTimeFormatter.ofPattern((String) _options.get("Date Format")))
////                .timeFormat(DateTimeFormatter.ofPattern((String) _options.get("Time Format")))
////                .dateTimeFormat(DateTimeFormatter.ofPattern((String) _options.get("DateTime Format")))
//                .header(_options.get("Header").asBoolean())
//                .tableName(_options.get("Table Name").asString())
////                .sampleSize((int) _options.get("Sample"))
////                .sample(((int) _options.get("Sample")) > 0)
//                .build();
//    }
    
}
