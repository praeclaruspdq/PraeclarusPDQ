package com.processdataquality.praeclarus.pattern;

import com.processdataquality.praeclarus.annotations.PluginMetaData;
import com.processdataquality.praeclarus.plugin.Options;
import org.apache.commons.text.similarity.LevenshteinDistance;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

/**
 * @author Michael Adams
 * @date 11/5/21
 */
@PluginMetaData(
        name = "Levenshtein Distance",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Calculates activity label similarity using Levenshtein Distance",
        group = PatternGroup.DISTORTED_LABEL
)
public class DistortedLabelLevenshtein extends AbstractDistortedLabel {

    private final LevenshteinDistance levenshtein =
            new LevenshteinDistance(getOptions().getIntValue("Threshold"));

    public DistortedLabelLevenshtein() { }


    @Override
    protected void detect(StringColumn column, String s1, String s2) {
        int threshold = getOptions().getIntValue("Threshold");
        int distance = levenshtein.apply(s1, s2);
        if (distance > 0 && distance <= threshold) {
            addResult(column, s1, s2);
        }
    }

    @Override
    public Options getOptions() {
        Options options = super.getOptions();
        if (!options.containsKey("Threshold")) {
            options.put("Threshold", 2);
        }
        return options;
    }



    public static void main(String[] args) {
        Table master = Table.create("Master").addColumns(StringColumn.create("Label1"));
        master.column(0).appendCell("house");
        master.column(0).appendCell("hose");
        master.column(0).appendCell("house");
        master.column(0).appendCell("house");
        master.column(0).appendCell("roust");
        master.column(0).appendCell("rusty");
        master.column(0).appendCell("nomatch");
        DistortedLabelLevenshtein lev = new DistortedLabelLevenshtein();
        Options options = lev.getOptions();
        options.put("Column Name", "Label1");
        lev.setOptions(options);
        Table result = lev.detect(master);
        System.out.println(result.toString());

        Table changes = Table.create("Repair").addColumns(
                StringColumn.create("Replace"),StringColumn.create("Replacement")
        );
        changes.column(0).appendCell("hose");
        changes.column(1).appendCell("house");
        changes.column(0).appendCell("roust");
        changes.column(1).appendCell("house");
        Table newMaster = lev.repair(master, changes);
        System.out.println(newMaster.toString());
    }
}
