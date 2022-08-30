/*
 * Copyright (c) 2022 Queensland University of Technology
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

package com.processdataquality.praeclarus.option;

import java.util.UUID;

/**
 * An interface for objects providing user-configurable options (parameters) at runtime
 * @author Michael Adams
 * @date 5/5/2022
 */
public interface HasOptions {

    /**
     * @return A map of configuration parameters that may be populated by implementers.
     */
    Options getOptions();


    /**
     * A default identifier for the implementing object. Expected to be overridden, but
     * overriding is not mandatory
     * @return a UUID representing a unique identifier for the implementing object
     */
    default String getId() { return UUID.randomUUID().toString(); }


    /**
     * All implementers must provide a name
     * @return the name (or label) of the implementing object
     */
    String getName();

}
