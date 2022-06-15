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

import com.processdataquality.praeclarus.repo.user.ConfirmationToken;
import com.processdataquality.praeclarus.repo.user.ConfirmationTokenRepository;
import com.processdataquality.praeclarus.repo.user.UserRepository;
import com.processdataquality.praeclarus.security.user.PdqUser;
import com.processdataquality.praeclarus.ui.component.announce.Announcement;
import com.processdataquality.praeclarus.ui.component.form.PasswordResetForm;
import com.processdataquality.praeclarus.ui.component.form.PasswordResetFormBinder;
import com.processdataquality.praeclarus.ui.util.UiUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.List;
import java.util.Map;

@AnonymousAllowed
@Route("confirm-reset")
@PageTitle("Reset Password - Praeclarus PDQ")
public class PasswordResetView extends VerticalLayout implements HasUrlParameter<String> {

    private UserRepository userRepository;
    private ConfirmationTokenRepository tokenRepository;
    private PasswordResetFormBinder formBinder;

    public PasswordResetView(UserRepository userRepository,
                             ConfirmationTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        PasswordResetForm form = new PasswordResetForm();
        setSizeFull();
        setHorizontalComponentAlignment(Alignment.CENTER, form);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        add(UiUtil.getLargeLogo(), new H1("Praeclarus PDQ"), form);

        formBinder = new PasswordResetFormBinder(form, userRepository);
        formBinder.addBindingAndValidation();
    }


    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        Location location = event.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        Map<String, List<String>> parametersMap = queryParameters.getParameters();
        List<String> tokenList = parametersMap.get("token");
        if (! (tokenList == null || tokenList.isEmpty())) {
            String token = tokenList.get(0);

            ConfirmationToken confirmToken = tokenRepository.findByConfirmationToken(token);
            if (confirmToken != null) {
                PdqUser user = userRepository.findByEmailIgnoreCase(confirmToken.getUser().getEmail());
                tokenRepository.delete(confirmToken);             // one time use
                if (user != null) {
                    formBinder.setUser(user);
                    return;
                }
            }
        }
        Announcement.error("Invalid reset token");
        UI.getCurrent().navigate("login");
    }

}