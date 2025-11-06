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
import com.processdataquality.praeclarus.exception.InvalidOptionValueException;
import com.processdataquality.praeclarus.option.ListOption;

import tech.tablesaw.api.Table;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.function.Consumer;
import java.util.ArrayList;

/**
 * @author Jonghyeon Ko,Marco Comuzzi
 * @date 19/9/25
 */
@Plugin(
        name = "Injector: Control-Flow Anomalies",
        author = "Jonghyeon Ko, Marco Comuzzi",
        version = "1.0",
        synopsis = "Injects control-flow anomaly patterns in an event log"
)

@Pattern(group = PatternGroup.ANOMALOUS_TRACES)


public class ControlFlowAnomalyInjector extends AbstractAnomalousTrace {

    private static final String PYTHON_EXEC = "python";
    private static final String SCRIPT_NAME_PATTERNS = "main_airbagel_patterns.py";
    private static final String SCRIPT_NAME_ATTRIBUTES = "main_airbagel_attributes.py";

    private Table _detected;

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

	private ArrayList<String> selectTypes = new ArrayList<String>();
	private ArrayList<String> selectAttrTypes = new ArrayList<String>();
   

    public ControlFlowAnomalyInjector() {
        super();
        // _detected = createResultTable();
        getOptions().addDefault("1. Weight(Skip)", 0.2);
        getOptions().addDefault("2. Weight(Insert)", 0.2);
        getOptions().addDefault("2-A. MaxLen(Insert)", 3);
        getOptions().addDefault("3. Weight(Replace)", 0.2);
        getOptions().addDefault("4. Weight(Rework)", 0.2);
        getOptions().addDefault("4-A. MaxLen(Rework)", 3);
        getOptions().addDefault("5. Weight(Move)", 0.2);
        getOptions().addDefault("5-A. MaxDiff(Move)", 5);
        
		selectTypes.add("Year");
		selectTypes.add("Month");
		selectTypes.add("Day");
		selectTypes.add("Hour");
		selectTypes.add("Minute");
		selectTypes.add("Second");
		
		getOptions().addDefault(
				new ListOption("5-B. TimeUnit", selectTypes));

        getOptions().addDefault("6. Prob(Mistake)", 0.02);

        
        // --- Merged Attribute Options ---
        selectAttrTypes.add("None"); // "None" or ""
        selectAttrTypes.add("Resource");
        selectAttrTypes.add("System");
        getOptions().addDefault(
                new ListOption("7. Synthetic Attribute", selectAttrTypes));

        getOptions().addDefault("8. Number (ResGr, Sys)", 4);
        getOptions().addDefault("9. Size (ResGr)", 0);
    }

    private String getScriptPath(String scriptName) {
        try {
            String projectRoot = System.getProperty("user.dir");
            return projectRoot + File.separator + "python" + File.separator + scriptName;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to locate " + scriptName, e);
        }
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

    private String readStream(java.io.InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }


	@Override
	public boolean canDetect() {
		return false;
	}
    
    @Override
	public boolean canRepair() {
		return true;
	}

    @Override
    public Table repair(Table master) throws InvalidOptionException {
    	
    	Table logForPatterns; 
    	
        try {
            // 0. Python version and requirements check
            printPythonVersion();
            ensurePythonRequirements();

            // 1. Check if Attribute Generation is needed
            String attr = ((ListOption) getOptions().get("7. Synthetic Attribute")).getSelected();
            
            if (attr == null || attr.equals("None")) {
                System.out.println("--- Skipping Attribute Generation Stage (None selected) ---");
                logForPatterns = master;
            } else {
                // --- 2. Run First Script (Attributes) ---
                System.out.println("--- Running Attribute Generation Stage ---");
                
                // Read attribute parameters
                String num = getOptions().get("8. Number (ResGr, Sys)").asString();
                String size = getOptions().get("9. Size (ResGr)").asString();

                String scriptPathAttrs = getScriptPath(SCRIPT_NAME_ATTRIBUTES);
                ProcessBuilder pbAttrs = new ProcessBuilder(PYTHON_EXEC, scriptPathAttrs, 
                                                        attr, num, size);

                Process processAttrs = pbAttrs.start();
                
                // Process 1: stdout/stderr
                StringBuilder outputDataAttrs = new StringBuilder();
                StringBuilder errorDataAttrs = new StringBuilder();
                Consumer<String> outputConsumerAttrs = line -> outputDataAttrs.append(line).append('\n');
                Consumer<String> errorConsumerAttrs = line -> errorDataAttrs.append(line).append(System.lineSeparator());
                Thread outputThreadAttrs = new Thread(new StreamGobbler(processAttrs.getInputStream(), outputConsumerAttrs));
                Thread errorThreadAttrs = new Thread(new StreamGobbler(processAttrs.getErrorStream(), errorConsumerAttrs));
                outputThreadAttrs.start();
                errorThreadAttrs.start();

                // Process 1: stdin (Send the original master log)
                try (OutputStream stdin = processAttrs.getOutputStream()) {
                    master.write().csv(stdin);
                } 
                
                // Process 1: Wait and check
                int exitCodeAttrs = processAttrs.waitFor();
                outputThreadAttrs.join();
                errorThreadAttrs.join();

                String errorStringAttrs = errorDataAttrs.toString();
                System.out.println("--- DEBUG: Python stderr (Attributes) (Exit Code: " + exitCodeAttrs + ") ---");
                System.out.println(errorStringAttrs.isEmpty() ? "[stderr was empty]" : errorStringAttrs);
                System.out.println("-------------------------------------------------");

                if (exitCodeAttrs != 0) {
                    throw new InvalidOptionValueException("Attribute Generation script failed: " + errorStringAttrs);
                }

                // Process 1: Parse result
                String outputStringAttrs = outputDataAttrs.toString();
                if (outputStringAttrs.trim().isEmpty()) {
                     throw new InvalidOptionValueException("Attribute script returned no data.");
                } else {
                    try (StringReader reader = new StringReader(outputStringAttrs)) {
                        logForPatterns = Table.read().csv(reader);
                    }
                }
            }

        } catch (IOException | InterruptedException e) {
            throw new InvalidOptionValueException("Attribute Generation step failed", e);
        }

        // --- 3. Run Second Script (Patterns) ---
        try {
            System.out.println("--- Running Pattern Injection Stage ---");
            
            // Read pattern parameters
            String wskip = getOptions().get("1. Weight(Skip)").asString();
            String winsert = getOptions().get("2. Weight(Insert)").asString();
            String p1insert = getOptions().get("2-A. MaxLen(Insert)").asString();
            String wreplace = getOptions().get("3. Weight(Replace)").asString();
            String wrework = getOptions().get("4. Weight(Rework)").asString();
            String p1rework = getOptions().get("4-A. MaxLen(Rework)").asString();
            String wmove = getOptions().get("5. Weight(Move)").asString();
            String p1move = getOptions().get("5-A. MaxDiff(Move)").asString();
            String p2move = ((ListOption) getOptions().get("5-B. TimeUnit")).getSelected();  
            String prob = getOptions().get("6. Prob(Mistake)").asString();
            
            String scriptPathPatterns = getScriptPath(SCRIPT_NAME_PATTERNS);
            ProcessBuilder pbPatterns = new ProcessBuilder(PYTHON_EXEC, scriptPathPatterns, 
                                                    wskip,winsert,wrework,wmove,wreplace,
                                                    p1insert,p1rework,p1move,p2move,
                                                    prob);

            Process processPatterns = pbPatterns.start();
            
            // Process 2: stdout/stderr
            StringBuilder outputDataPatterns = new StringBuilder();
            StringBuilder errorDataPatterns = new StringBuilder();
            Consumer<String> outputConsumerPatterns = line -> outputDataPatterns.append(line).append('\n');
            Consumer<String> errorConsumerPatterns = line -> errorDataPatterns.append(line).append(System.lineSeparator());
            Thread outputThreadPatterns = new Thread(new StreamGobbler(processPatterns.getInputStream(), outputConsumerPatterns));
            Thread errorThreadPatterns = new Thread(new StreamGobbler(processPatterns.getErrorStream(), errorConsumerPatterns));
            outputThreadPatterns.start();
            errorThreadPatterns.start();

            // Process 2: stdin (Send the result of the first script)
            try (OutputStream stdin = processPatterns.getOutputStream()) {
                logForPatterns.write().csv(stdin);
            } 
            
            // Process 2: Wait and check
            int exitCodePatterns = processPatterns.waitFor();
            outputThreadPatterns.join();
            errorThreadPatterns.join();

            String errorStringPatterns = errorDataPatterns.toString();
            System.out.println("--- DEBUG: Python stderr (Patterns) (Exit Code: " + exitCodePatterns + ") ---");
            System.out.println(errorStringPatterns.isEmpty() ? "[stderr was empty]" : errorStringPatterns);
            System.out.println("-------------------------------------------------");
            
            if (exitCodePatterns != 0) {
                throw new InvalidOptionValueException("Pattern Injection script failed: " + errorStringPatterns);
            }
            
            // Process 2: Parse final result
            String outputStringPatterns = outputDataPatterns.toString();
            if (outputStringPatterns.trim().isEmpty()) {
                 throw new InvalidOptionValueException("Pattern script returned no data.");
            } else {
                try (StringReader reader = new StringReader(outputStringPatterns)) {
                    _detected = Table.read().csv(reader); 
                }
            }

        } catch (IOException | InterruptedException e) {
            throw new InvalidOptionValueException("Pattern Injection step failed", e);
        }

        return _detected;
    }

}