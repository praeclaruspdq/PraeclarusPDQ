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

package com.processdataquality.praeclarus.writer;

import com.processdataquality.praeclarus.option.FileOption;
import com.processdataquality.praeclarus.plugin.AbstractPlugin;
import com.processdataquality.praeclarus.util.DataCollection;
import org.apache.commons.io.output.WriterOutputStream;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.Destination;
import tech.tablesaw.io.WriteOptions;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * @author Michael Adams
 * @date 9/6/21
 */
public abstract class AbstractDataWriter extends AbstractPlugin implements DataWriter {

    protected Destination _destination;

    protected AbstractDataWriter() {
        getOptions().addDefault(new FileOption("Destination", ""));
    }

    // each sub-class will have unique read options for data format etc.
    protected abstract WriteOptions getWriteOptions() throws IOException;


    @Override
    public void write(Table table, DataCollection auxData) throws IOException {
        table.write().usingOptions(getWriteOptions());
    }


    @Override
    public Destination getDestination() {
        return _destination;
    }

    @Override
    public void setDestination(Destination destination) {
        _destination = destination;
    }


    public OutputStream getDestinationAsOutputStream() throws IOException {
        Destination destination = getDestination();
        if (destination != null) {
            if (destination.stream() != null) {
                return destination.stream();
            }
            if (destination.writer() != null) {
                return new WriterOutputStream(destination.writer(), Charset.defaultCharset());
            }
        }
        throw new IOException("Unable to get an OutputStream from Destination");
    }


    public void setDestination(File file) throws IOException {
        setDestination(new Destination(file));
    }


    public void setDestination(Writer writer) {
        setDestination(new Destination(writer));
    }


    public void setDestination(OutputStream stream) {
        setDestination(new Destination(stream));
    }

}
