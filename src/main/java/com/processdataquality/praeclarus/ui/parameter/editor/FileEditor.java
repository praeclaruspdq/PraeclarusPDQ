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

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.reader.FileDataReader;
import com.processdataquality.praeclarus.ui.parameter.PluginParameter;
import com.processdataquality.praeclarus.util.FileUtil;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Michael Adams
 * @date 5/5/21
 */
@CssImport("./styles/pdq-styles.css")
@JsModule("./src/fs.js")
public class FileEditor extends AbstractEditor {

    public enum TYPE { Open, Save}

    private static final AtomicInteger ID_SUFFIX = new AtomicInteger();

    private TextField _field;
    private final TYPE _type;


    public FileEditor(PDQPlugin plugin, PluginParameter param, TYPE type) {
        super(plugin, param);
        _type = type;
        setId("filepropertyeditor" + ID_SUFFIX.getAndIncrement());
    }

    @ClientCallable
    private void setfile(String fileName, String content) {
        _field.setValue(fileName);
        File temp = FileUtil.stringToTempFile(content);
        if (temp != null && getPlugin() instanceof FileDataReader) {
            ((FileDataReader) getPlugin()).setFilePath(temp.getPath());
        }
    }


    protected HorizontalLayout createField(PluginParameter param) {
        _field = initTextField(param);

        HorizontalLayout layout = new HorizontalLayout();
        layout.add(_field, createButton());
        layout.setFlexGrow(1f, _field);
        layout.setWidth("75%");
        return layout;
    }


    private Button createButton() {
        Icon icon = VaadinIcon.FOLDER_OPEN_O.create();
        icon.setSize("24px");
        return new Button(icon, e ->
                UI.getCurrent().getPage().executeJs("getFile($0)",
                        this.getId().get()));
    }

    
}
