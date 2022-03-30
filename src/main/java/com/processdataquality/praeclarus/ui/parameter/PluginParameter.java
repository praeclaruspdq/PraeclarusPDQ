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

package com.processdataquality.praeclarus.ui.parameter;

import com.google.common.collect.ImmutableList;
import com.processdataquality.praeclarus.option.ColumnNameListOption;
import com.processdataquality.praeclarus.option.FileOption;
import com.processdataquality.praeclarus.option.Option;
import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.ui.parameter.editor.*;
import com.processdataquality.praeclarus.writer.DataWriter;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.List;

/**
 * @author Michael Adams
 * @date 29/4/21
 */
public class PluginParameter {

    private final Option _option;
    private boolean _updated = false;

    public PluginParameter(Option option) {
        _option = option;
    }

    public String getName() { return _option.key(); }

    public Object getValue() { return _option.value(); }

    public String getStringValue() { return String.valueOf(getValue()); }

    public boolean isUpdated() { return _updated; }


    public Option getOption() { return _option; }


    public void setValue(Object value) {
        _option.setValue(value);
        _updated = true;
    }

    public void setStringValue(String value) {
        convertAndSet(value);
    }

    public HorizontalLayout editor(PDQPlugin plugin) {
        if (_option instanceof FileOption) {
            if (plugin instanceof DataWriter) {
                return new FileSaveEditor(plugin, this);
            }
            else {
                return new FileOpenEditor(plugin, this);
            }
        }
        else if (_option instanceof ColumnNameListOption) {
            if (getValue() instanceof List<?>) {
                return new ImmutableListEditor(plugin, this);
            }
        }
        else if (getValue() instanceof Boolean) {
            return new BooleanEditor(plugin, this);
        }

//        else if (_value instanceof Character) {
//            _value = value.charAt(0);
//        }
        else if (getValue() instanceof Integer) {
            return new IntEditor(plugin, this);
        }
        else if (getValue() instanceof Double) {
            return new NumberEditor(plugin, this);
        }
        else if (getValue() instanceof String[]) {
            return new StringListEditor(plugin, this);
        }
        else if (getValue() instanceof ImmutableList) {
            return new ImmutableListEditor(plugin, this);
        }
        
        return new StringEditor(plugin, this);
    }


    private void convertAndSet(String value) {
        if (getValue() instanceof Boolean) {
            setValue(Boolean.valueOf(value));
        }
        else if (getValue() instanceof Character) {
            setValue(value.charAt(0));
        }
        else if (getValue() instanceof Integer) {
            setValue(Integer.valueOf(value));
        }
        else if (getValue() instanceof Long) {
            setValue(Long.valueOf(value));
        }
        else {
            setValue(value);
        }
    }

}
