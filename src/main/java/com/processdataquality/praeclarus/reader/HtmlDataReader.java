package com.processdataquality.praeclarus.reader;

import com.processdataquality.praeclarus.annotations.PluginMetaData;
import com.processdataquality.praeclarus.plugin.Options;
import tech.tablesaw.io.html.HtmlReadOptions;

import java.time.format.DateTimeFormatter;

/**
 * @author Michael Adams
 * @date 30/3/21
 */
@PluginMetaData(
        name = "HTML Reader",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Loads a log file formatted as HTML."
)
public class HtmlDataReader extends AbstractDataReader {
    
    @Override
    public Options getOptions() {
        Options options = super.getOptions();
        if (! options.containsKey("Table Index")) {
            options.put("Table Index", 0);
        }
        return options;
    }


    protected HtmlReadOptions getReadOptions() {
        String urlStr = (String) _options.get("Source");
        return HtmlReadOptions.builder(urlStr)
                .missingValueIndicator((String) _options.get("Missing Value"))
                .dateFormat(DateTimeFormatter.ofPattern((String) _options.get("Date Format")))
                .timeFormat(DateTimeFormatter.ofPattern((String) _options.get("Time Format")))
                .dateTimeFormat(DateTimeFormatter.ofPattern((String) _options.get("DateTime Format")))
                .header((boolean) _options.get("Header"))
                .tableName((String) _options.get("Table Name"))
                .sample((boolean) _options.get("Sample"))
                .tableIndex((int) _options.get("Table Index"))
                .build();
    }

}
