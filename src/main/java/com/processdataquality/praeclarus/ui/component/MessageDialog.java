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

package com.processdataquality.praeclarus.ui.component;


import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * @author Michael Adams
 * @date 25/11/21
 */
public class MessageDialog extends Dialog {

    private final HorizontalLayout _buttonBar = new HorizontalLayout();
    private final Div _text = new Div();

    public MessageDialog(String title) {
        setWidth("300px");
        setModal(true);
        setCloseOnOutsideClick(false);
        add(new Div(new H3(title)));
//        _buttonBar.getStyle().set("background-color", "#F4F5F7");
        add(_text, _buttonBar);
    }


    public void setText(String text) {
        _text.add(new Html("<p>" + text + "</p>"));
    }


    public void addButton(Button b) {
        _buttonBar.add(b);
        b.addClickListener(e -> close());
    }
}
