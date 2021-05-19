package com.processdataquality.praeclarus.writer;

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import tech.tablesaw.api.Table;

import java.io.IOException;

/**
 * @author Michael Adams
 * @date 31/3/21
 */
public interface DataWriter extends PDQPlugin {

    /**
     * Writes the data in a Table object to a sink (file, stream, etc)
     * @throws IOException if anything goes wrong
     */
    void write(Table table) throws IOException;

}
