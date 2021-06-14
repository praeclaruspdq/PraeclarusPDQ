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

import com.processdataquality.praeclarus.annotations.Plugin;
import com.processdataquality.praeclarus.plugin.Options;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.fixed.FixedWidthWriteOptions;

import java.io.IOException;

/**
 * @author Michael Adams
 * @date 12/4/21
 */
@Plugin(
        name = "Fixed Width Writer",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Writes the log output to a Fixed Width Format file."
)
public class FixedWidthDataWriter extends AbstractDataWriter {

    private Options _options;

    @Override
    public void write(Table table) throws IOException {
        if (_options == null) getOptions();  // fill null options with defaults
        FixedWidthWriteOptions options = FixedWidthWriteOptions.builder(
                _options.get("Destination").asString())
                .header(_options.get("Header").asBoolean())
                .padding(_options.get("Pad char").asChar())
                .build();
        table.write().usingOptions(options);
    }

    @Override
    public Options getOptions() {
        if (_options == null) {
            _options = new Options();
            _options.addDefault("Header", true);
            _options.addDefault("Destination", "out.fw");
            _options.addDefault("Pad char", ' ');
        }
        return _options;
    }

    @Override
    public int getMaxInputs() {
        return 1;
    }

    @Override
    public int getMaxOutputs() {
        return 0;
    }
}
