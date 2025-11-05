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

import tech.tablesaw.api.Table;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.function.Consumer;
/**
 * @author Jonghyeon Ko,Marco Comuzzi
 * @date 19/9/25
 */
@Plugin(
        name = "Injector: ScatteredEvent",
        author = "Jonghyeon Ko, Marco Comuzzi",
        version = "1.0",
        synopsis = "Injects an imperfection pattern in an event log"
)

@Pattern(group = PatternGroup.SCATTERED_EVENT)

public class ScatteredEventInjector extends AbstractImperfectInjector {

    private static final String PYTHON_EXEC = "python";
    private static final String SCRIPT_NAME = "main_imperfection_patterns.py";

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

    public ScatteredEventInjector() {
        super();
        getOptions().addDefault("1. Target", "[Activity:'Make decision'>>('Make revision1', 'Make revision2')]");
        getOptions().addDefault("2. Action", "[Resource]_[0-9:{2}][a-zA-Z:{5}]_[Timestamp*(%Y%m%d %H%M%S%f)]");
        getOptions().addDefault("3. Loc", "[Description:idx(-1)]");
        getOptions().addDefault("4. Del", "True");
        getOptions().addDefault("5. Time start", "2023-09-26 09:00:00.000");
        getOptions().addDefault("6. Time end", "2023-12-26 09:00:00.000");
        getOptions().addDefault("7. Ratio", 0.1);
        getOptions().addDefault("8. Declare", "Chain Response[Make decision, Notify accept] |A.Resource is Manager-000001 |T.Resource is Manager-000003 |");
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
    public Table repair(Table master) throws InvalidOptionException {
        try {
            // 0. Python version
            printPythonVersion();

            // 1. requirements check and install
            ensurePythonRequirements();

            // 2. Read parameters.
            String target = getOptions().get("1. Target").asString();
            String action = getOptions().get("2. Action").asString();
            String loc = getOptions().get("3. Loc").asString();
            String del = getOptions().get("4. Del").asString();
            String timeStart = getOptions().get("5. Time start").asString();
            String timeEnd = getOptions().get("6. Time end").asString();
            String ratio = getOptions().get("7. Ratio").asString();
            String declare = getOptions().get("8. Declare").asString();

            // 3. Run py
            String scriptPath = getScriptPath();

            String inputParameter_pattern = "Scattered Event"; 
            ProcessBuilder pb = new ProcessBuilder(PYTHON_EXEC, scriptPath, 
                                                    inputParameter_pattern,
                                                    target,
                                                    action,
                                                    loc,
                                                    del,
                                                    timeStart,
                                                    timeEnd,
                                                    ratio,
                                                    declare);            // ProcessBuilder pb = new ProcessBuilder(PYTHON_EXEC, scriptPath);
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