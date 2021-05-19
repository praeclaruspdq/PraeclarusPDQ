package com.processdataquality.praeclarus.plugin;

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


    public static PluginFactory<DataReader> readers() { return READER_FACTORY; }

    public static PluginFactory<DataWriter> writers() { return WRITER_FACTORY; }

    public static PluginFactory<ImperfectionPattern> patterns() { return PATTERN_FACTORY; }

    
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
        return null;
    }
}
