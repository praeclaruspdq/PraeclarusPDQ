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

import com.processdataquality.praeclarus.annotation.Plugin;
import tech.tablesaw.io.WriteOptions;
import tech.tablesaw.io.html.HtmlWriteOptions;

import java.io.IOException;

/**
 * @author Michael Adams
 * @date 12/4/21
 */
@Plugin(
        name = "HTML Writer",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Writes the log output to a HTML formatted file."
)
public class HtmlDataWriter extends AbstractDataWriter {

    public HtmlDataWriter() {
        super();
        getOptions().addDefault("Escape Text", true);
    }


    @Override
    protected WriteOptions getWriteOptions() throws IOException {
        HtmlWriteOptions.Builder builder = HtmlWriteOptions.builder(getDestination());
        for (String key : getOptions().getChanges().keySet()) {
            if (key.equals("Escape Text")) {
                builder.escapeText(getOptions().get("Escape Text").asBoolean());
            }
        }
        return builder.build();
    }

    
}
