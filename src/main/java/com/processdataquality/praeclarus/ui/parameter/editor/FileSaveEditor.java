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

import com.processdataquality.praeclarus.option.HasOptions;
import com.processdataquality.praeclarus.option.Option;
import com.processdataquality.praeclarus.writer.AbstractDataWriter;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.io.StringWriter;

/**
 * @author Michael Adams
 * @date 8/6/21
 */
public class FileSaveEditor extends AbstractFileEditor {

    public FileSaveEditor(HasOptions container, Option option) {
        super(container, option);
    }


    @Override
    protected Button createButton() {
        Icon icon = VaadinIcon.DOWNLOAD_ALT.create();
        icon.setSize("24px");
        return new Button(icon, e ->
                UI.getCurrent().getPage().executeJs("pickSaveFile" + "($0, $1)",
                        this.getId().get(), getOpts()));
    }


    @ClientCallable
    private void setSaveFileName(String fileName) {
        setValue(fileName);
        ((AbstractDataWriter) getPlugin()).setDestination(new LogWriter(this.getId().get()));
    }


    protected String getOpts() {
        String[] parts = getFileDescriptors();
        if (parts.length == 3) {
            return String.format("{\"types\": [{ \"description\": \"%s\", " +
                    "\"accept\": {\"%s\": [\"%s\"]}}]}", (Object[]) parts);
        }
        return "{}";
    }


    
    static class LogWriter extends StringWriter {

        String _key;

        LogWriter(String key) {
            super();
            _key = key;
        }


        // called when writing is complete and ready to be sent to file
        @Override
        public void flush() {
            super.flush();

            UI.getCurrent().getPage().executeJs("writeFile" + "($0, $1)",
                    _key, this.toString());

        }
    }

}
