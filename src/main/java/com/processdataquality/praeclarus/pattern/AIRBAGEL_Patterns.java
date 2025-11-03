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
        name = "Injector: AnomalousTraces",
        author = "Jonghyeon Ko, Marco Comuzzi",
        version = "1.0",
        synopsis = "Injects control-flow patterns in an event log"
)

@Pattern(group = PatternGroup.ANOMALOUS_TRACES)


public class AIRBAGEL_Patterns extends AbstractAnomalousTrace {

    private static final String PYTHON_EXEC = "python";
    private static final String SCRIPT_NAME = "main_airbagel_patterns.py";

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

	private ArrayList<String> joinTypes = new ArrayList<String>();
   

    public AIRBAGEL_Patterns() {
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
        
		joinTypes.add("Year");
		joinTypes.add("Month");
		joinTypes.add("Day");
		joinTypes.add("Hour");
		joinTypes.add("Minute");
		joinTypes.add("Second");
		
		getOptions().addDefault(
				new ListOption("5-B. TimeUnit", joinTypes));

        getOptions().addDefault("6. Prob(Mistake)", 0.02);
    }

    private String getScriptPath() {
        try {
            String projectRoot = System.getProperty("user.dir");
            return projectRoot + File.separator + "python" + File.separator + SCRIPT_NAME;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to locate main_imperfection_patterns.py", e);
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
        try {
            // 0. Python version
            printPythonVersion();

            // 1. requirements check and install
            ensurePythonRequirements();

            // 2. Read parameters.
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

            // 3. Run py          
            String scriptPath = getScriptPath();
            ProcessBuilder pb = new ProcessBuilder(PYTHON_EXEC, scriptPath, 
                                                    wskip,winsert,wrework,wmove,wreplace,
                                                    p1insert,p1rework,p1move,p2move,
                                                    prob);

            Process process = pb.start();
            
            // 4. Prepare to read stdout/stderr asynchronously to prevent deadlocks
            StringBuilder outputData = new StringBuilder();
            StringBuilder errorData = new StringBuilder();

            // Use a Consumer to append each line to the StringBuilder.
            Consumer<String> outputConsumer = line -> outputData.append(line).append(System.lineSeparator());
            Consumer<String> errorConsumer = line -> errorData.append(line).append(System.lineSeparator());

            Thread outputThread = new Thread(new StreamGobbler(process.getInputStream(), outputConsumer));
            Thread errorThread = new Thread(new StreamGobbler(process.getErrorStream(), errorConsumer));

            outputThread.start();
            errorThread.start();

            // 5. Java -> Python: Send the 'master' table to the Python script's stdin.  
            try (OutputStream stdin = process.getOutputStream()) {
                master.write().csv(stdin);
            } 
            
            // 6. Wait for the process to terminate
            int exitCode = process.waitFor();

            // 7. Wait for the stream reader threads to join (finish)
            outputThread.join();
            errorThread.join();

            // 8. Handle errors
            String errorString = errorData.toString();
            
            // DEBUGGING BLOCK
            System.out.println("--- DEBUG: Python stderr (Exit Code: " + exitCode + ") ---");
            if (errorString.isEmpty()) {
                System.out.println("[stderr was empty]");
            } else {
                System.out.println(errorString);
            }
            System.out.println("-------------------------------------------------");
            
            if (exitCode != 0) {
                System.err.println("Python Failure (Exit Code: " + exitCode + "):\n" + errorString);
                throw new InvalidOptionValueException("Python Failure: " + errorString);
            }
            
            // 9. Parse the outputData string into a Table.
            String outputString = outputData.toString();
            if (outputString.trim().isEmpty()) {
                 System.err.println("Python script returned no data (stdout was empty).");

            } else {
                try (StringReader reader = new StringReader(outputString)) {
                    _detected = Table.read().csv(reader);
                }
            }

        } catch (IOException | InterruptedException e) {
            throw new InvalidOptionValueException("Repair step failed", e);
        }
        return _detected;
    }

}