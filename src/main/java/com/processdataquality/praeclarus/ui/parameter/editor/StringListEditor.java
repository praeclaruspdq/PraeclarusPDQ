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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;

/**
 * @author Michael Adams
 * @date 27/10/21
 */
public class StringListEditor extends AbstractEditor {

    public StringListEditor(HasOptions container, Option option) {
         super(container, option);
     }


    @Override
    protected Component createField() {
        ComboBox<String> field = new ComboBox<>();
        field.setItems((String[]) getOption().value());
        field.setClearButtonVisible(true);
        field.setWidth("75%");

        // user had added a new value
        field.addCustomValueSetListener(e -> {
            String value = e.getDetail();
            if (isValidValue((String[]) getOption().value(), value)) {
                updateOption(addItem(value));
                field.setItems((String[]) getOption().value());
            }
        });
        return field;
    }


    private boolean isValidValue(String[] extantValues, String value) {
        if (value == null || value.isEmpty()) return false;
        for (String extantValue : extantValues) {
            if (value.equals(extantValue)) return false;
        }
        return true;
    }


    private String[] addItem(String value) {
        String[] items = (String[]) getOption().value();
        String[] newItems = new String[items.length + 1];
        System.arraycopy(items, 0, newItems, 0, items.length);
        newItems[items.length] = value;
        return newItems;
    }
}
