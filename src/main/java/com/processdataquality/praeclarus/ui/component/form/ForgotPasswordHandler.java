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

import com.processdataquality.praeclarus.email.EmailSenderService;
import com.processdataquality.praeclarus.repo.user.ConfirmationToken;
import com.processdataquality.praeclarus.repo.user.ConfirmationTokenRepository;
import com.processdataquality.praeclarus.repo.user.UserRepository;
import com.processdataquality.praeclarus.security.user.PdqUser;
import com.processdataquality.praeclarus.ui.component.announce.Announcement;
import com.processdataquality.praeclarus.ui.component.dialog.EmailAddressDialog;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.login.AbstractLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

/**
 * @author Michael Adams
 * @date 7/6/2022
 */
@Component
public class ForgotPasswordHandler implements
        ComponentEventListener<AbstractLogin.ForgotPasswordEvent> {


    private UserRepository userRepository;


    private ConfirmationTokenRepository tokenRepository;


    private EmailSenderService emailSenderService;


    @Autowired
    public void setUserRepository(UserRepository repo) {
        userRepository = repo;
    }

    @Autowired
    public void setTokenRepository(ConfirmationTokenRepository repo) {
        tokenRepository = repo;
    }

    @Autowired
    public void setEmailService(EmailSenderService service) {
        emailSenderService = service;
    }

    
    @Override
    public void onComponentEvent(AbstractLogin.ForgotPasswordEvent forgotPasswordEvent) {
        EmailAddressDialog dialog = new EmailAddressDialog();
        dialog.getOKButton().addClickListener(e -> processAddress(dialog.getAddress()));
        dialog.open();
    }


    private void processAddress(String address) {
        PdqUser user = userRepository.findByEmailIgnoreCase(address);
        if (user != null) {
            ConfirmationToken token = new ConfirmationToken(user);
            tokenRepository.save(token);

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(address);
            mailMessage.setSubject("Verify Praeclarus Password Reset Request");
            mailMessage.setFrom("preclaruspdq@gmail.com");
            mailMessage.setText("To complete the password reset process, please click here: "
                    + "http://localhost:8080/confirm-reset?token="
                    + token.getConfirmationToken());
            emailSenderService.sendEmail(mailMessage);

            Announcement.success("Password reset email sent to " + address);
        }
        else {
            Announcement.error("Unregistered email address");
        }
    }
}
