package com.processdataquality.praeclarus.reader;

import com.processdataquality.praeclarus.plugin.Options;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.ReadOptions;

import java.io.IOException;

/**
 * @author Michael Adams
 * @date 29/4/21
 */
public abstract class AbstractDataReader implements DataReader {

    protected Options _options;
    
    protected abstract ReadOptions getReadOptions();

    @Override
    public Table read() throws IOException {
        return Table.read().usingOptions(getReadOptions());
    }


    @Override
    public Options getOptions() {
        if (_options == null) {
            _options = new Options();
            _options.putAll(new CommonReadOptions().toMap());
        }
        return _options;
    }


    @Override
    public void setOptions(Options options) {
        _options = options;
    }

}
