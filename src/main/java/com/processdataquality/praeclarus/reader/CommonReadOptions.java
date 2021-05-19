package com.processdataquality.praeclarus.reader;

import tech.tablesaw.io.ReadOptions;
import tech.tablesaw.io.Source;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 30/3/21
 */
public class CommonReadOptions {

    public Map<String, Object> toMap() {

        // create dummy source so we can build a default ReadOptions
        Source source = new Source(new File("temp.csv"), StandardCharsets.UTF_8);
        ReadOptions options = CsvReadOptions.builder(source).build();

        Map<String, Object> map = new HashMap<>();
        map.put("Source", "");
        map.put("Missing Value", options.missingValueIndicator());
        map.put("Date Format", options.dateFormatter());
        map.put("DateTime Format", options.dateTimeFormatter());
        map.put("Time Format", options.timeFormatter());
        map.put("Header", options.header());
        map.put("Table Name", options.tableName());
        map.put("Sample", options.sample());
        map.put("Locale", options.locale());
        return map;
    }

}
