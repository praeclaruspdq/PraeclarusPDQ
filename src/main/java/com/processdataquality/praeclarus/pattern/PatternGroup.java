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

/**
 * @author Michael Adams
 * @date 12/5/21
 */
public enum PatternGroup {

    FORM_BASED("Form Based"),
    TIME_TRAVEL("Time Travel"),
    UNANCHORED_EVENT("Unanchored Event"),
    SCATTERED_EVENT("Scattered Event"),
    ELUSIVE_CASE("Elusive Case"),
    SCATTERED_CASE("Scattered Case"),
    COLLATERAL_EVENTS("Collateral Events"),
    POLLUTED_LABEL("Polluted Label"),
    DISTORTED_LABEL("Distorted Label"),
    SYNONYMOUS_LABELS("Synonymous Labels"),
    HOMONYMOUS_LABELS("Homonymous Labels"),
    ANOMALOUS_TRACES("Anomalous Traces"),
    UNGROUPED("Ungrouped");

    private final String _name;

    PatternGroup(String name) { _name = name; }

    public String getName() { return _name; }
}
