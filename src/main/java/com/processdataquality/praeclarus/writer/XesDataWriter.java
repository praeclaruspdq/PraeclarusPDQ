/*
 * Copyright (c) 2021-2022 Queensland University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.processdataquality.praeclarus.writer;

import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionException;
import com.processdataquality.praeclarus.util.DataCollection;
import org.apache.commons.lang3.math.NumberUtils;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.classification.XEventResourceClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.*;
import org.deckfour.xes.out.XesXmlSerializer;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.columns.booleans.BooleanColumnType;
import tech.tablesaw.columns.datetimes.DateTimeColumnType;
import tech.tablesaw.columns.numbers.DoubleColumnType;
import tech.tablesaw.columns.numbers.LongColumnType;
import tech.tablesaw.columns.strings.StringColumnType;
import tech.tablesaw.io.WriteOptions;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 14/12/21
 */
@Plugin(
        name = "XES Writer",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Writes the log output to an XES file.",
        fileDescriptors = "XES Files;text/xml;.xes"
)
public class XesDataWriter extends AbstractDataWriter {

    public XesDataWriter() {
        super();
        addDefaultOptions();
    }

    @Override
    public void write(Table table, DataCollection auxData) throws IOException {
        XLog log = createXLog(table);
        write(log);
    }


    public void addDefaultOptions() {
        getOptions().addDefault("Case ID column", "case:id");
        getOptions().addDefault("Name column", "concept:name");
        getOptions().addDefault("Time column", "time:timestamp");
        getOptions().addDefault("Lifecycle column", "lifecycle:transition");
        getOptions().addDefault("Instance column", "concept:instance");
        getOptions().addDefault("Resource column", "org:resource");
        getOptions().addDefault("Data column", "data");
    }


    @Override
    protected WriteOptions getWriteOptions() throws IOException {
        return null;
    }


    private XLog createXLog(Table table) throws IOException {
        Map<String, String> colNames = mapColNames();
        checkColumnNamesInTable(table, colNames);
        XFactory xFactory = new XFactoryNaiveImpl();
        XLog xLog = xFactory.createLog();
        XConceptExtension conceptExtension = addExtensions(xLog, table.name());
        addGlobals(xLog);

        XTrace xTrace = null;
        String currentCaseId = null;

        for (int i=0; i<table.rowCount(); i++) {
            Row row = table.row(i);
            String caseId = row.getString("case:id");

            // start a new trace on case id change
            if (currentCaseId == null || !currentCaseId.equals(caseId)) {
                xTrace = xFactory.createTrace();
                conceptExtension.assignName(xTrace, caseId);
                xLog.add(xTrace);
                currentCaseId = caseId;
            }

            XEvent xEvent = xFactory.createEvent();
            xTrace.add(parseRow(xEvent, table.columns(), row));
        }
        return xLog;
    }


    private XEvent parseRow(XEvent xEvent, List<Column<?>> columns, Row row) {
        for (Column<?> column : columns) {
            String colName = column.name();
            if ("case:id".equals(colName)) {
                continue;
            }
            XAttribute attribute = null;
            ColumnType colType = column.type();
            if ("data".equals(colName)) {
                addData(xEvent, getStringValue(row, colName));
                continue;
            }

            if (colType instanceof StringColumnType) {
                attribute = new XAttributeLiteralImpl(colName, getStringValue(row, colName));
            }
            else if (colType instanceof DateTimeColumnType) {
                attribute = new XAttributeTimestampImpl(colName, getTimeValue(row, colName));
            }
            else if (colType instanceof LongColumnType) {
                attribute = new XAttributeDiscreteImpl(colName, getLongValue(row, colName));
            }
            else if (colType instanceof DoubleColumnType) {
                attribute = new XAttributeContinuousImpl(colName, getDoubleValue(row, colName));
            }
            else if (colType instanceof BooleanColumnType) {
                attribute = new XAttributeBooleanImpl(colName,
                        Boolean.TRUE.equals(getBooleanValue(row, colName)));
            }
            if (attribute != null) {
                xEvent.getAttributes().put(colName, attribute);
            }
        }
        return xEvent;
    }


    private XConceptExtension addExtensions(XLog xLog, String tableName) {
        XConceptExtension conceptExtension = XConceptExtension.instance();
        XLifecycleExtension lifeExtension = XLifecycleExtension.instance();
        xLog.getExtensions().add(conceptExtension);
        xLog.getExtensions().add(lifeExtension);
        xLog.getExtensions().add(XTimeExtension.instance());
        xLog.getExtensions().add(XOrganizationalExtension.instance());
        xLog.getClassifiers().add(new XEventNameClassifier());
        xLog.getClassifiers().add(new XEventLifeTransClassifier());
        xLog.getClassifiers().add(new XEventResourceClassifier());
        
        conceptExtension.assignName(xLog, (tableName != null ? tableName : "UNKNOWN"));
        lifeExtension.assignModel(xLog, XLifecycleExtension.VALUE_MODEL_STANDARD);
        return conceptExtension;
    }


    private void addGlobals(XLog xLog) {
        xLog.getGlobalTraceAttributes().add(new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, "UNKNOWN"));
        xLog.getGlobalEventAttributes().add(new XAttributeTimestampImpl(XTimeExtension.KEY_TIMESTAMP, 0));
        xLog.getGlobalEventAttributes().add(new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, "UNKNOWN"));
        xLog.getGlobalEventAttributes().add(new XAttributeLiteralImpl(XLifecycleExtension.KEY_TRANSITION, "UNKNOWN"));
        xLog.getGlobalEventAttributes().add(new XAttributeLiteralImpl(XConceptExtension.KEY_INSTANCE, "UNKNOWN"));
        xLog.getGlobalEventAttributes().add(new XAttributeLiteralImpl(XOrganizationalExtension.KEY_RESOURCE, "UNKNOWN"));
    }


    private String getStringValue(Row row, String colName) {
        return validColumn(row, colName) ? row.getString(colName) : null;
    }


    private Timestamp getTimeValue(Row row, String colName) {
        return validColumn(row, colName) ? Timestamp.valueOf(row.getDateTime(colName)) : null;
    }

    private long getLongValue(Row row, String colName) {
        return validColumn(row, colName) ? row.getLong(colName) : -1;
    }

    private Double getDoubleValue(Row row, String colName) {
        return validColumn(row, colName) ? row.getDouble(colName) : -1.0;
    }

    private Boolean getBooleanValue(Row row, String colName) {
        return validColumn(row, colName) ? row.getBoolean(colName) : null;
    }


    private boolean validColumn(Row row, String colName) {
        return colName != null && row.columnNames().contains(colName);
    }
    
    // check that used supplied col names match those in the table
    private void checkColumnNamesInTable(Table table, Map<String, String> nameMap) throws IOException {
        if (! nameMap.containsKey("case:id")) {
            throw new IOException("No column name provided for case:id");
        }

        Collection<String> udNames = nameMap.values();
        for (String colName : table.columnNames()) {
            if (! udNames.contains(colName)) {
                 throw new IOException("Missing mapping for column name : " + colName);
             }
        }
    }


    private Map<String, String> mapColNames() throws IOException {
        Map<String, String> map = new HashMap<>();
        try {
            mapColName(map, "case:id", "Case ID column");
            mapColName(map, "concept:name", "Name column");
            mapColName(map, "time:timestamp", "Time column");
            mapColName(map, "lifecycle:transition", "Lifecycle column");
            mapColName(map, "concept:instance", "Instance column");
            mapColName(map, "org:resource", "Resource column");
            mapColName(map, "data", "Data column");
        }
        catch (InvalidOptionException ipe) {
             throw new IOException(ipe.getMessage());
        }
        return map;
    }


    private void mapColName(Map<String, String> map, String key, String optionKey)
            throws InvalidOptionException {
        String userValue = getOptions().getNotNull(optionKey).asString();
        if (! userValue.isEmpty()) {
            map.put(key, userValue);
        }
    }


    private void addData(XEvent event, String data) {
        if (! (data == null || data.isEmpty())) {
            for (String entry : data.split(";")) {
                String[] parts = entry.split(",");
                String type = parts[0];
                String key = parts[1];
                String value = parts[2];

                XAttribute attribute;
                if ("string".equals(type)) {
                    attribute = new XAttributeLiteralImpl(key, value);
                }
                else if ("date".equals(type)) {
                    LocalDateTime dateTime = LocalDateTime.parse(value);
                    attribute = new XAttributeTimestampImpl(key, Timestamp.valueOf(dateTime));
                }
                else if ("long".equals(type)) {
                    attribute = new XAttributeDiscreteImpl(key, NumberUtils.toLong(value));
                }
                else if ("double".equals(type)) {
                    attribute = new XAttributeContinuousImpl(key, NumberUtils.toDouble(value));

                }
                else if ("boolean".equals(type)) {
                    attribute = new XAttributeBooleanImpl(key, "TRUE".equalsIgnoreCase(value));
                }
                else {
                    attribute = new XAttributeLiteralImpl(key, value);
                }

                event.getAttributes().put(key, attribute);
            }
        }
    }


    private void write(XLog xLog) throws IOException {
        XesXmlSerializer serializer = new XesXmlSerializer();
        serializer.serialize(xLog, getDestinationAsOutputStream());
    }
    
}