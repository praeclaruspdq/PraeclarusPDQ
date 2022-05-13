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
import com.processdataquality.praeclarus.node.Node;
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

    private static AuthenticationEventRepository authenticationEventRepository;
    private static ConnectorEventRepository connectorEventRepository;
    private static NodeChangeEventRepository nodeChangeEventRepository;
    private static NodeEventRepository nodeEventRepository;
    private static NodeRollbackEventRepository nodeRollbackEventRepository;
    private static NodeRunEventRepository nodeRunEventRepository;
    private static WorkflowCreationEventRepository workflowCreationEventRepository;
    private static WorkflowIOEventRepository workflowIOEventRepository;
    private static WorkflowRenameEventRepository workflowRenameEventRepository;

    
    // Inject repositories
    public EventLogger(AuthenticationEventRepository authRepo,
                       ConnectorEventRepository connRepo,
                       NodeChangeEventRepository ncRepo,
                       NodeEventRepository nRepo,
                       NodeRollbackEventRepository nrbRepo,
                       NodeRunEventRepository nrRepo,
                       WorkflowCreationEventRepository wcRepo,
                       WorkflowIOEventRepository wioRepo,
                       WorkflowRenameEventRepository wrRepo) {
        authenticationEventRepository = authRepo;
        connectorEventRepository = connRepo;
        nodeChangeEventRepository = ncRepo;
        nodeEventRepository = nRepo;
        nodeRollbackEventRepository = nrbRepo;
        nodeRunEventRepository = nrRepo;
        workflowCreationEventRepository = wcRepo;
        workflowIOEventRepository = wioRepo;
        workflowRenameEventRepository = wrRepo;
    }


    public static void addEventListener(LogEventListener lel) {
        listeners.add(lel);
    }


    public static boolean removeEventListener(LogEventListener lel) {
        return listeners.remove(lel);
    }


    public static void logonSuccessEvent(String user) {
        authenticationEvent(user, LogConstant.LOGON_SUCCESS, null);
    }


    public static void logonFailEvent(String user, String reason) {
        authenticationEvent(user, LogConstant.LOGON_FAIL, reason);
    }


    public static void logOffEvent(String user) {
        authenticationEvent(user, LogConstant.LOGOFF, null);
    }

    
    public static void authenticationEvent(String user, LogConstant constant, String reason) {
        AuthenticationEvent event = new AuthenticationEvent(user, constant, reason);
        save(authenticationEventRepository, event);
    }


    public static void addConnectorEvent(String user, String source, String target) {
        connectorEvent(user, LogConstant.CONNECTOR_ADDED, source, target);
    }


    public static void removeConnectorEvent(String user, String source, String target) {
        connectorEvent(user, LogConstant.CONNECTOR_REMOVED, source, target);
    }


    public static void connectorEvent(String user, LogConstant constant, String source, String target) {
        ConnectorEvent event = new ConnectorEvent(user, constant, source, target);
        save(connectorEventRepository, event);
    }


    public static void optionChangeEvent(String user, Node node, String option,
                                         String oldValue, String newValue) {
        OptionChangeEvent event = new OptionChangeEvent(user, node, option,
                oldValue, newValue);
        save(nodeChangeEventRepository, event);
    }



    public static void nodeAddedEvent(String user, Node node) {
        nodeEvent(user, LogConstant.NODE_ADDED, node);
    }


    public static void nodeRemovedEvent(String user, Node node) {
        nodeEvent(user, LogConstant.NODE_REMOVED, node);
    }


    public static void nodeEvent(String user, LogConstant constant, Node node) {
        NodeEvent event = new NodeEvent(user, constant, node);
        save(nodeEventRepository, event);
    }


    public static void nodeRollbackEvent(String user, Node node) {
        NodeRollbackEvent event = new NodeRollbackEvent(user, node);
        save(nodeRollbackEventRepository, event);
    }


    public static void nodeRunEvent(String user, Node node, String outcome) {
        NodeRunEvent event = new NodeRunEvent(user, node, outcome);
        save(nodeRunEventRepository, event);
    }


    public static void workflowCreationEvent(String user, String workflowId) {
        WorkflowCreationEvent event = new WorkflowCreationEvent(user, workflowId);
        save(workflowCreationEventRepository, event);
    }


    public static void workflowLoadEvent(String user, String fileName) {
        workflowIOEvent(user, LogConstant.WORKFLOW_UPLOADED, fileName);
    }


    public static void workflowSaveEvent(String user, String fileName) {
        workflowIOEvent(user, LogConstant.WORKFLOW_DOWNLOADED, fileName);
    }


    public static void workflowIOEvent(String user, LogConstant constant, String fileName) {
        WorkflowIOEvent event = new WorkflowIOEvent(user, constant, fileName);
        save(workflowIOEventRepository, event);
    }


    public static void workflowRenameEvent(String user, String oldName, String newName) {
        WorkflowRenameEvent event = new WorkflowRenameEvent(user, oldName, newName);
        save(workflowRenameEventRepository, event);
    }

    
    private static <T extends AbstractLogEvent> void save(CrudRepository<T, Long> repo, T event) {
        repo.save(event);
        announce(event);
    }


    private static void announce(AbstractLogEvent event) {
        listeners.forEach(l -> l.eventLogged(event));
    }
}
