package com.processdataquality.praeclarus.reader;

import com.processdataquality.praeclarus.annotations.PluginMetaData;
import tech.tablesaw.io.json.JsonReadOptions;

import java.time.format.DateTimeFormatter;

/**
 * @author Michael Adams
 * @date 29/3/21
 */
@PluginMetaData(
        name = "JSON Reader",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Loads a log file formatted as JSON."
)
public class JsonDataReader extends AbstractDataReader {

    protected JsonReadOptions getReadOptions() {
        String fileName = (String) _options.get("Source");
        return JsonReadOptions.builder(fileName)
                .missingValueIndicator((String) _options.get("Missing Value"))
                .dateFormat(DateTimeFormatter.ofPattern((String) _options.get("Date Format")))
                .timeFormat(DateTimeFormatter.ofPattern((String) _options.get("Time Format")))
                .dateTimeFormat(DateTimeFormatter.ofPattern((String) _options.get("DateTime Format")))
                .header((boolean) _options.get("Header"))
                .tableName((String) _options.get("Table Name"))
                .sample(((int) _options.get("Sample")) > 0)
                .build();
    }

}
