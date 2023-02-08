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
 * @date 17/3/2022
 */
public class InvalidOptionValueException extends RuntimeException {

    public InvalidOptionValueException() {
    }

    public InvalidOptionValueException(String message) {
        super(message);
        super.initCause(getCause());
    }

    public InvalidOptionValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidOptionValueException(Throwable cause) {
        super(cause);
    }

    public InvalidOptionValueException(String message, Throwable cause,
                                       boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
