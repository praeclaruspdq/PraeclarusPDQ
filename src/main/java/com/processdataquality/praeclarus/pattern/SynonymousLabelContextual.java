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
import com.processdataquality.praeclarus.exception.InvalidOptionException;
import com.processdataquality.praeclarus.option.Options;

import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

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

	public SynonymousLabelContextual() {
		super();
	}


	protected void addDefaultOptions() {
		super.addDefaultOptions();
		Options options = super.getOptions();

		// TODO define limit for each option
		options.addDefault("String Similarity Threshold", 0.5);
		options.addDefault("Control Flow Similarity Weight", 1);
		options.addDefault("Resource Similarity Weight", 1);
		options.addDefault("Data Similarity Weight", 1);
		options.addDefault("Time Similarity Weight", 1);
		options.addDefault("Duration Similarity Weight", 1);
	}
	
	@Override
	protected void detect(Table table, StringColumn selectedColumn, String sortColName) throws InvalidOptionException {
		super.detect(table, selectedColumn, sortColName);
		super.addSimilarityResults(table);
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
