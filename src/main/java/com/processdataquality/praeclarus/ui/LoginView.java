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

import com.processdataquality.praeclarus.ui.component.form.ForgotPasswordHandler;
import com.processdataquality.praeclarus.ui.util.UiUtil;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

/**
 * @author Michael Adams
 * @date 26/5/2022
 */
@Route("login")
@PageTitle("Login - Praeclarus PDQ")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    public LoginView(ForgotPasswordHandler forgotPasswordHandler){
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        loginForm.setAction("login");
        add(UiUtil.getLargeLogo(), new H1("Praeclarus PDQ"), loginForm,
                registrationLink());
        loginForm.addForgotPasswordListener(forgotPasswordHandler);
    }

    
    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        
        // inform the user about an authentication error
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }


    private RouterLink registrationLink() {
        return new RouterLink("New User? Register Here", RegistrationView.class);
    }

}
