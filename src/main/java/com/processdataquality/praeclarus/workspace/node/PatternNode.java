package com.processdataquality.praeclarus.workspace.node;

import com.processdataquality.praeclarus.pattern.ImperfectionPattern;
import com.processdataquality.praeclarus.plugin.PDQPlugin;
import tech.tablesaw.api.Table;

/**
 * @author Michael Adams
 * @date 12/5/21
 */
public class PatternNode extends Node {

    public PatternNode(PDQPlugin plugin) {
        super(plugin);
        setAllowedInputs(2);
        setAllowedOutputs(1);
    }

    @Override
    public Table run() {
        ImperfectionPattern imperfectionPattern = (ImperfectionPattern) getPlugin();
        Table master = getInput();
        if (getInputCount() == 1) {
            Table changes = imperfectionPattern.detect(master);
            if (imperfectionPattern.canRepair()) {
                addInput(changes);
                setPause(true);
            }
            return changes;
        }
        else if (getInputCount() == 2) {
            Table newMaster = imperfectionPattern.repair(master, getInput(1));
            setPause(false);
            return newMaster;
        }
        throw new IllegalArgumentException("Incorrect number of inputs");
    }
}
