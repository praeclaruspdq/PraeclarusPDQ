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


import tech.tablesaw.api.Table;
import tech.tablesaw.io.Source;

import java.io.IOException;

/**
 * @author Michael Adams
 * @date 29/3/21
 */
public interface DataReader {

    /**
     * Fills a Table object with data from a source
     * @return the filled Table object
     * @throws IOException if anything goes wrong
     */
    Table read() throws IOException;


    /**
     * Sets the input source for this reader
     * @param source A tablesaw Source (may reference a File, InputStream, etc.)
     */
    void setSource(Source source);


    /**
     * @return the currently set input data source (if any)
     */
    Source getSource();
    
}
