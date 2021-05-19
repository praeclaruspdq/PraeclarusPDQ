package com.processdataquality.praeclarus.pattern;

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import tech.tablesaw.api.Table;

/**
 * @author Michael Adams
 * @date 11/5/21
 */
public interface ImperfectionPattern extends PDQPlugin {

    /**
     * Detect instances of an imperfection pattern found within a table
     * @param table a table containing values to check for the pattern
     * @return a table where each row contains values detected using the pattern
     */
    Table detect(Table table);


    /**
     * Repair instances of an imperfection pattern found within a table
     * @param master the original table containing pattern instances
     * @param changes a table containing the rows and values that describe
     *                the necessary changes to repair the pattern instances found
     * @return        a table of the original data with the repairs done
     */
    Table repair(Table master, Table changes);


    /**
     * Determines whether this particular plugin can repair the data
     * @return true if this plugin can repair the data
     */
    boolean canRepair();
}
