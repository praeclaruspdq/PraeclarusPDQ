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

import com.processdataquality.praeclarus.ui.repo.StoredWorkflow;
import com.processdataquality.praeclarus.ui.repo.WorkflowStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Michael Adams
 * @date 17/10/2022
 */
@RestController
@RequestMapping("api/v1")
public class PdqController {

    @GetMapping("/workflows")
    List<StoredWorkflow> all() {
        return WorkflowStore.findall();
    }


    @GetMapping("/workflows/{id}")
    StoredWorkflow one(@PathVariable String id) {
        return WorkflowStore.findById(id);
    }


    @GetMapping("/test")
    String test() {
        return "Successful!";
    }

}
