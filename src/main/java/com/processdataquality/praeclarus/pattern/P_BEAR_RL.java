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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.function.Consumer;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;


import com.processdataquality.praeclarus.annotation.Pattern;
import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionException;
import com.processdataquality.praeclarus.exception.InvalidOptionValueException;
import com.processdataquality.praeclarus.exception.OptionException;

import tech.tablesaw.api.Table;



/**
 * @author Jonghyeon Ko
 * @date 03/10/25
 */
@Plugin(
        name = "P-BEAR-RL",
        author = "Jonghyeon Ko",
        version = "0.1",
        synopsis = "Repair anomalous traces in an event log"
)
@Pattern(group = PatternGroup.ANOMALOUS_TRACES)

public class P_BEAR_RL extends AbstractPBEARRL {
    
    private static final Logger LOG = Logger.getLogger(P_BEAR_RL.class.getName());

    private static final String PYTHON_EXEC = "python";
    private static final String SCRIPT_NAME = "main_pbear.py";    

    // Column names for the detected anomaly table
    private static final String ANOMALY_CASE_ID_COL = "Case ID";
    private static final String ANOMALY_PATTERN_COL = "AnomalyPatterns";

    private static final String DATA_DELIMITER = "---PBEAR_DATA_SEPARATOR---";
    public static final String NORMAL_LOG_KEY = "Normal Log";

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


    public P_BEAR_RL() {
        super();
        getOptions().addDefault("#_Episode", 1000);
        getOptions().addDefault("Alpha", 0.00);
        // Ensure AbstractPatternInjector uses the correct result table structure
    }
    
    /**
     * Print the current Python version (e.g., Python 3.11.6)
     */
    private void printPythonVersion() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(PYTHON_EXEC, "--version");
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
    private static final String REQUIREMENTS = "requirements.txt";
    private void ensurePythonRequirements() throws IOException, InterruptedException {
        String projectRoot = System.getProperty("user.dir");
        File reqFile = new File(projectRoot + File.separator + "python", REQUIREMENTS);

        if (!reqFile.exists()) {
            System.out.println("⚠ requirements.txt not found, skipping dependency check.");
            return;
        }

        ProcessBuilder pb = new ProcessBuilder(
                PYTHON_EXEC, "-m", "pip", "install", "-r", reqFile.getAbsolutePath(), "-q"
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
        }
    }

    /**
     * Executes the Python script to run P-BEAR-RL, which saves the anomalies to a file.
     * Then reads the anomaly file to populate the _detected table for the UI.
     */
    @Override
    public Table detect(Table table) throws InvalidOptionException, OptionException {
    	

        // --- START DEBUG CHECK (Added log message) ---
        LOG.info("P_BEAR_RL pattern component execution started.");
        // --- END DEBUG CHECK ---
        
        // 1. Execute Python script
        try {
        	
            // --- START PYTHON ENVIRONMENT CHECK ---
            printPythonVersion();
            ensurePythonRequirements();
            // --- END PYTHON ENVIRONMENT CHECK ---
            
        	_detected = createResultTable();
        	
            String scriptPath = getScriptPath(); // Assumed to be inherited/defined in AbstractPatternInjector

            // Read parameters from options
            
            String numEpisode = String.valueOf(getOptions().get("#_Episode").asInt());
            String alpha = String.valueOf(getOptions().get("Alpha").asDouble());

            
            // FIX: Use ProcessBuilder with inheritIO(), similar to ScatteredCase, for robust long-running process handling.
            ProcessBuilder pb = new ProcessBuilder(PYTHON_EXEC, scriptPath);
            
            // Add options as arguments (sys.argv[1], sys.argv[2] in Python)
            pb.command().add(numEpisode); 
            pb.command().add(alpha);
            
            LOG.info("Executing Python command with args: " + pb.command().stream().collect(Collectors.joining(" ")));
            
            Process process = pb.start();
            
            // 4. Prepare to read stdout/stderr asynchronously to prevent deadlocks
            StringBuilder outputData = new StringBuilder();
            StringBuilder errorData = new StringBuilder();

            // Use a Consumer to append each line to the StringBuilder.
            Consumer<String> outputConsumer = line -> outputData.append(line).append(System.lineSeparator());
//            Consumer<String> errorConsumer = line -> errorData.append(line).append(System.lineSeparator());

            
            Consumer<String> errorConsumer = line -> {
                System.out.println("[Py-stderr] " + line); // <-- 실시간 출력 추가
                errorData.append(line).append(System.lineSeparator());
            };
            
            Thread outputThread = new Thread(new StreamGobbler(process.getInputStream(), outputConsumer));
            Thread errorThread = new Thread(new StreamGobbler(process.getErrorStream(), errorConsumer));
            

            
            outputThread.start();
            errorThread.start();

            
            // 5. Java -> Python: Send the 'master' table to the Python script's stdin.  
            
            // --- THIS IS THE KEY ---
            // 'table' is the anomalous log (from the preprocessor)
            // 'normalTable' is retrieved from storage (put there by the preprocessor)
            LOG.info("Attempting to get '" + NORMAL_LOG_KEY + "' from AuxiliaryDatasets...");
            Table normalTable = getAuxiliaryDatasets().getTable(NORMAL_LOG_KEY);
            
            if (normalTable == null) {
                LOG.severe("!!! '" + NORMAL_LOG_KEY + "' NOT FOUND in Auxiliary Datasets.");
                throw new InvalidOptionException(
                    "Auxiliary dataset '" + NORMAL_LOG_KEY + "' is missing. " +
                    "Did you forget to run the 'Preprocess P-BEAR-RL' plugin first?"
                );
            }
            
            LOG.info("Successfully retrieved '" + NORMAL_LOG_KEY + "'. Converting tables to CSV...");
            ByteArrayOutputStream anomalousBaos = new ByteArrayOutputStream();
            table.write().csv(anomalousBaos);
            
            ByteArrayOutputStream normalBaos = new ByteArrayOutputStream();
            normalTable.write().csv(normalBaos);

            // Write both CSVs to the *single* stdin stream, separated by our delimiter
            try (OutputStream stdin = process.getOutputStream()) {
                stdin.write(anomalousBaos.toByteArray());
                stdin.write(("\n" + DATA_DELIMITER + "\n").getBytes(StandardCharsets.UTF_8));
                stdin.write(normalBaos.toByteArray());
                stdin.flush();
            }
            LOG.info("Sent data to Python stdin. Waiting for process...");
            
            // 6. Wait for the process to terminate
            int exitCode = process.waitFor();

            // 7. Wait for the stream reader threads to join (finish)
            outputThread.join();
            errorThread.join();

            // 8. Handle errors
            String errorString = errorData.toString();
            LOG.info("Python process finished with exit code: " + exitCode);

            // DEBUGGING BLOCK
            System.out.println("--- DEBUG: Python stderr ---");
            if (errorString.isEmpty()) {
                System.out.println("[stderr was empty]");
            } else {
                System.out.println(errorString);
            }
            System.out.println("----------------------------");
            
            if (exitCode != 0) {
                System.err.println("Python Failure (Exit Code: " + exitCode + "):\n" + errorString);
                throw new InvalidOptionValueException("Python Failure: " + errorString);
            }
            
            LOG.info("P-BEAR-RL script executed successfully.");
            String outputString = outputData.toString();
            
            
            if (outputString.trim().isEmpty()) {
                System.err.println("Python script returned no data (stdout was empty).");
                this.detectedAnomalyReport = null; 

            } else {
               try (StringReader reader = new StringReader(outputString)) {
               	this.detectedAnomalyReport = Table.read().csv(reader);
                getAuxiliaryDatasets().put("PBEAR_FULL_REPORT", this.detectedAnomalyReport);
               }
            }
            

            // Filter the table to keep only the required columns and rename them
            if (this.detectedAnomalyReport != null &&
            	this.detectedAnomalyReport.containsColumn(ANOMALY_CASE_ID_COL) && 
            	this.detectedAnomalyReport.containsColumn(ANOMALY_PATTERN_COL)) {
                
                // Keep only Case ID and AnomalyPatterns
            	Table detectedAnomalyReport_table = this.detectedAnomalyReport.select(ANOMALY_CASE_ID_COL, ANOMALY_PATTERN_COL);
                detectedAnomalyReport_table = detectedAnomalyReport_table.dropDuplicateRows(); 

                // Set the detected table for the UI
                _detected = detectedAnomalyReport_table;
                LOG.info("Detected anomaly table");

            }
            
            
        } catch (IOException | InterruptedException e) {
            throw new InvalidOptionException("Failed to run Python script. Ensure Python environment is correct and script exists.", e);
        }

        // Ensure the anomaly column is the second column (index 1) for the UI (as per AbstractAnomalousTrace structure)
        if (_detected.columnNames().size() > 1 && !_detected.column(1).name().equals("Root-Cause of Anomalies")) {
            _detected.column(1).setName("Root-Cause of Anomalies");
        }
        return _detected;
    }

    /**
     * Utility method to get the Python script path.
     */
    private String getScriptPath() {
        // Implementation from AbstractPatternInjector (assumed to be correct path calculation)
        String projectRoot = System.getProperty("user.dir");
        return projectRoot + File.separator + "python" + File.separator + SCRIPT_NAME;
    }

}
