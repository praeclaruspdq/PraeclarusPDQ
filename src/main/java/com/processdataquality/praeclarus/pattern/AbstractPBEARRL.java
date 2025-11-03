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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;


import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionException;
import com.processdataquality.praeclarus.exception.OptionException;
import com.processdataquality.praeclarus.plugin.uitemplate.ButtonAction;
import com.processdataquality.praeclarus.plugin.uitemplate.PluginUI;
import com.processdataquality.praeclarus.plugin.uitemplate.UIButton;
import com.processdataquality.praeclarus.plugin.uitemplate.UIContainer;
import com.processdataquality.praeclarus.plugin.uitemplate.UITable;

import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;


public abstract class AbstractPBEARRL extends AbstractDataPattern {
	
	protected Table _detected;
	protected Table finalLog;
	protected Logger _log;

	protected Table detectedAnomalyReport;
    private static final String CASE_ID_UNLABELED = "Case ID"; // "caseid-anomalous-trace-plugin"; 
    private static final String ORDER_COL = "Order";
    private static final String ACTIVITY_COL = "Activity";
    private static final String ANOMALY_CASE_ID_COL = "Case ID";
    private static final String ANOMALY_PATTERN_COL = "AnomalyPatterns";
    
    
    /**
     * Helper class to consume an InputStream asynchronously to prevent deadlocks.
     */
    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    consumer.accept(line);
                }
            } catch (IOException e) {
                System.err.println("StreamGobbler IOException: " + e.getMessage());
            }
        }
    }


    protected AbstractPBEARRL() {
        super();
        _detected = createResultTable(); 
        _log = Logger.getLogger(this.getName());      
    }
    

    @Override
	public Table detect(Table table) throws OptionException {
		return _detected;
	}
	
	/**
     * Repair instances of an anomalous trace by removing the traces corresponding to
     * the anomalous case ids detected
     * @param master the original input table
     * @return        a table where anomalous traces are removed
     */
    @Override
    public Table repair(Table master) throws InvalidOptionException {

        // Final desired columns for the output log: Case, Activity, AnomalyPatterns, Order
        final String[] FINAL_COLUMNS = {
            ANOMALY_CASE_ID_COL, 
            ACTIVITY_COL, 
            ANOMALY_PATTERN_COL, 
            ORDER_COL
        };
        
        // 1. Get the list of cases selected for repair from the UI (Substitution targets)
        Table repairsTable = getRepairs(); 
        if (repairsTable.isEmpty()) {
            return master; 
        }
        List<String> caseIdsToRepair = repairsTable.column(ANOMALY_CASE_ID_COL).asStringColumn().asList();
        
        // 2. Get the list of all detected anomaly cases (the entire list shown in the UI)
        List<String> allDetectedCaseIds = _detected.column(ANOMALY_CASE_ID_COL).asStringColumn().asList();

        // 3. Extract (normal) traces from the master log, excluding all detected anomalous cases
        Table nonAnomalousCases = master.where(
            master.column(CASE_ID_UNLABELED).asStringColumn().isNotIn(allDetectedCaseIds));


        // --- 4. Process nonAnomalousCases (Normal, Unaffected Cases) ---
        // These need to be converted to the final four-column schema.
        Table normalCasesProcessed = formatLogToFinalSchema(nonAnomalousCases, false);


        // 5. (Retained cases) Extract traces that were detected but *not* selected for repair
        List<String> caseIdsToKeepOriginal = allDetectedCaseIds.stream()
            .filter(id -> !caseIdsToRepair.contains(id))
            .collect(Collectors.toList());
        
        // Extract the traces to be retained (original form) from the master table
        Table casesToKeepOriginal = master.where(
            master.column(CASE_ID_UNLABELED).asStringColumn().isIn(caseIdsToKeepOriginal));
        // --- 6. Process casesToKeepOriginal (Anomalous but Retained) ---
        // These need to be converted to the final four-column schema.
        Table retainedCasesProcessed = formatLogToFinalSchema(casesToKeepOriginal, true);


        Table fullReport = getAuxiliaryDatasets().getTable("PBEAR_FULL_REPORT");

        // Add a null check in case the state was lost
        if (fullReport == null) {
            throw new InvalidOptionException(
                "P-BEAR full report is missing. Please re-run the detect() step."
            );
        }

        Table repairedCases = fullReport.where(
                fullReport.column(CASE_ID_UNLABELED).asStringColumn().isIn(caseIdsToRepair));        
                
        Table repairedCasesProcessed;

        repairedCasesProcessed = repairedCases.select(FINAL_COLUMNS);

        Table filtered = null;

        filtered = normalCasesProcessed
            .append(retainedCasesProcessed) 
            .append(repairedCasesProcessed);     
        
        // Clean up the temporary case ID column if it exists and return
        if (filtered.containsColumn(CASE_ID_UNLABELED)) {
            filtered.removeColumns(CASE_ID_UNLABELED);
        }
        return filtered;
    }
    


    public Table getRepairs() { 
        List<UITable> tables = _ui.extractTables();
        return tables.get(0).getTable();
    }


    /**
     * Converts an event log fragment (non-anomalous or retained anomalous) from the master 
     * structure to the final four-column output structure (Case, Activity, AnomalyPatterns, Order).
     * @param logFragment The table containing traces.
     * @param isRetained True if these are anomalous cases that were chosen to be retained (needs explanation string).
     * @return A new table with the target schema.
     */
    private Table formatLogToFinalSchema(Table logFragment, boolean isRetained) throws InvalidOptionException {
        // 1. Select the required columns from the master log structure
        final String[] MASTER_COLS = {
            CASE_ID_UNLABELED, // This column is renamed later
            ACTIVITY_COL,
            ORDER_COL 
        };
        
        try {
            Table processed = logFragment.select(MASTER_COLS);

            // 2. Rename the internal case ID column to the public ANOMALY_CASE_ID_COL ("Case ID")
            processed.column(CASE_ID_UNLABELED).setName(ANOMALY_CASE_ID_COL);

            // 3. Add the AnomalyPatterns column
            StringColumn anomalyCol = StringColumn.create(ANOMALY_PATTERN_COL);
            String placeholder = isRetained 
                ? "Anomaly detected but trace retained" 
                : "Normal trace";
            
            // Fill the new column with the appropriate placeholder
            for (int i = 0; i < processed.rowCount(); i++) {
                anomalyCol.append(placeholder);
            }
            processed.addColumns(anomalyCol);
            
            return processed;
            
        } catch (IllegalArgumentException e) {
            throw new InvalidOptionException("Master log or detected log is missing core columns (Case ID, Activity, or Order).", e);
        }
    }

	/**
     * Creates the table that will receive the imperfect values detected
     * @return the empty table
     */
    protected Table createResultTable() {
        return Table.create("Result").addColumns(
                StringColumn.create("Case ID"),
                StringColumn.create("Root-Cause of Anomalies")
               
        );
    }


    @Override
    public PluginUI getUI() {
        if (_ui == null) {
            String title = getClass().getAnnotation(Plugin.class).name() + " - Detected";
            _ui = new PluginUI(title);

            UITable table = new UITable(_detected);
            table.setMultiSelect(true);
            UIContainer tableLayout = new UIContainer();
            tableLayout.add(table);
            _ui.add(tableLayout);

            UIContainer buttonLayout = new UIContainer(UIContainer.Orientation.HORIZONTAL);
            buttonLayout.add(new UIButton(ButtonAction.CANCEL));
            buttonLayout.add(new UIButton(ButtonAction.REPAIR));
            _ui.add(buttonLayout);
        }
        return _ui;
   }

   // --- START: Utility methods moved from P_BEAR_RL ---

   /**
    * Print the current Python version (e.g., Python 3.11.6)
    */
    protected void printPythonVersion(String pythonExec) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(pythonExec, "--version");
        pb.redirectErrorStream(true);  
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line = reader.readLine();
            if (line != null && !line.isEmpty()) {
                System.out.println("Python Version: " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.err.println("⚠ Failed to get Python version (exit code " + exitCode + ")");
        }
    }

    /**
     * Check and install Python requirements before running script
     */
    protected void ensurePythonRequirements(String pythonExec, String requirementsFileName) throws IOException, InterruptedException {
        String projectRoot = System.getProperty("user.dir");
        File reqFile = new File(projectRoot + File.separator + "python", requirementsFileName);

        if (!reqFile.exists()) {
            System.out.println("⚠ " + requirementsFileName + " not found, skipping dependency check.");
            return;
        }

        ProcessBuilder pb = new ProcessBuilder(
                pythonExec, "-m", "pip", "install", "-r", reqFile.getAbsolutePath(), "-q"
        );
        
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            String errorOutput = readStream(process.getErrorStream());
            System.err.println("❌ Failed to install Python dependencies. Exit code: " + exitCode);
            System.err.println("--- Error Details ---");
            System.err.println(errorOutput);
            throw new RuntimeException("Failed to install Python dependencies. Exit code: " + exitCode);
        } else {
             System.out.println("Python dependencies checked/installed successfully.");
        }
    }

    /**
     * Reads an InputStream into a String.
     * @param inputStream The stream to read.
     * @return The string content of the stream.
     * @throws IOException
     */
    protected String readStream(java.io.InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }
   

    /**
     * Creates and starts a new thread for asynchronously consuming a stream.
     * @param inputStream The stream to consume.
     * @param consumer The consumer logic.
     * @return The started Thread.
     */
    protected Thread startStreamGobbler(InputStream inputStream, Consumer<String> consumer) {
         Thread thread = new Thread(new StreamGobbler(inputStream, consumer));
         thread.start();
         return thread;
    }
    
   // --- END: Utility methods moved from P_BEAR_RL ---
	
}


