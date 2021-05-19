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

package com.processdataquality.praeclarus.writer;

import com.processdataquality.praeclarus.annotations.PluginMetaData;
import com.processdataquality.praeclarus.plugin.Options;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvWriteOptions;

import java.io.IOException;

/**
 * @author Michael Adams
 * @date 31/3/21
 */
@PluginMetaData(
        name = "CSV Writer",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Writes the log output to a CSV file."
)
public class CsvDataWriter implements DataWriter {

    private Options _options;

    @Override
    public void write(Table table) throws IOException {
        if (_options == null) getOptions();  // fill null options with defaults
        CsvWriteOptions options = CsvWriteOptions.builder((String) _options.get("Destination"))
                .header((boolean) _options.get("Header"))
                .separator((char) _options.get("Separator"))
                .quoteChar((char) _options.get("Quote"))
                .build();
        table.write().usingOptions(options);
    }

    @Override
    public Options getOptions() {
        if (_options == null) {
            _options = new Options();
            _options.put("Header", true);
            _options.put("Destination", "out.csv");
            _options.put("Separator", ',');
            _options.put("Quote", '\"');
        }
        return _options;
    }

    @Override
    public void setOptions(Options options) {
        _options = options;
    }

}
