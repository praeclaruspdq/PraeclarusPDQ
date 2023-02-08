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

package com.processdataquality.praeclarus.ui.parameter;

import com.processdataquality.praeclarus.option.*;
import com.processdataquality.praeclarus.support.math.Pair;
import com.processdataquality.praeclarus.ui.parameter.editor.*;
import com.processdataquality.praeclarus.writer.DataWriter;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.List;

/**
 * @author Michael Adams
 * @date 31/3/2022
 */
public class EditorFactory {

    public HorizontalLayout create(HasOptions container, Option option) {
        if (option instanceof FileOption) {
            if (container instanceof DataWriter) {
                return new FileSaveEditor(container, option);
            }
            else {
                return new FileOpenEditor(container, option);
            }
        }
        else if (option instanceof ColumnNameListOption) {
            if (option.value() instanceof List<?>) {
                return new ImmutableListEditor(container, option);
            }
        }
        
        else if (option instanceof ListOption) {
            if (option.value() instanceof List<?>) {
                return new ImmutableListEditor(container, option);
            }
        }
        
        else if (option instanceof ColumnNameListAndStringOption) {
            if (option.value() instanceof Pair<?, ?>) {
                return new ListAndStringEditor(container, option);
            }
        }
        else if (option instanceof MultiLineOption) {
           return new TextEditor(container, option);
        }
        else if (option.value() instanceof Boolean) {
            return new BooleanEditor(container, option);
        }
        else if (option.value()instanceof Integer) {
            return new IntEditor(container, option);
        }
        else if (option.value() instanceof Double) {
            return new NumberEditor(container, option);
        }
        else if (option.value() instanceof String[]) {
            return new StringListEditor(container, option);
        }
        else if (option.value() instanceof List<?>) {
            return new ImmutableListEditor(container, option);
        }

        return new StringEditor(container, option);
    }
}
