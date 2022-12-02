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

import com.processdataquality.praeclarus.exception.WorkflowNotFoundException;
import com.processdataquality.praeclarus.ui.canvas.Workflow;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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


    public static StoredWorkflow save(String id, String owner, boolean shared, String json) {
        return save(new StoredWorkflow(id, owner, shared, json));
    }

    public static List<StoredWorkflow> findall() {
        List<StoredWorkflow> list = new ArrayList<>();
        repository.findAll().forEach(list::add);
        return list;
    }

    public static StoredWorkflow findById(String id) throws WorkflowNotFoundException {
        return repository.findById(id).orElseThrow(() -> new WorkflowNotFoundException(id));
    }

    public static List<StoredWorkflow> findPrivate(String owner) {
        return repository.findByOwnerAndSharedFalse(owner);
    }

    public static List<StoredWorkflow> findPublic() {
        return repository.findBySharedTrue();
    }

    public static List<StoredWorkflow> findPrivateOrPublic(String owner) {
        return repository.findByOwnerOrSharedTrue(owner);
    }


    public static StoredWorkflow save(Workflow workflow) {
        workflow.getGraph().updateLastSavedTime();
        String json = workflow.asJson().toString();      // also triggers props update
        return save(new StoredWorkflow(
                workflow.getGraph().getId(),
                workflow.getGraph().getOwner(),
                workflow.getGraph().isShared(),
                json));
    }

    public static StoredWorkflow save(StoredWorkflow storedWorkflow) {
        return repository.save(storedWorkflow);
    }


    public static void delete(StoredWorkflow storedWorkflow) {
        repository.delete(storedWorkflow);
    }

}
