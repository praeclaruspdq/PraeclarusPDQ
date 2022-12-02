/*
 * Copyright (c) 2022 Queensland University of Technology
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

import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.option.HasOptions;
import com.processdataquality.praeclarus.option.Option;
import com.processdataquality.praeclarus.plugin.AbstractPlugin;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

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

    public AbstractFileEditor(HasOptions container, Option option) {
        super(container, option);
        setId("filepropertyeditor" + ID_SUFFIX.getAndIncrement());
    }


    protected void setValue(Object value) {
//        updateOption(value);
        _field.setValue(String.valueOf(value));
    }


    protected String[] getFileDescriptors() {
        Plugin metaData = getPlugin().getClass().getAnnotation(Plugin.class);
        if (metaData != null) {
            String descriptors = metaData.fileDescriptors();
            if (!descriptors.isEmpty()) {
                return descriptors.split(";");
            }
        }
        return null;
    }


    protected HorizontalLayout createField() {
        _field = initTextField();

        HorizontalLayout layout = new HorizontalLayout();
        layout.add(_field, createButton());
        layout.setFlexGrow(1f, _field);
        layout.setWidth("75%");
        return layout;
    }


    protected AbstractPlugin getPlugin() { return (AbstractPlugin) getContainer(); }

    protected abstract Button createButton() ;


}
