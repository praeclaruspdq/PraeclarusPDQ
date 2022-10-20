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

package com.processdataquality.praeclarus.api.v1.hateoas;

import com.processdataquality.praeclarus.api.v1.Controller;
import com.processdataquality.praeclarus.ui.repo.StoredWorkflow;
import org.jetbrains.annotations.NotNull;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * @author Michael Adams
 * @date 20/10/2022
 */
@Component
public class WorkflowModelAssembler
        implements RepresentationModelAssembler<StoredWorkflow, EntityModel<StoredWorkflow>> {


    @Override
    public @NotNull EntityModel<StoredWorkflow> toModel(@NotNull StoredWorkflow workflow) {
        return EntityModel.of(workflow,
                linkTo(methodOn(Controller.class).findAuthorisedWorkflow(workflow.getId())).withSelfRel(),
                linkTo(methodOn(Controller.class).findAllAuthorisedWorkflows()).withRel("workflows"));
    }

//    @Override
//    public CollectionModel<EntityModel<StoredWorkflow>> toCollectionModel(Iterable entities) {
//        return RepresentationModelAssembler.super.toCollectionModel(entities);
//    }
}
