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

package com.processdataquality.praeclarus.plugin;

/**
 * @author Michael Adams
 * @date 6/4/21
 */
public interface PDQPlugin {
    
    /**
     * @return A map of configuration parameters for the plugin.
     */
    Options getOptions();

    /**
     * Sets the configuration parameters for the plugin
     * @param options a map of configuration keys and values
     */
    void setOptions(Options options);

}
