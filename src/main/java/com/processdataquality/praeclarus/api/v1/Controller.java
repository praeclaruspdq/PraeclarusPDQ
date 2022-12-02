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

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.WriterConfig;
import com.processdataquality.praeclarus.exception.WorkflowNotFoundException;
import com.processdataquality.praeclarus.graph.Graph;
import com.processdataquality.praeclarus.graph.GraphRunner;
import com.processdataquality.praeclarus.node.Node;
import com.processdataquality.praeclarus.pattern.PatternGroup;
import com.processdataquality.praeclarus.plugin.PluginService;
import com.processdataquality.praeclarus.repo.graph.GraphStore;
import com.processdataquality.praeclarus.ui.repo.StoredWorkflow;
import com.processdataquality.praeclarus.ui.repo.WorkflowStore;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipFile;

/**
 * @author Michael Adams
 * @date 17/10/2022
 */
@RestController
@RequestMapping("api/v1")
public class Controller {

//    private final WorkflowModelAssembler _assembler;
//
//    Controller(WorkflowModelAssembler assembler) {
//        _assembler = assembler;
//    }

    @GetMapping("/workflows")
    public String findAllAuthorisedWorkflows() {
        String owner = getUserName();
        List<StoredWorkflow> workflows = WorkflowStore.findPrivateOrPublic(owner);
        return summariseWorkflows(workflows);
    }


    @GetMapping("/workflows/{id}")
    public StoredWorkflow findAuthorisedWorkflow(@PathVariable String id) {
        StoredWorkflow workflow = WorkflowStore.findById(id);
        if (workflow != null && (workflow.isShared() || workflow.hasOwner(getUserName()))) {
            return workflow;
        }
        throw new WorkflowNotFoundException(id);
    }


    @PostMapping("/workflows/{id}/run")
    public String runWorkflow(@RequestBody String logFile, @RequestBody ZipFile zipFile,
                              @PathVariable String id) {
         StoredWorkflow workflow = findAuthorisedWorkflow(id);
        Optional<Graph> optional = GraphStore.get(id);
        if (optional.isPresent()) {
            Graph graph = optional.get();
            graph.refreshOptions();
            Set<Node> heads = graph.getHeads();
            heads.forEach(head -> {
                // if reader: (AbsDataReader.setsource()
                // setInputs((Table) logfile
            //    head.getPlugin().getAuxiliaryDatasets().put(unzipped zip file)
            });
            GraphRunner runner = new GraphRunner(graph);
            ///runner.run(heads.iterator().next());
            // getTails, getoutputs,
        }
        // return them
        return "";
    }


    @GetMapping("/readers")
    String findAllReaders() {
        JsonArray array = new PluginJsonizer().jsonize(PluginService.readers());
        return summarisePlugins(array, "readers");
    }


    @GetMapping("/writers")
    String findAllWriters() {
        JsonArray array = new PluginJsonizer().jsonize(PluginService.writers());
        return summarisePlugins(array, "writers");
    }


    @GetMapping("/actions")
    String findAllActions() {
        JsonArray array = new PluginJsonizer().jsonize(PluginService.actions());
        return summarisePlugins(array, "actions");
    }


    @GetMapping("/patterns")
    String findAllPatterns() {
        JsonArray array = new PluginJsonizer().jsonize(PluginService.patterns());
        return summarisePlugins(array, "patterns");
    }


    @GetMapping("/patterns/groups/{group}")
    String findPatternsInGroup(@PathVariable PatternGroup group) {
        JsonArray array = new PluginJsonizer().jsonize(PluginService.patterns(), group);
        return summarisePlugins(array, group.name() + "_patterns");
    }

    

    private String getUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }
        return "anonUser";
    }


    private String summariseWorkflows(List<StoredWorkflow> workflows) {
        JsonArray array = new JsonArray();
        workflows.forEach(w -> array.add(w.toSummaryJson()));
        return array.toString();
    }


    private String summarisePlugins(JsonArray array, String prefix) {
            JsonObject object = new JsonObject();
            object.add(prefix, array);
            return object.toString(WriterConfig.PRETTY_PRINT);
    }

}
