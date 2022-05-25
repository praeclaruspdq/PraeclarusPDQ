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

package com.processdataquality.praeclarus.plugin;


import com.processdataquality.praeclarus.annotations.Pattern;
import com.processdataquality.praeclarus.annotations.Plugin;
import com.processdataquality.praeclarus.config.PluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author Michael Adams
 * @date 14/4/21
 */
public class PluginFactory<T> {

    private static final Logger LOG = LoggerFactory.getLogger(PluginFactory.class);

    private final Map<String, Class<T>> _classMap;


    public PluginFactory(Class<T> type) {
        _classMap = buildMap(type);
    }


    public List<String> getPluginNames() {
        return new ArrayList<>(_classMap.keySet());
    }


    public List<Plugin> getMetaDataList() {
        List<Plugin> list = new ArrayList<>();
        for (Class<?> clazz : _classMap.values()) {
            Plugin metaData = clazz.getAnnotation(Plugin.class);
            if (metaData != null) {
                list.add(metaData);
            }
        }
        return list;
    }


    public Plugin getPluginAnnotation(String className) {
        Class<?> clazz = _classMap.get(className);
        if (clazz != null) {
            return clazz.getAnnotation(Plugin.class);
        }
        return null;
    }


    public List<Pattern> getPatternAnnotations(String className) {
        Class<?> clazz = _classMap.get(className);
        if (clazz != null) {
            Pattern[] annotations = clazz.getAnnotationsByType(Pattern.class);
            return annotations.length > 0 ? Arrays.asList(annotations) :
                    new ArrayList<>();       // deliberately not Collections.emptyList()
        }
        return null;
    }


    // ensure the plugin class is known to the framework
    public T newInstance(String className) throws NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<T> clazz = _classMap.get(className);
        if (clazz == null) {
            throw new InstantiationException("Unable to instantiate class. No known class: " + className);
        }
        return clazz.getDeclaredConstructor().newInstance();
    }


    private Map<String, Class<T>> buildMap(Class<T> type) {
        try {
            PluginLoader loader = new PluginLoader(new PluginConfig().getPaths());
            return loader.loadAsMap(type);
        }
        catch (IOException e) {
            LOG.error("Failed to load plugins. ", e);
            return Collections.emptyMap();
        }
    }
    
}
