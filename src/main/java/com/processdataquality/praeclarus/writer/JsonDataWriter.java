package com.processdataquality.praeclarus.writer;

import com.processdataquality.praeclarus.annotations.PluginMetaData;
import com.processdataquality.praeclarus.plugin.Options;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.Destination;
import tech.tablesaw.io.json.JsonWriteOptions;

import java.io.File;
import java.io.IOException;

/**
 * @author Michael Adams
 * @date 12/4/21
 */
@PluginMetaData(
        name = "JSON Writer",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Writes the log output to a JSON formatted file."
)
public class JsonDataWriter implements DataWriter {

    private Options _options;

    @Override
    public void write(Table table) throws IOException {
        if (_options == null) getOptions();  // fill null options with defaults
        JsonWriteOptions options = JsonWriteOptions.builder(
                new Destination(new File((String) _options.get("Destination"))))
                .header((boolean) _options.get("Header"))
                .build();
        table.write().usingOptions(options);
    }

    @Override
    public Options getOptions() {
        if (_options == null) {
            _options = new Options();
            _options.put("Header", true);
            _options.put("Destination", "out.json");
        }
        return _options;
    }

    @Override
    public void setOptions(Options options) {
        _options = options;
    }

}
