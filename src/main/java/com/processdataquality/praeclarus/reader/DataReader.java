package com.processdataquality.praeclarus.reader;


import com.processdataquality.praeclarus.plugin.PDQPlugin;
import tech.tablesaw.api.Table;

import java.io.IOException;

/**
 * @author Michael Adams
 * @date 29/3/21
 */
public interface DataReader extends PDQPlugin {

    /**
     * Fills a Table object with data from a source
     * @return the filled Table object
     * @throws IOException if anything goes wrong
     */
    Table read() throws IOException;
    
}
