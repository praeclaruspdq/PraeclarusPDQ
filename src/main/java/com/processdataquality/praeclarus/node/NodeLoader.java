/*
 * Copyright (c) 2021-2022 Queensland University of Technology
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

package com.processdataquality.praeclarus.node;

import com.eclipsesource.json.JsonObject;
import com.processdataquality.praeclarus.plugin.AbstractPlugin;
import com.processdataquality.praeclarus.plugin.PluginFactory;
import com.processdataquality.praeclarus.plugin.PluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Rehydrates a Node from its JSON representation
 * @author Michael Adams
 * @date 12/5/21
 */
public class NodeLoader {

    private static final Logger LOG = LoggerFactory.getLogger(NodeLoader.class);

    
    public Node fromJson(JsonObject json) throws IOException {
        Node node = null;
        AbstractPlugin plugin = newPluginInstance(json.getString("plugin", ""));
        if (plugin != null) {
            addOptions(plugin, json.get("options").asObject());
            String nodeID = json.getString("id", "");
            String label = json.getString("label", "");
            String commitID = json.getString("commitID", "");
            String tableID = json.getString("tableID", "");
            node = NodeFactory.create(plugin, nodeID);
            node.setLabel(label);
            if (!commitID.isEmpty()) {
                node.setCommitID(commitID);
                if (!tableID.isEmpty()) {
                    node.loadOutput(tableID);           // loads from repo
                }
            }
        }
        return node;
    }


    @SuppressWarnings("unchecked")
    private AbstractPlugin newPluginInstance(String fqClassName) {
        try {
            Class<?> c = Class.forName(fqClassName);
            if (AbstractPlugin.class.isAssignableFrom(c)) {
                Class<? extends AbstractPlugin> clazz = (Class<? extends AbstractPlugin>) c;
                PluginFactory<? extends AbstractPlugin> pluginFactory = PluginService.factory(clazz);
                if (pluginFactory != null) {
                    return pluginFactory.newInstance(fqClassName);
                }
                else throw new InstantiationException("Plugin class is unregistered.");
            }
            else throw new InstantiationException("Plugin class is not a valid PDQ plugin.");
        }
        catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException |
               InstantiationException | NoSuchMethodException e) {
            LOG.error("Failed to load plugin: " + fqClassName, e);
            return null;
        }
    }


    private void addOptions(AbstractPlugin plugin, JsonObject jsonOptions) {
        if (jsonOptions != null) {
            for (String key : jsonOptions.names()) {
                plugin.getOptions().add(key, jsonOptions.get(key));
            }
        }
    }

}
