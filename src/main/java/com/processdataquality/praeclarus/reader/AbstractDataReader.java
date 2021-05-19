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

import com.processdataquality.praeclarus.plugin.Options;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.ReadOptions;

import java.io.IOException;

/**
 * @author Michael Adams
 * @date 29/4/21
 */
public abstract class AbstractDataReader implements DataReader {

    protected Options _options;
    
    protected abstract ReadOptions getReadOptions();

    @Override
    public Table read() throws IOException {
        return Table.read().usingOptions(getReadOptions());
    }


    @Override
    public Options getOptions() {
        if (_options == null) {
            _options = new Options();
            _options.putAll(new CommonReadOptions().toMap());
        }
        return _options;
    }


    @Override
    public void setOptions(Options options) {
        _options = options;
    }

}
