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

package com.processdataquality.praeclarus.ui.repo;

import com.processdataquality.praeclarus.ui.canvas.Workflow;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.stereotype.Component;

/**
 * @author Michael Adams
 * @date 4/5/2022
 */
@Component
public class WorkflowStore {

    private static WorkflowRepository repository;

    public WorkflowStore(WorkflowRepository repo) {
        repository = repo;
    }


    public StoredWorkflow save(String id, String owner, boolean shared, String json) {
        return save(new StoredWorkflow(id, owner, shared, json));
    }

    public StoredWorkflow save(Workflow workflow) throws JSONException {
        return save(new StoredWorkflow(
                workflow.getNetwork().getId(),
                workflow.getNetwork().getOwner(),
                workflow.getNetwork().isShared(),
                workflow.asJson().toString()));
    }

    public StoredWorkflow save(StoredWorkflow storedWorkflow) {
        return repository.save(storedWorkflow);
    }



}
