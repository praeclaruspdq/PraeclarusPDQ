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

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Michael Adams
 * @date 4/5/2022
 */
@Repository
public interface WorkflowRepository extends CrudRepository<StoredWorkflow, String> {

    List<StoredWorkflow> findByOwner(String owner);

    List<StoredWorkflow> findBySharedTrue();

    List<StoredWorkflow> findByOwnerAndSharedFalse(String owner);

    List<StoredWorkflow> findByOwnerOrSharedTrue(String owner);

}
