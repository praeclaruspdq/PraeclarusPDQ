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
import org.apache.commons.text.similarity.LevenshteinDistance;
import tech.tablesaw.api.StringColumn;

/**
 * @author Michael Adams
 * @date 11/5/21
 */
@Plugin(
        name = "Levenshtein",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Calculates activity label similarity using Levenshtein Distance"
)
@Pattern(group = PatternGroup.DISTORTED_LABEL)
public class DistortedLabelLevenshtein extends AbstractImperfectLabel {

    public DistortedLabelLevenshtein() {
        super();
        getOptions().addDefault("Threshold", 2);
    }


    @Override
    protected void detect(StringColumn column, String s1, String s2) {
        int threshold = getOptions().get("Threshold").asInt();
        LevenshteinDistance levenshtein = new LevenshteinDistance(threshold);
        int distance = levenshtein.apply(s1, s2);
        if (distance > 0 && distance <= levenshtein.getThreshold()) {
            addResult(column, s1, s2);
        }
    }

}
