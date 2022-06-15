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

package com.processdataquality.praeclarus.ui;

import com.processdataquality.praeclarus.repo.user.UserRepository;
import com.processdataquality.praeclarus.ui.component.form.RegistrationForm;
import com.processdataquality.praeclarus.ui.component.form.RegistrationFormBinder;
import com.processdataquality.praeclarus.ui.util.UiUtil;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
@Route("register")
@PageTitle("Register - Praeclarus PDQ")
public class RegistrationView extends VerticalLayout {

    public RegistrationView(UserRepository userRepository) {
        RegistrationForm registrationForm = new RegistrationForm();
        setSizeFull();
        setHorizontalComponentAlignment(Alignment.CENTER, registrationForm);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        add(UiUtil.getLargeLogo(), new H1("Praeclarus PDQ"), registrationForm);

        RegistrationFormBinder registrationFormBinder =
                new RegistrationFormBinder(registrationForm, userRepository);
        registrationFormBinder.addBindingAndValidation();
    }
}