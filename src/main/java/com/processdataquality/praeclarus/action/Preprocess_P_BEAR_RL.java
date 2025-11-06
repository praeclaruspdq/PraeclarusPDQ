/*
 * Copyright (c) 2021-2023 Queensland University of Technology
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

package com.processdataquality.praeclarus.action;

import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionValueException;
import tech.tablesaw.api.Table;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author Jonghyeon Ko
 * @date 03/10/25
 * * This is an Action plugin designed to be run *before* P-BEAR-RL.
 * It takes two tables as input:
 * 1. (Input 0) The Anomalous Log
 * 2. (Input 1) The Normal Log
 * * It stores the Normal Log in the AuxiliaryDatasets storage under the
 * name "Normal Log" and then passes the Anomalous Log to the next
 * plugin in the workflow.
 */
@Plugin(
    name = "Preprocess P-BEAR-RL", 
    author = "Jonghyeon Ko", 
    version = "1.0", 
    synopsis = "Prepares inputs for P-BEAR-RL by storing the Normal Log in Auxiliary Datasets."
)
public class Preprocess_P_BEAR_RL extends AbstractAction {

    private static final Logger LOG = Logger.getLogger(Preprocess_P_BEAR_RL.class.getName());
    public static final String NORMAL_LOG_KEY = "Normal Log";
    public static final String EXPECTED_NORMAL_TABLE_NAME = "Normal";

    public Preprocess_P_BEAR_RL() {
        super();
        // This plugin has no user-configurable options
    }

    /**
     * Executes the action.
     * @param inputList A list of tables. Expects exactly two:
     * inputList.get(0) = Anomalous Log
     * inputList.get(1) = Normal Log
     * @return The Anomalous Log (table 0), to be passed to the next plugin.
     */
    @Override
    public Table run(List<Table> inputList) throws InvalidOptionValueException {
        
        LOG.info("Starting P-BEAR-RL Preprocessor Action.");

        // 1. Validate inputs (similar to Join.java)
        if (inputList == null || inputList.size() != 2) {
            throw new InvalidOptionValueException(
                "This action requires exactly two tables as input: 1. Anomalous Log, 2. Normal Log.");
        }

        Table tableA = inputList.get(0);
        Table tableB = inputList.get(1);

        if (tableA == null || tableB == null) {
            throw new InvalidOptionValueException("Input tables cannot be null.");
        }

        LOG.info("Received Table Normal Data (" + tableA.rowCount() + " rows)");
        LOG.info("Received Table Anomalous Data (" + tableB.rowCount() + " rows)");

        Table anomalousLog;
        Table normalLog;
        
        if (tableA.rowCount() > tableB.rowCount()) {
            normalLog = tableA;
            anomalousLog = tableB;
        } else {
            // Default case: tableB is longer, equal, or was the intended normal log
            normalLog = tableB;
            anomalousLog = tableA;
        }
        
        LOG.info("Identified Anomalous Log: '" + anomalousLog.name() + "' (" + anomalousLog.rowCount() + " rows)");
        LOG.info("Identified Normal Log: '" + normalLog.name() + "' (" + normalLog.rowCount() + " rows)");


        // 3. "Self-save" the Normal Log to Auxiliary Datasets
        // This makes it available to the main P-BEAR-RL Pattern plugin
        try {
            getAuxiliaryDatasets().put(NORMAL_LOG_KEY, normalLog);
            LOG.info("Successfully stored '" + NORMAL_LOG_KEY + "' in AuxiliaryDatasets.");
        } catch (Exception e) {
            throw new InvalidOptionValueException("Failed to store Normal Log in AuxiliaryDatasets.", e);
        }
        
        // 4. Pass the Anomalous Log down the workflow pipeline
        return anomalousLog;
    }

    /**
     * This action requires exactly 2 inputs.
     * @return 2
     */
    @Override
    public int getMaxInputs() {
        return 2;
    }
}
