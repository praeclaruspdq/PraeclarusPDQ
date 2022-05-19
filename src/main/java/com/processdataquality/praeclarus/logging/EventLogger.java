/*
 * Copyright (c) 2021 Queensland University of Technology
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

package com.processdataquality.praeclarus.logging;

import com.processdataquality.praeclarus.logging.entity.*;
import com.processdataquality.praeclarus.logging.repository.*;
import com.processdataquality.praeclarus.graph.Graph;
import com.processdataquality.praeclarus.node.Node;
import com.processdataquality.praeclarus.option.Option;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 1/12/21
 */
@Component
public class EventLogger {

    public static final DateTimeFormatter dtFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static final Set<LogEventListener> listeners = new HashSet<>();
    private static boolean capturing = true;

    private static AuthenticationEventRepository authenticationEventRepository;
    private static ConnectorEventRepository connectorEventRepository;
    private static OptionChangeEventRepository optionChangeEventRepository;
    private static NodeEventRepository nodeEventRepository;
    private static NodeExecutionEventRepository nodeExecutionEventRepository;
    private static GraphCreatedEventRepository graphCreatedEventRepository;
    private static GraphIOEventRepository graphIOEventRepository;

    
    // Inject repositories
    public EventLogger(AuthenticationEventRepository authRepo,
                       ConnectorEventRepository connRepo,
                       OptionChangeEventRepository ncRepo,
                       NodeEventRepository nRepo,
                       NodeExecutionEventRepository nrRepo,
                       GraphCreatedEventRepository wcRepo,
                       GraphIOEventRepository wioRepo) {
        authenticationEventRepository = authRepo;
        connectorEventRepository = connRepo;
        optionChangeEventRepository = ncRepo;
        nodeEventRepository = nRepo;
        nodeExecutionEventRepository = nrRepo;
        graphCreatedEventRepository = wcRepo;
        graphIOEventRepository = wioRepo;
    }


    // event capture and announcing can be turned off temporarily while graph loading etc.
    public static void ignoreEvents() { capturing = false; }

    public static void captureEvents() { capturing = true; }


    public static void addEventListener(LogEventListener lel) {
        listeners.add(lel);
    }


    public static boolean removeEventListener(LogEventListener lel) {
        return listeners.remove(lel);
    }


    public static void logonSuccessEvent(String user) {
        authenticationEvent(user, EventType.LOGON_SUCCESS, null);
    }


    public static void logonFailEvent(String user, String reason) {
        authenticationEvent(user, EventType.LOGON_FAIL, reason);
    }


    public static void logOffEvent(String user) {
        authenticationEvent(user, EventType.LOGOFF, null);
    }

    
    public static void authenticationEvent(String user, EventType constant, String reason) {
        AuthenticationEvent event = new AuthenticationEvent(user, constant, reason);
        save(authenticationEventRepository, event);
    }


    public static void addConnectorEvent(Graph graph, String user, Node source, Node target) {
        connectorEvent(graph, user, EventType.CONNECTOR_ADDED, source, target);
    }


    public static void removeConnectorEvent(Graph graph, String user, Node source, Node target) {
        connectorEvent(graph, user, EventType.CONNECTOR_REMOVED, source, target);
    }


    public static void connectorEvent(Graph graph, String user, EventType constant,
                                      Node source, Node target) {
        ConnectorEvent event = new ConnectorEvent(graph, user, constant, source, target);
        save(connectorEventRepository, event);
    }


    public static void optionChangeEvent(Graph graph, String user, Option option) {
        optionChangeEvent(graph.getId(), graph.getName(), user, option);
    }


    public static void optionChangeEvent(Node node, String user, Option option) {
        optionChangeEvent(node.getID(), node.getLabel(), user, option);
    }


    public static void optionChangeEvent(String compId, String compName, String user,
                                         Option option) {
        OptionChangeEvent event = new OptionChangeEvent(compId, compName, user,
                option);
        save(optionChangeEventRepository, event);
    }


    public static void nodeAddedEvent(Graph graph, Node node, String user) {
        nodeEvent(graph, node, EventType.NODE_ADDED, user);
    }


    public static void nodeRemovedEvent(Graph graph, Node node, String user) {
        nodeEvent(graph, node, EventType.NODE_REMOVED, user);
    }


    public static void nodeEvent(Graph graph, Node node, EventType constant, String user) {
        NodeEvent event = new NodeEvent(graph, node, constant, user);
        save(nodeEventRepository, event);
    }


    public static void nodeCompletedEvent(Graph graph, Node node, String user, String outcome) {
        nodeExecutionEvent(graph, node, EventType.NODE_COMPLETED, user, outcome);
    }


    public static void nodePausedEvent(Graph graph, Node node, String user, String outcome) {
        nodeExecutionEvent(graph, node, EventType.NODE_PAUSED, user, outcome);
    }


    public static void nodeRollbackEvent(Graph graph, Node node, String user, String outcome) {
        nodeExecutionEvent(graph, node, EventType.NODE_ROLLBACK, user, outcome);
    }

    
    public static void nodeExecutionEvent(Graph graph, Node node, EventType eventType,
                                          String user, String outcome) {
        NodeExecutionEvent event = new NodeExecutionEvent(graph, node, eventType, user, outcome);
        save(nodeExecutionEventRepository, event);
    }


    public static void graphCreatedEvent(Graph graph, String user) {
        GraphCreatedEvent event = new GraphCreatedEvent(graph, user);
        save(graphCreatedEventRepository, event);
    }


    public static void graphUploadEvent(Graph graph, String user) {
        graphIOEvent(graph, user, EventType.GRAPH_UPLOADED);
    }


    public static void graphDownloadEvent(Graph graph, String user) {
        graphIOEvent(graph, user, EventType.GRAPH_DOWNLOADED);
    }


    public static void graphStoreEvent(Graph graph, String user) {
        graphIOEvent(graph, user, EventType.GRAPH_STORED);
    }


    public static void graphLoadEvent(Graph graph, String user) {
        graphIOEvent(graph, user, EventType.GRAPH_LOADED);
    }


    public static void graphDiscardedEvent(String id, String name, String user) {
        GraphIOEvent event = new GraphIOEvent(id, name, user, EventType.GRAPH_DISCARDED);
        save(graphIOEventRepository, event);
    }


    public static void graphIOEvent(Graph graph, String user, EventType constant) {
        GraphIOEvent event = new GraphIOEvent(graph, user, constant);
        save(graphIOEventRepository, event);
    }

    
    private static <T extends AbstractLogEvent> void save(CrudRepository<T, Long> repo, T event) {
        if (capturing) {
            repo.save(event);
            announce(event);
        }
    }


    private static void announce(AbstractLogEvent event) {
        listeners.forEach(l -> l.eventLogged(event));
    }
}
