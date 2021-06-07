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
     * @return true if this plugin can accept another plugin as input (i.e. the
     * threshold of allowable inputs for this plugin has not yet been reached)
     */
    int getMaxInputs();


    /**
     * @return true if this plugin can accept another plugin as output (i.e. the
     * threshold of allowable outputs for this plugin has not yet been reached)
     */
    int getMaxOutputs();

}
