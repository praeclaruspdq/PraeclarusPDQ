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

package com.processdataquality.praeclarus.plugin;

import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.option.HasOptions;

/**
 * The base interface for all plugins in the environment
 *
 * @author Michael Adams
 * @date 6/4/21
 */
public interface PDQPlugin extends HasOptions {
    
    /**
     * @return the maximum number of plugins that can be connected as inputs to this
     * plugin (i.e. the threshold of allowable inputs for this plugin)
     */
    int getMaxInputs();


    /**
     * @return the maximum number of plugins that can be connected as outputs from this
     * plugin (i.e. the threshold of allowable outputs for this plugin)
     */
    int getMaxOutputs();


    /**
     * @return a default name for this plugin, either the name provided in the plugin's
     * metadata, or the simple name of the plugin class
     */
    default String getName() {
        Plugin metaData = getClass().getAnnotation(Plugin.class);
        return metaData != null ? metaData.name() : getClass().getSimpleName();
    }

}
