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

package com.processdataquality.praeclarus.plugin.uitemplate;

/**
 * @author Michael Adams
 * @date 1/11/21
 */
public class UIButton implements UIComponent {

    private String _label;
    private final ButtonAction _action;
    private ClickListener _listener;

    public UIButton(ButtonAction action) {
         _action = action;
    }

    public UIButton(ButtonAction action, String label) {
        this(action);
        _label = label;
    }

    public UIButton(ButtonAction action, String label, ClickListener listener) {
        this(action, label);
        _listener = listener;
    }


    public String getLabel() {
        return _label != null ? _label : _action.label();
    }

    public ButtonAction getAction() {
        return _action;
    }

    public ClickListener getListener() {
        return _listener;
    }
    
    public void setListener(ClickListener listener) {
        _listener = listener;
    }

    public boolean hasListener() { return _listener != null; }
    
}
