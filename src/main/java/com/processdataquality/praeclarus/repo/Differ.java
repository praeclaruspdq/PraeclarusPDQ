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

package com.processdataquality.praeclarus.repo;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import tech.tablesaw.api.Table;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 10/11/21
 */
public class Differ {

    public List<Table> diff(String content1, String content2) throws IOException {
        StringBuilder content1Diff = new StringBuilder();
        StringBuilder content2Diff = new StringBuilder();
        LineIterator li1 = new LineIterator(new StringReader(content1));
        LineIterator li2 = new LineIterator(new StringReader(content2));

        while (li1.hasNext() || li2.hasNext()) {
            String left = (li1.hasNext() ? li1.nextLine() : "") + "\n";
            String right = (li2.hasNext() ? li2.nextLine() : "") + "\n";

            // add first line as header, otherwise add if different
            if (content1Diff.length() == 0 || !left.equals(right)) {
                content1Diff.append(left);
                content2Diff.append(right);
            }
        }
        return tableList(content1Diff.toString(), content2Diff.toString());
    }


    private List<Table> tableList(String lines1, String lines2) throws IOException {
        List<Table> tableList = new ArrayList<>();
        tableList.add(csvToTable(lines2));               // list previous first
        tableList.add(csvToTable(lines1));
        return tableList;
    }


    private Table csvToTable(String csvLines) throws IOException {
        return Table.read().csv(IOUtils.toInputStream(csvLines, Charset.defaultCharset()));
    }

}
