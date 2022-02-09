/*
 * Copyright (c) 2021 Queensland University of Technology
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

import com.processdataquality.praeclarus.annotations.Plugin;
import com.processdataquality.praeclarus.plugin.Option;
import com.processdataquality.praeclarus.plugin.Options;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.classification.XEventResourceClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.deckfour.xes.out.XesXmlSerializer;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.WriteOptions;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
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

    @Override
    public void write(Table table) throws IOException {
        XLog log = createXLog(table);
        write(log);
    }


    @Override
    public Options getOptions() {
        _options.addDefault(new Option("Destination", "", true));
        _options.addDefault("Case ID column", "case:id");
        _options.addDefault("Name column", "concept:name");
        _options.addDefault("Time column", "time:timestamp");
        _options.addDefault("Lifecycle column", "lifecycle:transition");
        _options.addDefault("Instance column", "concept:instance");
        _options.addDefault("Resource column", "org:resource");
        return _options;
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
        XConceptExtension conceptExtension = XConceptExtension.instance();
        XLifecycleExtension lifeExtension = XLifecycleExtension.instance();
        XTimeExtension timeExtension = XTimeExtension.instance();
        XOrganizationalExtension orgExtension = XOrganizationalExtension.instance();
        xLog.getExtensions().add(conceptExtension);
        xLog.getExtensions().add(lifeExtension);
        xLog.getExtensions().add(timeExtension);
        xLog.getExtensions().add(orgExtension);
        xLog.getClassifiers().add(new XEventNameClassifier());
        xLog.getClassifiers().add(new XEventLifeTransClassifier());
        xLog.getClassifiers().add(new XEventResourceClassifier());
        conceptExtension.assignName(xLog, table.name());            
        lifeExtension.assignModel(xLog, XLifecycleExtension.VALUE_MODEL_STANDARD);

        xLog.getGlobalTraceAttributes().add(new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, "UNKNOWN"));

        xLog.getGlobalEventAttributes().add(new XAttributeTimestampImpl(XTimeExtension.KEY_TIMESTAMP, 0));
        xLog.getGlobalEventAttributes().add(new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, "UNKNOWN"));
        xLog.getGlobalEventAttributes().add(new XAttributeLiteralImpl(XLifecycleExtension.KEY_TRANSITION, "UNKNOWN"));
        xLog.getGlobalEventAttributes().add(new XAttributeLiteralImpl(XConceptExtension.KEY_INSTANCE, "UNKNOWN"));
        xLog.getGlobalEventAttributes().add(new XAttributeLiteralImpl(XOrganizationalExtension.KEY_RESOURCE, "UNKNOWN"));

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

            // todo: ensure valid lifecycle value
            XEvent xEvent = xFactory.createEvent();
            String name = getStringValue(row, colNames.get("concept:name"));
            if (name != null) {
                conceptExtension.assignName(xEvent, name);
            }
            String instance = getStringValue(row, colNames.get("concept:instance"));
            if (instance != null) {
                conceptExtension.assignInstance(xEvent, instance);
            }
            String transition = getStringValue(row, colNames.get("lifecycle:transition"));
            if (transition != null) {
                lifeExtension.assignTransition(xEvent, transition);
            }
            String resource = getStringValue(row, colNames.get("org:resource"));
            if (resource != null) {
                orgExtension.assignResource(xEvent, resource);
            }

            Timestamp timestamp = getTimeValue(row, colNames.get("time:timestamp"));
            if (timestamp != null) {
                timeExtension.assignTimestamp(xEvent,timestamp);
            }
            
            xTrace.add(xEvent);
        }
        return xLog;
    }


    private String getStringValue(Row row, String colName) {
        if (colName != null && row.columnNames().contains(colName)) {
            return row.getString(colName);
        }
        return null;
    }


    private Timestamp getTimeValue(Row row, String colName) {
        if (colName != null && row.columnNames().contains(colName)) {
            return Timestamp.valueOf(row.getDateTime(colName));
        }
        return null;
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


    private Map<String, String> mapColNames() {
        Map<String, String> map = new HashMap<>();
        mapColName(map, "case:id", "Case ID column");
        mapColName(map, "concept:name", "Name column");
        mapColName(map, "time:timestamp", "Time column");
        mapColName(map, "lifecycle:transition", "Lifecycle column");
        mapColName(map, "concept:instance", "Instance column");
        mapColName(map, "org:resource", "Resource column");
        return map;
    }


    private void mapColName(Map<String, String> map, String key, String optionKey) {
        String userValue = getOptions().get(optionKey).asString();
        if (! (userValue == null || userValue.isEmpty())) {
            map.put(key, userValue);
        }
    }


    private void write(XLog xLog) throws IOException {
        XesXmlSerializer serializer = new XesXmlSerializer();
        serializer.serialize(xLog, getDestinationAsOutputStream());
    }
    
}
