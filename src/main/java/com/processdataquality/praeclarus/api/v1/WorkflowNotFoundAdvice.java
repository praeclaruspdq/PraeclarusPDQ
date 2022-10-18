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

package com.processdataquality.praeclarus.api.v1;

import com.processdataquality.praeclarus.exception.WorkflowNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Michael Adams
 * @date 17/10/2022
 */
@ControllerAdvice
public class WorkflowNotFoundAdvice {

    @ResponseBody
    @ExceptionHandler(WorkflowNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String workflowNotFoundHandler(WorkflowNotFoundException wnfe) {
        return wnfe.getMessage();
    }
}
