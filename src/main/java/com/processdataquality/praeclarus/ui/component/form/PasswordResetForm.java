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

package com.processdataquality.praeclarus.ui.component.form;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.PasswordField;

public class PasswordResetForm extends FormLayout {

   private H3 title;

   private PasswordField password;
   private PasswordField passwordConfirm;

   private Span errorMessageField;

   private Button submitButton;


   public PasswordResetForm() {
       title = new H3("Reset Password");
       password = new PasswordField("Password");
       passwordConfirm = new PasswordField("Confirm password");
       password.setRequiredIndicatorVisible(true);
       passwordConfirm.setRequiredIndicatorVisible(true);

       errorMessageField = new Span();

       submitButton = new Button("Reset");
       submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

       add(title, password, passwordConfirm, errorMessageField, submitButton);

       // Max width of the Form
       setMaxWidth("400px");
   }

   public PasswordField getPasswordField() { return password; }

   public PasswordField getPasswordConfirmField() { return passwordConfirm; }

   public Span getErrorMessageField() { return errorMessageField; }

   public Button getSubmitButton() { return submitButton; }

}