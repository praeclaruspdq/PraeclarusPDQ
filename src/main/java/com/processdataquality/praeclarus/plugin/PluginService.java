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

package com.processdataquality.praeclarus.plugin;

import com.processdataquality.praeclarus.action.AbstractAction;
import com.processdataquality.praeclarus.pattern.AbstractDataPattern;
import com.processdataquality.praeclarus.reader.AbstractDataReader;
import com.processdataquality.praeclarus.writer.AbstractDataWriter;

/**
 * @author Michael Adams
 * @date 29/4/21
 */
public class PluginService {

    private static final PluginFactory<AbstractDataReader> READER_FACTORY =
            new PluginFactory<>(AbstractDataReader.class);
    private static final PluginFactory<AbstractDataWriter> WRITER_FACTORY =
            new PluginFactory<>(AbstractDataWriter.class);
    private static final PluginFactory<AbstractDataPattern> PATTERN_FACTORY =
            new PluginFactory<>(AbstractDataPattern.class);
    private static final PluginFactory<AbstractAction> ACTION_FACTORY =
            new PluginFactory<>(AbstractAction.class);


    public static PluginFactory<AbstractDataReader> readers() { return READER_FACTORY; }

    public static PluginFactory<AbstractDataWriter> writers() { return WRITER_FACTORY; }

    public static PluginFactory<AbstractDataPattern> patterns() { return PATTERN_FACTORY; }

    public static PluginFactory<AbstractAction> actions() { return ACTION_FACTORY; }

    
    public static PluginFactory<? extends AbstractPlugin> factory(Class<? extends AbstractPlugin> clazz) {
        if (AbstractDataReader.class.isAssignableFrom(clazz)) {
            return READER_FACTORY;
        }
        if (AbstractDataWriter.class.isAssignableFrom(clazz)) {
            return WRITER_FACTORY;
        }
        if (AbstractDataPattern.class.isAssignableFrom(clazz)) {
            return PATTERN_FACTORY;
        }
        if (AbstractAction.class.isAssignableFrom(clazz)) {
            return ACTION_FACTORY;
        }
        return null;
    }
}
