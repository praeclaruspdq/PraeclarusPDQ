package com.processdataquality.praeclarus.reader;

import com.processdataquality.praeclarus.annotations.PluginMetaData;
import com.processdataquality.praeclarus.plugin.Options;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.*;
import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * @author Michael Adams
 * @date 31/3/21
 */
@PluginMetaData(
        name = "XES Reader",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Loads a log file stored in XES format."
)
public class XesDataReader implements DataReader {

    private String filePath;

    @Override
    public Table read() throws IOException {
        return createTable(parseInput());
    }

    @Override
    public Options getOptions() {
        Options options = new Options();
        options.put("Source", "");
        return options;
    }

    @Override
    public void setOptions(Options options) {
        filePath = (String) options.get("Source");
    }


    private List<XLog> parseInput() throws IOException {
        try {
            return new XesXmlParser().parse(new File(filePath));
        }
        catch (Exception e) {
            throw new IOException("Failed to load XES file", e);
        }
    }

    private Table createTable(List<XLog> logList) {
        List<Column<?>> columns = new ArrayList<>();
        columns.add(StringColumn.create("Case ID"));
        for (XLog log : logList) {
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

//            List<XAttribute> globalTraceAttributes = log.getGlobalTraceAttributes();
            for (XTrace trace : log) {
                XAttributeMap traceMap = trace.getAttributes();

                String caseID = ((XAttributeLiteral)traceMap.get("concept:name")).getValue();
                for (XEvent event : trace) {
                    XAttributeMap map = event.getAttributes();
                    for (Column<?> column : columns) {
                        if ("Case ID".equals(column.name())) {
                            ((StringColumn) column).append(caseID);
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
                }
            }
        }
        return Table.create(columns);
    }


    public static void main(String[] args) {
        XesDataReader reader = new XesDataReader();
        reader.filePath = "/Users/adamsmj/Downloads/Tutorial120.3.xes";
        try {
            Table t = reader.read();
            System.out.println(t.structure());
            System.out.println(t.first(5));

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}