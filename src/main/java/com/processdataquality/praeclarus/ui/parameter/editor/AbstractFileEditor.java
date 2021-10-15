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

import com.processdataquality.praeclarus.annotations.Plugin;
import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.ui.parameter.PluginParameter;
import com.processdataquality.praeclarus.ui.util.FileUtil;
import com.processdataquality.praeclarus.writer.DataWriter;
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
public abstract class AbstractFileEditor extends AbstractEditor {

    private static final AtomicInteger ID_SUFFIX = new AtomicInteger();
    private TextField _field;

    public AbstractFileEditor(PDQPlugin plugin, PluginParameter param) {
        super(plugin, param);
        setId("filepropertyeditor" + ID_SUFFIX.getAndIncrement());
    }

    @ClientCallable
    private void setfile(String fileName, String content) {
        _field.setValue(fileName);
        File temp = FileUtil.stringToTempFile(content);
//        if (temp != null && getPlugin() instanceof FileDataReader) {
//            ((FileDataReader) getPlugin()).setFilePath(temp.getPath());
//        }
    }

//    @ClientCallable
//    private void setFileName(String fileName) {
//        _field.setValue(fileName);
//    }


    @ClientCallable
    private void setSaveFileName(String fileName) {
        _field.setValue(fileName);
        if (getPlugin() instanceof DataWriter) {
//            ((DataWriter) getPlugin()).setOutputStream(new ByteArrayOutputStream());
        }
    }


    protected void setValue(Object value) {
        getParam().setValue(value);
        updateProperties(getParam());
        _field.setValue(getParam().getStringValue());
    }

    protected HorizontalLayout createField(PluginParameter param) {
        _field = initTextField(param);

        HorizontalLayout layout = new HorizontalLayout();
        layout.add(_field, createButton());
        layout.setFlexGrow(1f, _field);
        layout.setWidth("75%");
        return layout;
    }


    protected Button createButton(VaadinIcon vaadinIcon, String scriptName) {
        Icon icon = vaadinIcon.create();
        icon.setSize("24px");
        return new Button(icon, e ->
                UI.getCurrent().getPage().executeJs(scriptName + "($0, $1)",
                        this.getId().get(), getOpts()));
    }


    protected String getOpts() {
        Plugin metaData = getPlugin().getClass().getAnnotation(Plugin.class);
        if (metaData != null) {
            String descriptors = metaData.fileDescriptors();
            if (! descriptors.isEmpty()) {
                String[] parts = descriptors.split(";");
                if (parts.length == 3) {
                    return String.format("{\"types\": [{ \"description\": \"%s\", " +
                                   "\"accept\": {\"%s\": [\"%s\"]}}]}", (Object[]) parts);
                }
            }
        }
        return "{}";
    }


    protected abstract Button createButton() ;

}
