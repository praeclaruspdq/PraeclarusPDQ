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

package com.processdataquality.praeclarus.ui.task;

import com.processdataquality.praeclarus.node.Node;
import com.processdataquality.praeclarus.node.NodeTask;
import com.vaadin.flow.component.dependency.JsModule;

/**
 * @author Michael Adams
 * @date 15/6/21
 */
@JsModule("./src/fs.js")
public class ReaderTask implements NodeTask {

//    private final FileInput _fileInput;

    public ReaderTask() {
//        _fileInput = fileInput;
    }
    
    @Override
    public boolean run(Node node) {
//        UploadDialog uploadDialog = new UploadDialog((AbstractDataReader) node.getPlugin());
//        new Thread(uploadDialog::open).start();
//
//        while (!uploadDialog.isClosed()) {
//            try {
//                Thread.sleep(200);
//            }
//            catch (InterruptedException e) {
//                // ignore
//            }
//        }
//        return uploadDialog.isAccepted();

          return true;


//        _fileInput.reset();
//        new Thread(_fileInput::click).start();
//
//        while (! _fileInput.hasContent()) {
//            try {
//                Thread.sleep(500);
//            }
//            catch (InterruptedException e) {
//                // ignore
//            }
//        }
//
//        PDQPlugin plugin = node.getPlugin();
//        String content = "a,c,c"; //_fileInput.getContent();
//        ((AbstractDataReader) plugin).setSource(IOUtils.toInputStream(content, Charset.defaultCharset()));
//        return true;
    }

}
