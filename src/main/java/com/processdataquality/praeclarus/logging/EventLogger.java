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

/**
 * @author Michael Adams
 * @date 1/12/21
 */
@Component
public class EventLogger {

    public static final DateTimeFormatter dtFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");


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


    public String logonSuccessEvent(String user) {
        return authenticationEvent(user, LogConstant.LOGON_SUCCESS, null);
    }


    public String logonFailEvent(String user, String reason) {
        return authenticationEvent(user, LogConstant.LOGON_FAIL, reason);
    }


    public String logoffEvent(String user) {
        return authenticationEvent(user, LogConstant.LOGOFF, null);
    }

    
    public String authenticationEvent(String user, LogConstant constant, String reason) {
        AuthenticationEvent event = new AuthenticationEvent(user, constant, reason);
        return save(authenticationEventRepository, event);
    }


    public String addConnectorEvent(String user, String source, String target) {
        return connectorEvent(user, LogConstant.CONNECTOR_ADDED, source, target);
    }


    public String removeConnectorEvent(String user, String source, String target) {
        return connectorEvent(user, LogConstant.CONNECTOR_REMOVED, source, target);
    }


    public String connectorEvent(String user, LogConstant constant, String source, String target) {
        ConnectorEvent event = new ConnectorEvent(user, constant, source, target);
        return save(connectorEventRepository, event);
    }


    public String nodeChangeEvent(String user, Node node, String option, String oldValue, String newValue) {
        NodeChangeEvent event = new NodeChangeEvent(user, node, option, oldValue, newValue);
        return save(nodeChangeEventRepository, event);
    }


    public String nodeAddedEvent(String user, Node node) {
        return nodeEvent(user, LogConstant.NODE_ADDED, node);
    }


    public String nodeRemovedEvent(String user, Node node) {
        return nodeEvent(user, LogConstant.NODE_REMOVED, node);
    }


    public String nodeEvent(String user, LogConstant constant, Node node) {
        NodeEvent event = new NodeEvent(user, constant, node);
        return save(nodeEventRepository, event);
    }


    public String nodeRollbackEvent(String user, Node node) {
        NodeRollbackEvent event = new NodeRollbackEvent(user, node);
        return save(nodeRollbackEventRepository, event);
    }


    public String nodeRunEvent(String user, Node node, String outcome) {
        NodeRunEvent event = new NodeRunEvent(user, node, outcome);
        return save(nodeRunEventRepository, event);
    }


    public static String workflowCreationEvent(String user, String workflowId) {
        WorkflowCreationEvent event = new WorkflowCreationEvent(user, workflowId);
        return save(workflowCreationEventRepository, event);
    }


    public static String workflowLoadEvent(String user, String fileName) {
        return workflowIOEvent(user, LogConstant.WORKFLOW_LOADED, fileName);
    }


    public static String workflowSaveEvent(String user, String fileName) {
        return workflowIOEvent(user, LogConstant.WORKFLOW_SAVED, fileName);
    }


    public static String workflowIOEvent(String user, LogConstant constant, String fileName) {
        WorkflowIOEvent event = new WorkflowIOEvent(user, constant, fileName);
        return save(workflowIOEventRepository, event);
    }


    public static String workflowRenameEvent(String user, String oldName, String newName) {
        WorkflowRenameEvent event = new WorkflowRenameEvent(user, oldName, newName);
        return save(workflowRenameEventRepository, event);
    }

    
    private static <T> String save(CrudRepository<T, Long> repo, T event) {
        repo.save(event);
        return event.toString();
    }
}
