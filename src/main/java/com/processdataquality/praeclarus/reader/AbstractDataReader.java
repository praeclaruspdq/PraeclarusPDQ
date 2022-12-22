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

import com.processdataquality.praeclarus.exception.InvalidOptionValueException;
import com.processdataquality.praeclarus.option.FileOption;
import com.processdataquality.praeclarus.plugin.AbstractPlugin;
import org.apache.commons.io.input.ReaderInputStream;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.ReadOptions;
import tech.tablesaw.io.Source;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Michael Adams
 * @date 29/4/21
 */
public abstract class AbstractDataReader extends AbstractPlugin implements DataReader {

    protected Source _source;             // the data input source


    protected AbstractDataReader() {
        super();
        addDefaultOptions();
    }


    // each sub-class will have unique read options for data format etc.
    protected abstract ReadOptions getReadOptions() throws InvalidOptionValueException;


    @Override
    public int getMaxInputs() {
        return 0;
    }


    /**
     * Reads data from an input source into a Table
     * @return a Table containing the input data
     * @throws IOException if there's a problem reading
     */
    @Override
    public Table read() throws IOException {
        return Table.read().usingOptions(getReadOptions());
    }


    @Override
    public void setSource(Source source) {
        _source = source;
    }


    @Override
    public Source getSource() {
        if (_source == null) {
            throw new InvalidOptionValueException("Parameter 'Source' requires a value");
        }
        return _source;
    }


    /**
     * Extracts an InputStream from a Source (if possible)
     * @return A source's InputStream
     * @throws IOException if the source is null or an InputStream cannot be extracted
     */
    public InputStream getSourceAsInputStream() throws IOException {
        Source source = getSource();
        if (source != null) {
            if (source.inputStream() != null) {
                return source.inputStream(); 
            }
            if (source.file() != null) {
                return new FileInputStream(source.file());
            }
            if (source.reader() != null) {
                return new ReaderInputStream(source.reader(), StandardCharsets.UTF_8);
            }
        }
        throw new IOException("Unable to get InputStream from Source");
    }


    // thw methods below set the Source object from various supported sources

    public void setSource(File file) {
        setSource(file, Charset.defaultCharset());
    }


    public void setSource(File file, Charset charset) {
        setSource(new Source(file, charset));
    }


    public void setSource(InputStreamReader reader) {
        setSource(new Source(reader));
    }


    public void setSource(Reader reader) {
        setSource(new Source(reader));
    }


    public void setSource(InputStream inputStream) {
        setSource(inputStream, Charset.defaultCharset());
    }


    public void setSource(InputStream inputStream, Charset charset) {
        setSource(new Source(inputStream, charset));
    }


    public void setSource(String pathOrURL) {
        Source source;
        try {
            source = Source.fromUrl(pathOrURL);     // try URL first
        }
        catch (IOException e) {
            source = Source.fromString(pathOrURL);  // ok, must be a file path
        }
        setSource(source);
    }


    protected void addDefaultOptions() {
        getOptions().addDefaults(new CommonReadOptions().toMap());
        getOptions().addDefault(new FileOption("Source", ""));
    }

}
