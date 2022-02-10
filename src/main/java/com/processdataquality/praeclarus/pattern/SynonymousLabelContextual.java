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

import com.processdataquality.praeclarus.activitysimilaritymeasures.*;
import com.processdataquality.praeclarus.annotations.Pattern;
import com.processdataquality.praeclarus.annotations.Plugin;
import com.processdataquality.praeclarus.logelements.Activity;
import com.processdataquality.praeclarus.logelements.ParseTable;
import com.processdataquality.praeclarus.math.Pair;
import com.processdataquality.praeclarus.plugin.Options;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.ArrayList;

/**
 * @author Sareh Sadeghianasl
 * @date 11/1/22
 */
@Plugin(name = "Contextual",
		author = "Sareh Sadeghianasl",
		version = "1.0",
		synopsis = "Calculates activity label similarity using contextual information in the log")
@Pattern(group = PatternGroup.SYNONYMOUS_LABELS)

public class SynonymousLabelContextual extends AbstractImperfectLabelSimilarity {

	double[][] rs, ds, ts, dcfs, eds, ls;

	@Override
	public Options getOptions() {
		Options options = super.getOptions();

		// TODO define limit for each option

		if (!options.containsKey("Direct Control Flow Noise Threshold")) {
			options.addDefault("Direct Control Flow Noise Threshold", 0.05);
		}
		if (!options.containsKey("Overall Context Similarity Threshold")) {
			options.addDefault("Overall Context Similarity Threshold", 0.6);
		}
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

	@Override
	protected void detect(Table table, StringColumn selectedColumn) {
		ParseTable parser = new ParseTable(table, selectedColumn.name());
		parser.parse();
		rs = new ResourceSimilarity(parser.getActivities()).getSimilarity();
		ds = new DurationSimilarity(parser.getActivities()).getSimilarity();
		ts = new TimeSimilarity(parser.getActivities()).getSimilarity();
		dcfs = new ControlFlowSimilarity(parser.getActivities(), parser.getTraces(),
				getOptions().get("Direct Control Flow Noise Threshold").asDouble()).getDirectControlFlowSimilarity();
		eds = new EventDataSimilarity(parser.getActivities()).getSimilarity();
		ls = new StringSimilarity(parser.getActivities()).getSimilarity();

		ArrayList<Pair<Activity, Activity>> res = new ArrayList<Pair<Activity, Activity>>();
		double[][] grouped = new double[parser.getActivities().size()][parser.getActivities().size()];
		double[][] activityContextSimilariy = new double[parser.getActivities().size()][parser.getActivities().size()];
		for (int i = 0; i < parser.getActivities().size(); i++) {
			for (int j = 0; j < parser.getActivities().size(); j++) {
				Activity a1 = parser.getActivities().get(i);
				Activity a2 = parser.getActivities().get(j);
				if (j > i) {
					double overS = overallSimilarity(i, j);
					activityContextSimilariy[i][j] = overS;
					if (overS > getOptions().get("Overall Context Similarity Threshold").asDouble() && 
							ls[i][j]> getOptions().get("String Similarity Threshold").asDouble()) {
						res.add(new Pair<Activity, Activity>(a1, a2));
						addResult(selectedColumn, a1.getName(), a2.getName(), overS, ls[i][j], dcfs[i][j], rs[i][j], ts[i][j], ds[i][j], eds[i][j]);
						grouped[i][j] = overS;
					}
				} else if (j < i) {
					activityContextSimilariy[i][j] = activityContextSimilariy[j][i];
				} else {
					activityContextSimilariy[i][j] = 1;
				}
			}
		}
	}
	
	/**
	 * Calculates the average context similarity of activity labels a1 and a2 based
	 * on the dimension weights in the options
	 * 
	 * @param i the index of a1
	 * @param j the index of a2
	 * @return the average context similarity
	 */

	private double overallSimilarity(int i, int j) {
		double score = 0;
		double duW = getOptions().get("Duration Similarity Weight").asInt();
		double tW = getOptions().get("Time Similarity Weight").asInt();
		double rW = getOptions().get("Resource Similarity Weight").asInt();
		double dcfW = getOptions().get("Control Flow Similarity Weight").asInt();
		double edW = getOptions().get("Data Similarity Weight").asInt();
		double duScore = ds[i][j];
		double rScore = rs[i][j];
		double dcfScore = dcfs[i][j];
		double edScore = eds[i][j];
		double tScore = ts[i][j];
		if (duScore == -1)
			duW = 0;
		if (rScore == -1)
			rW = 0;
		if (edScore == -1)
			edW = 0;
		if (tScore == -1)
			tW = 0;
		if (duW + rW + dcfW + edW + tW != 0) {
			score = ((duW * duScore) + (rW * rScore) + (dcfW * dcfScore) + (edW * edScore) + (tW * tScore))
					/ (duW + rW + dcfW + edW + tW);
		} else {
			duW = rW = edW = tW = dcfW = 1;
			score = ((duW * duScore) + (rW * rScore) + (dcfW * dcfScore) + (edW * edScore) + (tW * tScore))
					/ (duW + rW + dcfW + edW + tW);
		}
		return score;
	}
	
	/**
	 * This plugin cannot repair a log, it only detects synonymous labels
	 * 
	 * @return false
	 */
	
//	@Override
//	public boolean canRepair() {
//		return false;
//	}

}
