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

package com.processdataquality.praeclarus.ui.component.dialog;


import com.processdataquality.praeclarus.ui.component.layout.JustifiedButtonLayout;
import com.processdataquality.praeclarus.ui.util.UiUtil;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 27/5/21
 */
public abstract class AbstractDialog extends Dialog {

    private H3 header;
    private final List<Component> content = new ArrayList<>();
    private final JustifiedButtonLayout buttonBar = new JustifiedButtonLayout();


    public AbstractDialog() {
        super();
        configure();
    }

    public AbstractDialog(Component... components) {
        this();
        add(components);
    }

    public AbstractDialog(String title) {
        this();
        setHeader(title);
    }

    public AbstractDialog(String title, String text) {
        this(title);
        setText(text);
    }


    public void open() {
        pack();
        super.open();
    }


    public JustifiedButtonLayout getButtonBar() { return buttonBar; }
    

    public void setHeader(String title) {
        header = new H3(title);
        UiUtil.removeTopMargin(header);
        UiUtil.setStyle(header, "margin-bottom", "20px");

    }


    public void setText(String text) {
        content.add(new Div(new Html("<p>" + text + "</p>")));
    }


    public void addComponent(Component... components) {
        content.addAll(List.of(components));
    }


    private void configure() {
        setWidth("500px");
        setCloseOnOutsideClick(false);
        setModal(true);
    }


    private void pack() {
        if (header != null) add(header);
        content.forEach(this::add);
        add(buttonBar);
    }

}
