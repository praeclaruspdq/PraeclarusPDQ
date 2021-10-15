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

package com.processdataquality.praeclarus.ui.parameter.editor;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;

/**
 * @author Michael Adams
 * @date 15/6/21
 */
@Tag("input")
@JsModule("./src/fs.js")
public class FileInput extends Component {

    private String _fileKey;
    private String _content;

    public FileInput() {
        setId("fileinput");
        getElement().setAttribute("type", "file");
        getElement().setAttribute("name", "fileload");
        getElement().getStyle().set("display", "none");
        UI.getCurrent().getPage().executeJs("addUploadListener($0)", getElement());
    }

    public void click() {
        UI.getCurrent().getPage().executeJs("$0.click()", getElement());
    }


    public void upload() {
        UI.getCurrent().getPage().executeJs("upload($0, $1)", getElement(), _fileKey);
    }

    public boolean hasSelected() { return _fileKey != null; }

    public boolean hasContent() { return _content != null; }

    public String getFileName() { return _fileKey; }

    public String getContent() { return _content; }

    public void reset() {
        _fileKey = null;
        _content = null;
    }

    @ClientCallable
    private void setFileName(String fileName) {
        _fileKey = fileName;
    }

    @ClientCallable
    private void setContent(String content) {
        _content = content;
    }


}
