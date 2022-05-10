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


import com.processdataquality.praeclarus.ui.util.UiUtil;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * @author Michael Adams
 * @date 25/11/21
 */
public class MessageDialog extends Dialog {

    private final HorizontalLayout _buttonBar = new HorizontalLayout();
    private final Div _text = new Div();

    private Button _confirm;
    private Button _reject;
    private Button _cancel;

    public MessageDialog(String title) {
        setWidth("500px");
        setModal(true);
        setCloseOnOutsideClick(false);
        setHeader(title);
        _buttonBar.getStyle().set("flex-wrap", "wrap");
        _buttonBar.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        add(_text, _buttonBar);
    }


    public void open() {
        initButtons();
        super.open();
    }


    public void setHeader(String title) {
        H3 header = new H3(title);
        UiUtil.removeTopMargin(header);
        UiUtil.setStyle(header, "margin-bottom", "20px");
        add(new Div(header));
    }


    public void setText(String text) {
        _text.add(new Html("<p>" + text + "</p>"));
    }


    public void addButton(Button b) {
        _buttonBar.add(b);
        b.addClickListener(e -> close());
    }


    public Button addConfirmButton(Button b) {
        b.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        b.addClickListener(e -> close());
        _confirm = b;
        return b;
    }


    public Button addConfirmButton(String text) {
        return addConfirmButton(new Button(text));
    }


    public Button addConfirmButton() {
        return addConfirmButton(new Button("Confirm"));
    }

    
    public Button addRejectButton(Button b) {
        b.addThemeVariants(ButtonVariant.LUMO_ERROR);
        b.addClickListener(e -> close());
        _reject = b;
        return b;
    }


    public Button addRejectButton(String text) {
        return addRejectButton(new Button(text));
    }


    public Button addRejectButton() {
        return addRejectButton(new Button("Reject"));
    }


    public Button  addCancelButton(Button b) {
        b.getStyle().set("margin-inline-end", "auto");
        b.addClickListener(e -> close());
        _cancel = b;
        return b;
    }


    public Button addCancelButton(String text) {
        return addCancelButton(new Button(text));
    }


    public Button addCancelButton() {
        return addCancelButton(new Button("Cancel"));
    }


    private void initButtons() {
        if (_cancel != null) _buttonBar.add(_cancel);
        if (_reject != null) _buttonBar.add(_reject);
        if (_confirm != null) _buttonBar.add(_confirm);
    }

}
