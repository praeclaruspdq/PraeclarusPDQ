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

package com.processdataquality.praeclarus.repo.graph;

import com.processdataquality.praeclarus.graph.Graph;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Michael Adams
 * @date 27/4/2022
 */
@Component
public class GraphStore {

    private static GraphRepository repository;

    public GraphStore(GraphRepository repo) {
        repository = repo;
    }


    public static Optional<Graph> get(String id) {
        return repository.findById(id);
    }


    public static List<Graph> getAll() {
        List<Graph> list = new ArrayList<>();
        repository.findAll().forEach(list::add);
        return list;
    }


    public static List<Graph> getAllPublic() {
        return repository.findBySharedTrue();
    }


    public static List<Graph> getAllByOwner(String owner) {
        return repository.findByOwner(owner);
    }


    public static List<Graph> getAllByOwnerNotPublic(String owner) {
        return repository.findByOwnerAndSharedFalse(owner);
    }
 

    public static void put(Graph graph) {
        repository.save(graph);
    }


    public static void addIfNew(Graph graph) {
        if (! repository.existsById(graph.getId())) {
            put(graph);
        }
    }
    
}
