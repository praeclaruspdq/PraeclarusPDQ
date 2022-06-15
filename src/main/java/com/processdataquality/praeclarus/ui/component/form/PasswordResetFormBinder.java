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

import com.processdataquality.praeclarus.repo.user.UserRepository;
import com.processdataquality.praeclarus.security.user.PdqUser;
import com.processdataquality.praeclarus.ui.component.announce.Announcement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordResetFormBinder {

   private final PasswordResetForm resetForm;
   private final UserRepository repo;
   private PdqUser userBean;

   /**
    * Flag for disabling first run for password validation
    */
   private boolean enablePasswordValidation;


   public PasswordResetFormBinder(PasswordResetForm resetForm,
                                  UserRepository userRepository) {
       this.resetForm = resetForm;
       repo = userRepository;
   }


   public void setUser(PdqUser user) {  userBean = user; }


   /**
    * Method to add the data binding and validation logics
    * to the registration form
    */
   public void addBindingAndValidation() {
       BeanValidationBinder<PdqUser> binder = new BeanValidationBinder<>(PdqUser.class);
       binder.bindInstanceFields(resetForm);

       // A custom validator for password fields
       binder.forField(resetForm.getPasswordField())
               .withValidator(this::passwordValidator).bind("password");

       // The second password field is not connected to the Binder, but we
       // want the binder to re-check the password validator when the field
       // value changes. The easiest way is just to do that manually.
       resetForm.getPasswordConfirmField().addValueChangeListener(e -> {
           // The user has modified the second field, now we can validate and show errors.
           // See passwordValidator() for how this flag is used.
           enablePasswordValidation = true;

           binder.validate();
       });

       // Set the label where bean-level error messages go
       binder.setStatusLabel(resetForm.getErrorMessageField());

       // And finally the submit button
       resetForm.getSubmitButton().addClickListener(event -> {
           try {

               // Run validators and write the values to the bean
               binder.writeBean(userBean);

               // Typically, you would here call backend to store the bean
               userBean.setPassword(new BCryptPasswordEncoder().encode(userBean.getPassword()));
               repo.save(userBean);

               // Show success message if everything went well
               showSuccess();
           }
           catch (ValidationException exception) {
               // validation errors are already visible for each field,
               // and bean-level errors are shown in the status label.
               // We could show additional messages here if we want, do logging, etc.
           }
       });
   }

   /**
    * Method to validate that:
    * <p>
    * 1) Password is at least 8 characters long
    * <p>
    * 2) Values in both fields match each other
    */
   private ValidationResult passwordValidator(String pass1, ValueContext ctx) {
       /*
        * Just a simple length check. A real version should check for password
        * complexity as well!
        */

       if (pass1 == null || pass1.length() < 8) {
           return ValidationResult.error("Password should be at least 8 characters long");
       }

       if (!enablePasswordValidation) {
           // user hasn't visited the field yet, so don't validate just yet, but next time.
           enablePasswordValidation = true;
           return ValidationResult.ok();
       }

       String pass2 = resetForm.getPasswordConfirmField().getValue();

       if (pass1.equals(pass2)) {
           return ValidationResult.ok();
       }

       return ValidationResult.error("Passwords do not match");
   }


   /**
    * We call this method when form submission has succeeded
    */
   private void showSuccess() {

       // Redirect back to login page
       UI.getCurrent().navigate("login");
       Announcement.show("Password successfully reset. Please log in.");
   }

}