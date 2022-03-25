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

import com.processdataquality.praeclarus.annotations.Pattern;
import com.processdataquality.praeclarus.annotations.Plugin;
import com.processdataquality.praeclarus.option.Options;

/**
 * @author Sareh Sadeghianasl
 * @date 11/1/22
 */
@Plugin(name = "Contextual (Synonymous)",
		author = "Sareh Sadeghianasl",
		version = "1.0",
		synopsis = "Calculates activity label similarity using contextual information in the log")
@Pattern(group = PatternGroup.SYNONYMOUS_LABELS)

public class SynonymousLabelContextual extends AbstractImperfectLabelContextual {

	@Override
	public Options getOptions() {
		Options options = super.getOptions();

		// TODO define limit for each option

		if (!options.containsKey("String Similarity Threshold")) {
			options.addDefault("String Similarity Threshold", 0.5);
		}
		if (!options.containsKey("Control Flow Similarity Weight")) {
			options.addDefault("Control Flow Similarity Weight", 1);
		}
		if (!options.containsKey("Resource Similarity Weight")) {
			options.addDefault("Resource Similarity Weight", 1);
		}
		if (!options.containsKey("Data Similarity Weight")) {
			options.addDefault("Data Similarity Weight", 1);
		}
		if (!options.containsKey("Time Similarity Weight")) {
			options.addDefault("Time Similarity Weight", 1);
		}
		if (!options.containsKey("Duration Similarity Weight")) {
			options.addDefault("Duration Similarity Weight", 1);
		}

		return options;
	}

	
	/**
	 * This plugin cannot repair a log, it only detects synonymous labels
	 * 
	 * @return false
	 */
	
	@Override
	public boolean canRepair() {
		return false;
	}

}
