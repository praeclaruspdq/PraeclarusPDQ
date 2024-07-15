package com.processdataquality.praeclarus.pattern;

import com.processdataquality.praeclarus.annotation.Pattern;
import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionException;
import com.processdataquality.praeclarus.option.ColumnNameListOption;
import com.processdataquality.praeclarus.option.Options;
import com.processdataquality.praeclarus.support.swiftmend.*;
import tech.tablesaw.api.*;
import tech.tablesaw.columns.Column;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * @author Savandi Kalukapuge
 * @date 12/7/24
 */
@Plugin(name = "SwiftMend DetectRepair",
        author = "Savandi Kalukapuge",
        version = "1.0",
        synopsis = "Detects and repairs synonymous, distorted, polluted labels in the event log emitted as a stream, using control-flow contextual information.")
@Pattern(group = PatternGroup.SYNONYMOUS_LABELS)
@Pattern(group = PatternGroup.POLLUTED_LABEL)
@Pattern(group = PatternGroup.DISTORTED_LABEL)
public class SwiftMendDetectRepair extends AbstractImperfectLabel {
    private ControlFlowSimilarity activityDetectRepair;
    private Table originalTable;

    public SwiftMendDetectRepair() {
        super();
        addDefaultOptions();
    }

    protected void addDefaultOptions() {
        Options options = super.getOptions();
        options.addDefault(new ColumnNameListOption("Case ID Column"));
        options.addDefault(new ColumnNameListOption("Activity Column"));
        options.addDefault(new ColumnNameListOption("Timestamp Column"));
        options.addDefault("Detection Window Size", 250.0);
        options.addDefault("Reference Window", 0);
        options.addDefault("Direct Causality Upper Threshold", 0.8);
        options.addDefault("Direct Causality Lower Threshold", 0.7);
        options.addDefault("Direct Parallelism Upper Threshold", 2.0);
        options.addDefault("Direct Parallelism Lower Threshold", 3.0);
        options.addDefault("Control Flow Similarity Threshold", 0.7);
    }

    @Override
    protected void detect(StringColumn column, String s1, String s2) {
    }

    @Override
    public Table detect(Table table) throws InvalidOptionException {
        this.originalTable = table;  // Save the original table
        Options options = getOptions();
        double windowSize = options.get("Detection Window Size").asDouble();
        int delay = options.get("Reference Window").asInt();
        float controlFlowSimThreshold = (float) options.get("Control Flow Similarity Threshold").asDouble();
        float upperCausalityThreshold = (float) options.get("Direct Causality Upper Threshold").asDouble();
        float upperParallelismThreshold = (float) options.get("Direct Parallelism Upper Threshold").asDouble();
        float lowerCausalityThreshold = (float) options.get("Direct Causality Lower Threshold").asDouble();
        float lowerParallelismThreshold = (float) options.get("Direct Parallelism Lower Threshold").asDouble();

        // Initialize the ActivityDetector with options
        activityDetectRepair = new ControlFlowSimilarity(windowSize, delay, controlFlowSimThreshold, upperCausalityThreshold, upperParallelismThreshold, lowerCausalityThreshold, lowerParallelismThreshold);

        _detected = createResultTable();  // Call the superclass method
        String caseIdColName = getCaseIDColumnName(table);
        String activityColName = getActivityColumnName(table);
        String timestampColName = getTimestampColumnName(table);

        table.stream().forEach(row -> {
            String caseId = readStringValue(row, caseIdColName);
            String activity = readStringValue(row, activityColName);
            String timestamp = readStringValue(row, timestampColName); // Use the DateTimeColumn directly
            String repairedActivity = activityDetectRepair.detect(caseId, activity, timestamp);

            // Create a new row in the _detected table
            Row detectedRow = _detected.appendRow();
            // Copy all values from the original row to the new row in _detected
            copyRowValues(detectedRow, table.columns(), row);

            // Update the activity column in the _detected table with repairedActivity
            detectedRow.setString(activityColName, repairedActivity);

            // Introduce a delay of 2 milliseconds
            try {
                TimeUnit.MILLISECONDS.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });
        _detected = sortByTimestamp(_detected, timestampColName);
        return _detected;
    }

    @Override
    public Table repair(Table master) throws InvalidOptionException {
        return _detected;
    }

    public Table sortByTimestamp(Table table, String timestampColName) throws InvalidOptionException {
        Table sortedTable = null;
        // Check if the timestamp column is already a DateTimeColumn
        Column<?> timestampColumn = table.column(timestampColName);
        DateTimeColumn dateTimeColumn = null;
        if (!(timestampColumn instanceof DateTimeColumn)) {
            dateTimeColumn = DateTimeColumn.create(timestampColName + "_DateTime");
            for (int i = 0; i < timestampColumn.size(); i++) {
                String timestamp = timestampColumn.getString(i);
                LocalDateTime dateTime = activityDetectRepair.textToTime(timestamp);
                dateTimeColumn.append(dateTime);
            }
            // Add the DateTimeColumn to the table
            table.addColumns(dateTimeColumn);
            // Sort the table by the new DateTimeColumn
            sortedTable = table.sortOn(dateTimeColumn.name());
        } else {
            // If it's already a DateTimeColumn, sort directly
            sortedTable = table.sortOn(timestampColName);
        }
        sortedTable.removeColumns(dateTimeColumn.name());
        return sortedTable;
    }

    protected String getCaseIDColumnName(Table table) throws InvalidOptionException {
        String colName = getSelectedColumnNameValue("Case ID Column");
        if (!table.columnNames().contains(colName)) {
            throw new InvalidOptionException("No column named '" + colName + "' in table");
        }
        return colName;
    }

    protected String getActivityColumnName(Table table) throws InvalidOptionException {
        String colName = getSelectedColumnNameValue("Activity Column");
        if (!table.columnNames().contains(colName)) {
            throw new InvalidOptionException("No column named '" + colName + "' in table");
        }
        return colName;
    }

    protected String getTimestampColumnName(Table table) throws InvalidOptionException {
        String colName = getSelectedColumnNameValue("Timestamp Column");
        if (!table.columnNames().contains(colName)) {
            throw new InvalidOptionException("No column named '" + colName + "' in table");
        }
        return colName;
    }

    private void copyRowValues(Row detectedRow, List<Column<?>> columns, Row row) {
        for (Column<?> column : columns) {
            String columnName = column.name();
            try {
                if (column instanceof IntColumn) {
                    if (!row.isMissing(columnName)) {
                        detectedRow.setInt(columnName, row.getInt(columnName));
                    }
                } else if (column instanceof DoubleColumn) {
                    if (!row.isMissing(columnName)) {
                        detectedRow.setDouble(columnName, row.getDouble(columnName));
                    }
                } else if (column instanceof StringColumn || column instanceof TextColumn) {
                    if (!row.isMissing(columnName)) {
                        detectedRow.setString(columnName, row.getString(columnName));
                    }
                } else if (column instanceof FloatColumn) {
                    if (!row.isMissing(columnName)) {
                        detectedRow.setFloat(columnName, row.getFloat(columnName));
                    }
                } else if (column instanceof DateColumn) {
                    if (!row.isMissing(columnName)) {
                        detectedRow.setDate(columnName, row.getDate(columnName));
                    }
                } else if (column instanceof DateTimeColumn) {
                    if (!row.isMissing(columnName)) {
                        detectedRow.setDateTime(columnName, row.getDateTime(columnName));
                    }
                } else if (column instanceof TimeColumn) {
                    if (!row.isMissing(columnName)) {
                        detectedRow.setTime(columnName, row.getTime(columnName));
                    }
                } else if (column instanceof BooleanColumn) {
                    if (!row.isMissing(columnName)) {
                        detectedRow.setBoolean(columnName, row.getBoolean(columnName));
                    }
                } else {
                    throw new IllegalArgumentException("Unsupported column type: " + column.type());
                }
            } catch (Exception e) {
                System.err.println("Error processing column: " + columnName + " with type: " + column.type());
                e.printStackTrace();
            }
        }
    }

    @Override
    protected Table createResultTable() {
        Table result = Table.create("OutputStream");
        for (Column<?> column : originalTable.columns()) {
            result.addColumns(column.emptyCopy());
        }
        return result;
    }

    public LocalDateTime textToTime(String text) {
        return super.textToTime(text);
    }
}