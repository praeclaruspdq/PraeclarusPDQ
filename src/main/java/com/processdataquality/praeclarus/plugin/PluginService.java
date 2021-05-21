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

import com.processdataquality.praeclarus.action.Action;
import com.processdataquality.praeclarus.pattern.ImperfectionPattern;
import com.processdataquality.praeclarus.reader.DataReader;
import com.processdataquality.praeclarus.writer.DataWriter;

/**
 * @author Michael Adams
 * @date 29/4/21
 */
public class PluginService {

    private static final PluginFactory<DataReader> READER_FACTORY = new PluginFactory<>(DataReader.class);
    private static final PluginFactory<DataWriter> WRITER_FACTORY = new PluginFactory<>(DataWriter.class);
    private static final PluginFactory<ImperfectionPattern> PATTERN_FACTORY
            = new PluginFactory<>(ImperfectionPattern.class);
    private static final PluginFactory<Action> ACTION_FACTORY = new PluginFactory<>(Action.class);


    public static PluginFactory<DataReader> readers() { return READER_FACTORY; }

    public static PluginFactory<DataWriter> writers() { return WRITER_FACTORY; }

    public static PluginFactory<ImperfectionPattern> patterns() { return PATTERN_FACTORY; }

    public static PluginFactory<Action> actions() { return ACTION_FACTORY; }

    
    public static PluginFactory<? extends PDQPlugin> factory(Class<? extends PDQPlugin> clazz) {
        if (clazz.equals(DataReader.class)) {
            return READER_FACTORY;
        }
        if (clazz.equals(DataWriter.class)) {
            return WRITER_FACTORY;
        }
        if (clazz.equals(ImperfectionPattern.class)) {
            return PATTERN_FACTORY;
        }
        if (clazz.equals(Action.class)) {
            return ACTION_FACTORY;
        }
        return null;
    }
}
