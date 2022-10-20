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

import com.processdataquality.praeclarus.annotations.Pattern;
import com.processdataquality.praeclarus.annotations.Plugin;
import com.processdataquality.praeclarus.pattern.PatternGroup;
import com.processdataquality.praeclarus.plugin.PluginFactory;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.List;

/**
 * @author Michael Adams
 * @date 19/10/2022
 */
public class PluginJsonizer {


    public JSONArray jsonize(PluginFactory<?> factory) throws JSONException {
        JSONArray array = new JSONArray();
        for (String className : factory.getPluginClassNames()) {
            Plugin plugin = factory.getPluginAnnotation(className);
            List<Pattern> patterns = factory.getPatternAnnotations(className);
            array.put(jsonizePlugin(className, plugin, patterns));
        }
        return array;
    }


    // filters patterns on a specified group
    public JSONArray jsonize(PluginFactory<?> factory, PatternGroup group) throws JSONException {
        JSONArray array = new JSONArray();
        for (String className : factory.getPluginClassNames()) {
            List<Pattern> patterns = factory.getPatternAnnotations(className);
            for (Pattern pattern : patterns) {
                 if (pattern.group().equals(group)) {
                     Plugin plugin = factory.getPluginAnnotation(className);
                     array.put(jsonizePlugin(className, plugin, patterns));
                }
            }
        }
        return array;
    }


    public JSONObject jsonizePlugin(String className, Plugin plugin,
                                    List<Pattern> patternGroups) throws JSONException {
        JSONObject pluginObject = new JSONObject();
        pluginObject.put("classname", className);
        pluginObject.put("name", plugin.name());
        pluginObject.put("author", plugin.author());
        pluginObject.put("synopsis", plugin.synopsis());
        pluginObject.put("description", plugin.description());
        pluginObject.put("version", plugin.version());
        if (! patternGroups.isEmpty()) {
            JSONArray array = new JSONArray();
            patternGroups.forEach(pattern -> array.put(pattern.group().name()));
            pluginObject.put("pattern_groups", array);
        }
        return pluginObject;
    }
}
