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


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.textfield.EmailField;

/**
 * @author Michael Adams
 * @date 27/5/21
 */
public class EmailAddressDialog extends AbstractDialog {

    private final EmailField field = new EmailField();
    private final Button ok = new Button("OK", event -> close());
    private final Button cancel = new Button("Cancel", event -> close());


    public EmailAddressDialog() {
        super("Please provide the email address for your account");
        addField();
        addButtons();
    }


    public Button getOKButton() { return ok; }

    public Button getCancelButton() { return cancel; }

    public String getAddress() { return field.getValue(); }


    private void addField() {
        field.setWidth("460px");
        field.getStyle().set("margin-bottom", "20px");
        addComponent(field);
    }


    private void addButtons() {
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getButtonBar().add(cancel, ok);
    }

}
