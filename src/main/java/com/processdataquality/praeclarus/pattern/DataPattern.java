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

package com.processdataquality.praeclarus.pattern;

import com.processdataquality.praeclarus.exception.OptionException;
import com.processdataquality.praeclarus.plugin.uitemplate.PluginUI;
import tech.tablesaw.api.Table;

/**
 * @author Michael Adams
 * @date 11/5/21
 */
public interface DataPattern {

    /**
     * Detect instances of an imperfection pattern found within a table
     * @param table a table containing values to check for the pattern
     * @return a table where each row contains values detected using the pattern
     */
    Table detect(Table table) throws OptionException;


    /**
     * Repair instances of an imperfection pattern found within a table
     * @param master the original table containing pattern instances
     * @return        a table of the original data with the repairs done
     */
    Table repair(Table master) throws OptionException;


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


    /**
     * Allows plugin to define its own UI (as a template to be instantiated by the front end)
     * @return the UI template
     */
    PluginUI getUI();


    /**
     * Returns the UI updated with any changes made in the front end
     * @param ui the updated UI template
     */
    void setUI(PluginUI ui);
    
}
