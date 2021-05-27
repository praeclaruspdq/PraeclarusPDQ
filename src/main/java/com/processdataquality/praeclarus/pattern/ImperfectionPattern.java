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

package com.processdataquality.praeclarus.pattern;

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import tech.tablesaw.api.Table;

/**
 * @author Michael Adams
 * @date 11/5/21
 */
public interface ImperfectionPattern extends PDQPlugin {

    /**
     * Detect instances of an imperfection pattern found within a table
     * @param table a table containing values to check for the pattern
     * @return a table where each row contains values detected using the pattern
     */
    Table detect(Table table);


    /**
     * Repair instances of an imperfection pattern found within a table
     * @param master the original table containing pattern instances
     * @param changes a table containing the rows and values that describe
     *                the necessary changes to repair the pattern instances found
     * @return        a table of the original data with the repairs done
     */
    Table repair(Table master, Table changes);


    /**
     * Determines whether this particular plugin can detect the imperfection pattern
     * (or only repair)
     * @return true if this plugin can detect the imperfection pattern
     */
    boolean canDetect();

    /**
     * Determines whether this particular plugin can repair the data
     * @return true if this plugin can repair the data
     */
    boolean canRepair();
}
