/*
 * Copyright (c) 2022 Queensland University of Technology
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

package com.processdataquality.praeclarus.exception;

/**
 * @author Michael Adams
 * @date 16/3/2022
 */
public class NodeRunnerException extends Exception {

    public NodeRunnerException() {
    }

    public NodeRunnerException(String message) {
        super(message);
    }

    public NodeRunnerException(String message, Throwable cause) {
        super(message, cause);
    }

    public NodeRunnerException(Throwable cause) {
        super(cause);
    }

    public NodeRunnerException(String message, Throwable cause,
                               boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
