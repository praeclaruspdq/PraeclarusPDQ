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
import tech.tablesaw.io.html.HtmlReadOptions;

/**
 * @author Michael Adams
 * @date 30/3/21
 */
@Plugin(
        name = "HTML Reader",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Loads a log file formatted as HTML."
)
public class HtmlDataReader extends AbstractDataReader {

    public HtmlDataReader() {
        super();
        getOptions().addDefault("Table Index", 0);
    }


    protected HtmlReadOptions getReadOptions() throws InvalidOptionValueException {
        return HtmlReadOptions.builder(getSource())
                .missingValueIndicator(getOptions().get("Missing Value").asString())
//                .dateFormat(DateTimeFormatter.ofPattern((String) _options.get("Date Format")))
//                .timeFormat(DateTimeFormatter.ofPattern((String) _options.get("Time Format")))
//                .dateTimeFormat(DateTimeFormatter.ofPattern((String) _options.get("DateTime Format")))
                .header(getOptions().get("Header").asBoolean())
                .tableName(getOptions().get("Table Name").asString())
                .sample(getOptions().get("Sample").asBoolean())
                .tableIndex(getOptions().get("Table Index").asInt())
                .build();
    }

}
