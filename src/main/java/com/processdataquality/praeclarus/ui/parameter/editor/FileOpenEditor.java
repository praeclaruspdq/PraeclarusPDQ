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
import com.processdataquality.praeclarus.ui.parameter.PluginParameter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * @author Michael Adams
 * @date 8/6/21
 */
@JsModule("./src/fs.js")
public class FileOpenEditor extends AbstractFileEditor {

    public FileOpenEditor(PDQPlugin plugin, PluginParameter param) {
        super(plugin, param);
    }

    @Override
    protected Button createButton() {
        Icon icon = VaadinIcon.FOLDER_OPEN_O.create();
        icon.setSize("24px");
        return new Button(icon, e ->
                UI.getCurrent().getPage().executeJs("pickOpenFile($0, $1, $2)",
                        this.getId().get(), "CSV Files", "csv"));

        //        UI.getCurrent().getPage().executeJs("getFile($0)", this.getId().get()));

    }
}
