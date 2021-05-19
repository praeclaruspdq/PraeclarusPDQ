package com.processdataquality.praeclarus.writer;

import com.processdataquality.praeclarus.annotations.PluginMetaData;
import com.processdataquality.praeclarus.plugin.Options;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.Destination;
import tech.tablesaw.io.html.HtmlWriteOptions;

import java.io.File;
import java.io.IOException;

/**
 * @author Michael Adams
 * @date 12/4/21
 */
@PluginMetaData(
        name = "HTML Writer",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Writes the log output to a HTML formatted file."
)
public class HtmlDataWriter implements DataWriter {

    private Options _options;

    @Override
    public void write(Table table) throws IOException {
        if (_options == null) getOptions();  // fill null options with defaults
        HtmlWriteOptions options = HtmlWriteOptions.builder(
                new Destination(new File((String) _options.get("Destination"))))
                .escapeText((boolean ) _options.get("Escape Text"))
                .build();
        table.write().usingOptions(options);
    }

    @Override
    public Options getOptions() {
        if (_options == null) {
            _options = new Options();
            _options.put("Escape Text", true);
            _options.put("Destination", "out.html");
        }
        return _options;
    }

    @Override
    public void setOptions(Options options) {
        _options = options;
    }

}
