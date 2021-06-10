/*
 * Copyright (c) 2021 Queensland University of Technology
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

package com.processdataquality.praeclarus.ui.util;

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.workspace.node.Node;
import com.processdataquality.praeclarus.writer.DataWriter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author Michael Adams
 * @date 9/6/21
 */
@JsModule("./src/fs.js")
public class NodeWriter {

    public NodeWriter() {  }

    public void write(Node node) {
        PDQPlugin plugin = node.getPlugin();
        if (plugin instanceof DataWriter) {
            try {
                ByteArrayOutputStream stream =
                        (ByteArrayOutputStream) ((DataWriter) plugin).getOutputStream();
                String contents = stream.toString("UTF-8");
                UI.getCurrent().getPage().executeJs("writeFile($0)", contents);
            }
            catch (UnsupportedEncodingException uee) {
                //
            }
        }
    }
    
}
