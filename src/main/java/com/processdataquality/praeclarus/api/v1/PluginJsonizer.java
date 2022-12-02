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
import com.processdataquality.praeclarus.annotation.Pattern;
import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.pattern.PatternGroup;
import com.processdataquality.praeclarus.plugin.PluginFactory;

import java.util.List;

/**
 * @author Michael Adams
 * @date 19/10/2022
 */
public class PluginJsonizer {


    public JsonArray jsonize(PluginFactory<?> factory) {
        JsonArray array = new JsonArray();
        for (String className : factory.getPluginClassNames()) {
            Plugin plugin = factory.getPluginAnnotation(className);
            List<Pattern> patterns = factory.getPatternAnnotations(className);
            array.add(jsonizePlugin(className, plugin, patterns));
        }
        return array;
    }


    // filters patterns on a specified group
    public JsonArray jsonize(PluginFactory<?> factory, PatternGroup group) {
        JsonArray array = new JsonArray();
        for (String className : factory.getPluginClassNames()) {
            List<Pattern> patterns = factory.getPatternAnnotations(className);
            for (Pattern pattern : patterns) {
                 if (pattern.group().equals(group)) {
                     Plugin plugin = factory.getPluginAnnotation(className);
                     array.add(jsonizePlugin(className, plugin, patterns));
                }
            }
        }
        return array;
    }


    public JsonObject jsonizePlugin(String className, Plugin plugin,
                                    List<Pattern> patternGroups) {
        JsonObject pluginObject = new JsonObject();
        pluginObject.add("classname", className);
        pluginObject.add("name", plugin.name());
        pluginObject.add("author", plugin.author());
        pluginObject.add("synopsis", plugin.synopsis());
        pluginObject.add("description", plugin.description());
        pluginObject.add("version", plugin.version());
        if (! patternGroups.isEmpty()) {
            JsonArray array = new JsonArray();
            patternGroups.forEach(pattern -> array.add(pattern.group().name()));
            pluginObject.add("pattern_groups", array);
        }
        return pluginObject;
    }
}
