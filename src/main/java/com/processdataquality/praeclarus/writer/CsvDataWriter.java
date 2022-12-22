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

package com.processdataquality.praeclarus.writer;

import com.processdataquality.praeclarus.annotation.Plugin;
import tech.tablesaw.io.csv.CsvWriteOptions;

/**
 * @author Michael Adams
 * @date 31/3/21
 */
@Plugin(
        name = "CSV Writer",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Writes the log output to a CSV file.",
        fileDescriptors = "CSV Files;text/csv;.csv"
)
public class CsvDataWriter extends AbstractDataWriter {

    public CsvDataWriter() {
        super();
        addDefaultOptions();
    }


    private void addDefaultOptions() {
        getOptions().addDefault("Header", true);
        getOptions().addDefault("Separator", ',');
        getOptions().addDefault("Quote", '\"');
    }


    @Override
    protected CsvWriteOptions getWriteOptions() {
        CsvWriteOptions.Builder builder = CsvWriteOptions.builder(getDestination());
        for (String key : getOptions().getChanges().keySet()) {
            if (key.equals("Header")) {
                builder.header(getOptions().get("Header").asBoolean());
            }
            if (key.equals("Separator")) {
                builder.separator(getOptions().get("Separator").asChar());
            }
            if (key.equals("Quote")) {
                builder.quoteChar(getOptions().get("Quote").asChar());
            }
        }
        return builder.build();
    }

}
