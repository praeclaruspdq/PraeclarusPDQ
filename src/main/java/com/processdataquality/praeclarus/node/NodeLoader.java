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

package com.processdataquality.praeclarus.node;

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Michael Adams
 * @date 12/5/21
 */
public class NodeLoader {
    
    public Node fromJson(JSONObject json) throws JSONException, IOException {
        Node node = null;
        PDQPlugin plugin = newPluginInstance(json.getString("plugin"));
        if (plugin != null) {
            addOptions(plugin, json.getJSONObject("options"));
            String nodeID = json.getString("id");
            String label = json.getString("label");
            String commitID = json.optString("commitID");
            String tableID = json.optString("tableID");
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
    private PDQPlugin newPluginInstance(String fqClassName) {
        try {
            Class<PDQPlugin> clazz = (Class<PDQPlugin>) Class.forName(fqClassName);
            return clazz.getDeclaredConstructor().newInstance();
        }
        catch (Throwable e) {
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    private void addOptions(PDQPlugin plugin, JSONObject jsonOptions) throws JSONException {
        if (jsonOptions != null) {
            Iterator<String> keys = jsonOptions.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                plugin.getOptions().add(key, jsonOptions.get(key));
            }
        }
    }


}
