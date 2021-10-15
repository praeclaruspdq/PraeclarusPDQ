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

package com.processdataquality.praeclarus.ui.task;

import com.processdataquality.praeclarus.workspace.node.Node;
import com.processdataquality.praeclarus.workspace.node.NodeTask;
import com.vaadin.flow.component.dependency.JsModule;

/**
 * @author Michael Adams
 * @date 15/6/21
 */
@JsModule("./src/fs.js")
public class WriterTask implements NodeTask {


    @Override
    public boolean run(Node node) {
//        PDQPlugin plugin = node.getPlugin();
//        if (plugin instanceof DataWriter) {
//            try {
//                ByteArrayOutputStream stream =
//                        (ByteArrayOutputStream) ((DataWriter) plugin).getOutputStream();
//                String contents = stream.toString("UTF-8");
//                UI.getCurrent().getPage().executeJs("writeFile($0)", contents);
                return true;
//            }
//            catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//        }
//        return false;
    }

}
