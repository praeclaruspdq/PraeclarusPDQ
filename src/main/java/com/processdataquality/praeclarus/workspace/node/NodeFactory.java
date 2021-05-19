package com.processdataquality.praeclarus.workspace.node;

import com.processdataquality.praeclarus.pattern.ImperfectionPattern;
import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.reader.DataReader;
import com.processdataquality.praeclarus.writer.DataWriter;

/**
 * @author Michael Adams
 * @date 13/5/21
 */
public class NodeFactory {

    public static Node create(PDQPlugin plugin) {
        if (plugin instanceof DataReader) {
            return new ReaderNode(plugin);
        }
        if (plugin instanceof DataWriter) {
            return new WriterNode(plugin);
        }
        if (plugin instanceof ImperfectionPattern) {
            return new PatternNode(plugin);
        }
        return null;
    }
}
