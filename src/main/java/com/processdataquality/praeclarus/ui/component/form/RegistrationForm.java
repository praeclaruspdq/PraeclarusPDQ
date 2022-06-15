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

import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;

import java.util.stream.Stream;

public class RegistrationForm extends FormLayout {

   private H3 title;

   private TextField firstname;
   private TextField lastname;

   private EmailField email;

   private TextField username;
   private PasswordField password;
   private PasswordField passwordConfirm;

   private Span errorMessageField;

   private Button submitButton;


   public RegistrationForm() {
       title = new H3("User Registration");
       firstname = new TextField("First name");
       lastname = new TextField("Last name");
       email = new EmailField("Email");
       username = new TextField("User name");
       password = new PasswordField("Password");
       passwordConfirm = new PasswordField("Confirm password");
       
       setRequiredIndicatorVisible(firstname, lastname, email, username, password,
               passwordConfirm);

       errorMessageField = new Span();

       submitButton = new Button("Join the community");
       submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

       add(title, firstname, lastname, username, email, password, passwordConfirm,
               errorMessageField, submitButton);

       // Max width of the Form
       setMaxWidth("500px");

       // Allow the form layout to be responsive.
       // On device widths 0-490px we have one column.
       // Otherwise, we have two columns.
       setResponsiveSteps(
               new ResponsiveStep("0", 1, ResponsiveStep.LabelsPosition.TOP),
               new ResponsiveStep("490px", 2, ResponsiveStep.LabelsPosition.TOP));

       // These components always take full width
       setColspan(title, 2);
       setColspan(errorMessageField, 2);
       setColspan(submitButton, 2);
   }

   public TextField getUsernameField() { return username; }

   public PasswordField getPasswordField() { return password; }

   public PasswordField getPasswordConfirmField() { return passwordConfirm; }

   public Span getErrorMessageField() { return errorMessageField; }

   public Button getSubmitButton() { return submitButton; }

   private void setRequiredIndicatorVisible(HasValueAndElement<?, ?>... components) {
       Stream.of(components).forEach(comp -> comp.setRequiredIndicatorVisible(true));
   }

}