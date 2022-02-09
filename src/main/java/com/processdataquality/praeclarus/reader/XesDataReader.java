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

package com.processdataquality.praeclarus.reader;

import com.processdataquality.praeclarus.annotations.Plugin;
import com.processdataquality.praeclarus.plugin.Option;
import com.processdataquality.praeclarus.plugin.Options;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.*;
import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.io.ReadOptions;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Michael Adams
 * @date 31/3/21
 */
@Plugin(
        name = "XES Reader",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Loads a log file stored in XES format."
)
public class XesDataReader extends AbstractDataReader {


    @Override
    public Table read() throws IOException {
        return createTable(parseInput());
    }

    @Override
    public Options getOptions() {
        Options options = new Options();
        options.addDefault(new Option("Source", "", true));
        return options;
    }

    @Override
    protected ReadOptions getReadOptions() {             // N/A
        return null;
    }



    private List<XLog> parseInput() throws IOException {
        try {
            return new XesXmlParser().parse(getSourceAsInputStream());
        }
        catch (Exception e) {
            throw new IOException("Failed to load XES file", e);
        }
    }


    private Table createTable(List<XLog> logList) {
        List<Column<?>> columns = new ArrayList<>();
        columns.add(StringColumn.create("case:id"));  // trace concept:name = case id
        int rowCount = 0;

        for (XLog log : logList) {                                 // will be only one

            // create a column for each global event attribute
            List<XAttribute> globalEventAttributes = log.getGlobalEventAttributes();
            for (XAttribute globalEventAttribute : globalEventAttributes) {
                String key = globalEventAttribute.getKey();
                if (key.startsWith("time")) {
                    columns.add(DateTimeColumn.create(key));
                }
                else {
                    columns.add(StringColumn.create(key));
                }
            }

            // todo: not every event has an org entry, so column lens not equal

            // fill column rows with trace attributes
            for (XTrace trace : log) {
                XAttributeMap traceMap = trace.getAttributes();
                String caseID = ((XAttributeLiteral)traceMap.get("concept:name")).getValue();

                // any non-global event attributes are ignored
                for (XEvent event : trace) {
                    XAttributeMap map = event.getAttributes();
                    for (Column<?> column : columns) {
                        if ("case:id".equals(column.name())) {
                            ((StringColumn) column).append(caseID);
                            rowCount = column.size();
                            continue;
                        }
                        
                        XAttribute attribute = map.get(column.name());
                        if (attribute instanceof XAttributeLiteral) {
                            ((StringColumn) column).append(((XAttributeLiteral) attribute).getValue());
                        }
                        else if (attribute instanceof XAttributeTimestamp) {
                            Date date = ((XAttributeTimestamp) attribute).getValue();
                            LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(),
                                    ZoneId.systemDefault());
                            ((DateTimeColumn) column).append(ldt);
                        }
                    }

                    // ensure all columns are of equal length after each event processed
                    for (Column<?> column : columns) {
                        if (column.size() < rowCount) {
                            column.appendMissing();     
                        }
                    }
                }
            }
        }
        return Table.create(columns);
    }


    public static void main(String[] args) {
        XesDataReader reader = new XesDataReader();
        reader.setSource(new File("/Users/adamsmj/Downloads/Tutorial120.3.xes"));
        try {
            Table t = reader.read();
            System.out.println(t.structure());
            System.out.println(t.summary());
            System.out.println();
            System.out.println(t.first(50));

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}