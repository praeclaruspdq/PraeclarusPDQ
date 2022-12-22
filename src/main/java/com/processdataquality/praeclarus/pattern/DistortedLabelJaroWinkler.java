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

import com.processdataquality.praeclarus.annotation.Pattern;
import com.processdataquality.praeclarus.annotation.Plugin;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import tech.tablesaw.api.StringColumn;

/**
 * @author Michael Adams
 * @date 11/5/21
 */
@Plugin(
        name = "Jaro-Winkler",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Calculates activity label similarity using JaroWinkler Distance"
)
@Pattern(group = PatternGroup.DISTORTED_LABEL)
public class DistortedLabelJaroWinkler extends AbstractImperfectLabel {

    private final JaroWinklerDistance jaroWinkler = new JaroWinklerDistance();

    public DistortedLabelJaroWinkler() {
        super();
        getOptions().addDefault("Threshold", 0.7);
    }


    @Override
    protected void detect(StringColumn column, String s1, String s2) {
        double threshold = getOptions().get("Threshold").asDouble();
        double distance = jaroWinkler.apply(s1, s2);
        if (distance > threshold && distance < 1.0) {
            addResult(column, s1, s2);
        }
    }

}
