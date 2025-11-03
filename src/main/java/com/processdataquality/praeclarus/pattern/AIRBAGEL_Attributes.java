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
 * @author Jonghyeon Ko
 * @date 01/10/25
 */
@Plugin(
        name = "Root-Cause Generator",
        author = "Jonghyeon Ko",
        version = "1.0",
        synopsis = "Injects synthetic root-cause attributes in an event log"
)

@Pattern(group = PatternGroup.ANOMALOUS_TRACES)

public class AIRBAGEL_Attributes extends AbstractAnomalousTrace {

    private static final String PYTHON_EXEC = "python";
    private static final String SCRIPT_NAME = "main_airbagel_attributes.py";

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
    public AIRBAGEL_Attributes() {
        super();

		joinTypes.add("Resource");
		joinTypes.add("System");
		getOptions().addDefault(
				new ListOption("1. Attribute", joinTypes));
	
        
        getOptions().addDefault("2. Number (ResGr, Sys)", 4);
        getOptions().addDefault("3. Size (ResGr)", 0);

    }

    private String getScriptPath() {
        try {
            String projectRoot = System.getProperty("user.dir");
            return projectRoot + File.separator + "python" + File.separator + SCRIPT_NAME;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to locate main_airbagel_attributes.py", e);
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

            // 1. Read parameters.
            String attr = ((ListOption) getOptions().get("1. Attribute")).getSelected();
            String num = getOptions().get("2. Number (ResGr, Sys)").asString();
            String size = getOptions().get("3. Size (ResGr)").asString();


            // 2. Run py
            String scriptPath = getScriptPath();

            ProcessBuilder pb = new ProcessBuilder(PYTHON_EXEC, scriptPath, 
                                                    attr,
                                                    num,
                                                    size);

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