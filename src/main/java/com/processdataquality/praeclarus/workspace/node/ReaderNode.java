package com.processdataquality.praeclarus.workspace.node;

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.reader.DataReader;
import tech.tablesaw.api.Table;

import java.io.IOException;

/**
 * @author Michael Adams
 * @date 12/5/21
 */
public class ReaderNode extends Node {

    public ReaderNode(PDQPlugin plugin) { 
        super(plugin);
        setAllowedInputs(0);
        setAllowedOutputs(1);
    }

    @Override
    public Table run() {
        try {
            Table t = ((DataReader) getPlugin()).read();
            setOutput(t);
            return t;
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
            return null;
        }
    }
}
