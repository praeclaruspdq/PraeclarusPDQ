package com.processdataquality.praeclarus.workspace.node;

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.writer.DataWriter;
import tech.tablesaw.api.Table;

import java.io.IOException;

/**
 * @author Michael Adams
 * @date 12/5/21
 */
public class WriterNode extends Node {

    public WriterNode(PDQPlugin plugin) {
        super(plugin);
        setAllowedInputs(1);
        setAllowedOutputs(0);
    }

    @Override
    public Table run() {
        try {
            Table t = getInput();
            ((DataWriter) getPlugin()).write(t);
            return t;
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
            return null;
        }
    }
}
