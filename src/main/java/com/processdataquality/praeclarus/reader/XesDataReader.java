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

package com.processdataquality.praeclarus.reader;

import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.option.FileOption;
import org.apache.commons.lang3.StringUtils;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.*;
import tech.tablesaw.api.*;
import tech.tablesaw.columns.Column;
import tech.tablesaw.io.ReadOptions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

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

    private final Map<String, Column<?>> _columns = new HashMap<>();
    private final StringColumn _dataColumn = StringColumn.create("data");
    private boolean _dataColumnHasValues = false;
    private boolean _globalsOnly = false;
    private boolean _includeData = false;


    public XesDataReader() {
        super();
        addDefaultOptions();
    }


    @Override
    public Table read() throws IOException {
        return createTable(parseInput());
    }

    protected void addDefaultOptions() {
        getOptions().addDefault("Globals Only", false);
        getOptions().addDefault("Include Data", true);
        getOptions().addDefault(new FileOption("Source", ""));
    }

    @Override
    protected ReadOptions getReadOptions() {             // N/A
        return null;
    }



    private List<XLog> parseInput() throws IOException {
        try {
            InputStream is = getSourceAsInputStream();
            if (is == null) {
                throw new IOException("Failed to read: No XES input source specified");
            }

            return new XesXmlParser().parse(getSourceAsInputStream());
        }
        catch (Exception e) {
            throw new IOException("Failed to load XES file", e);
        }
    }


    private Table createTable(List<XLog> logList) {
        _globalsOnly = getOptions().get("Globals Only").asBoolean();
        _includeData = getOptions().get("Include Data").asBoolean();

        for (XLog log : logList) {                                 // will be only one
            if (_globalsOnly) {
                addGlobalColumns(log);
            }
            parseLog(log);
        }

        List<Column<?>> columnList = new ArrayList<>(_columns.values());
        if (_includeData && _dataColumnHasValues) {
            padColumn(_dataColumn, getRowCount());
            columnList.add(_dataColumn);
        }

        return Table.create(columnList);
    }


    private void parseLog(XLog log) {
        for (XTrace trace : log) {
            parseTrace(trace);
        }
    }

    private void parseTrace(XTrace trace) {
        XAttributeMap traceMap = trace.getAttributes();
        String caseID = ((XAttributeLiteral) traceMap.get("concept:name")).getValue();

        for (XEvent event : trace) {
            getStringColumn("case:id").append(caseID);
            parseEvent(event);
        }
    }

    // one row of table
    private void parseEvent(XEvent event) {
        StringBuilder data = new StringBuilder();
        XAttributeMap map = event.getAttributes();
        for (String key : map.keySet()) {
            XAttribute attribute = map.get(key);
            if (attribute.getExtension() == null) {
                if (_includeData) {
                    data.append(parseDataAttribute(attribute));
                }
            }
            else if (_globalsOnly && ! _columns.containsKey(key)) {
                 continue;
            }
            else if (attribute instanceof XAttributeLiteral) {
                getStringColumn(key).append(((XAttributeLiteral) attribute).getValue());
            }
            else if (attribute instanceof XAttributeTimestamp) {
                Date date = ((XAttributeTimestamp) attribute).getValue();
                LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(),
                        ZoneId.systemDefault());
                getDateTimeColumn(key).append(ldt);
            }
            else if (attribute instanceof XAttributeDiscrete) {
                getLongColumn(key).append(((XAttributeDiscrete) attribute).getValue());
            }
            else if (attribute instanceof XAttributeContinuous) {
                getDoubleColumn(key).append(((XAttributeContinuous) attribute).getValue());
            }
            else if (attribute instanceof XAttributeBoolean) {
                getBooleanColumn(key).append(((XAttributeBoolean) attribute).getValue());
            }
        }
        if (data.length() > 0) {
            padColumn(_dataColumn, getRowCount() -1);
            _dataColumn.append(data.toString());
            _dataColumnHasValues = true;
        }
        padColumns(getRowCount());
    }
    

    private String parseDataAttribute(XAttribute attribute) {
        String key = attribute.getKey();
        String value = attribute.toString();
        String type = getAttributeType(attribute);
        return StringUtils.joinWith(",", type, key, value) + ";";
    }


    private String getAttributeType(XAttribute attribute) {
        if (attribute instanceof XAttributeLiteral) return "string";
        if (attribute instanceof XAttributeTimestamp) return "date";
        if (attribute instanceof XAttributeDiscrete) return "long";
        if (attribute instanceof XAttributeContinuous) return "double";
        if (attribute instanceof XAttributeBoolean) return "boolean";
        return "string";  // default
    }

    // create a column for each global event attribute
    private void addGlobalColumns(XLog log) {
        for (XAttribute attribute : log.getGlobalEventAttributes()) {
            String key = attribute.getKey();
            if (attribute instanceof XAttributeLiteral) getStringColumn(key);
            else if (attribute instanceof XAttributeTimestamp) getDateTimeColumn(key);
            else if (attribute instanceof XAttributeDiscrete) getLongColumn(key);
            else if (attribute instanceof XAttributeContinuous) getDoubleColumn(key);
            else if (attribute instanceof XAttributeBoolean) getBooleanColumn(key);
            else getStringColumn(key);  // default
        }
    }

    private StringColumn getStringColumn(String name) {
        StringColumn column = (StringColumn) _columns.get(name);
        if (column == null) {
            column = StringColumn.create(name);
            addColumn(column);
        }
        return column;
    }

    private BooleanColumn getBooleanColumn(String name) {
        BooleanColumn column = (BooleanColumn) _columns.get(name);
        if (column == null) {
            column = BooleanColumn.create(name);
            addColumn(column);
        }
        return column;
    }

    private DateTimeColumn getDateTimeColumn(String name) {
        DateTimeColumn column = (DateTimeColumn) _columns.get(name);
        if (column == null) {
            column = DateTimeColumn.create(name);
            addColumn(column);
        }
        return column;
    }

    private LongColumn getLongColumn(String name) {
        LongColumn column = (LongColumn) _columns.get(name);
        if (column == null) {
            column = LongColumn.create(name);
            addColumn(column);
        }
        return column;
    }

    private DoubleColumn getDoubleColumn(String name) {
        DoubleColumn column = (DoubleColumn) _columns.get(name);
        if (column == null) {
            column = DoubleColumn.create(name);
            addColumn(column);
        }
        return column;
    }

    private void addColumn(Column<?> column) {
        _columns.put(column.name(), column);
        padColumn(column, getRowCount() - 1);
    }

    private int getRowCount() {
        return getStringColumn("case:id").size();
    }

    private void padColumns(int count) {
        for (Column<?> column : _columns.values()) {
            if (column.size() < count) {
                padColumn(column, count);
            }
        }
    }

    private void padColumn(Column<?> column, int count) {
        for (int i = column.size(); i < count; i++) {
            column.appendMissing();
        }
    }

    
    public static void main(String[] args) {
        XesDataReader reader = new XesDataReader();
        reader.setSource(new File("/Users/adamsmj/Documents/Git/contributions/praeclarus/sareh220209/updates220214/reviewing.xes"));
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