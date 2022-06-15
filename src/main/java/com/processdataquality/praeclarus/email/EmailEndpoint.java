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

package com.processdataquality.praeclarus.email;

import com.processdataquality.praeclarus.repo.user.ConfirmationToken;
import com.processdataquality.praeclarus.repo.user.ConfirmationTokenRepository;
import com.processdataquality.praeclarus.repo.user.UserRepository;
import com.processdataquality.praeclarus.security.user.PdqUser;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.QueryParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author Michael Adams
 * @date 8/6/2022
 */
@Service
public class EmailEndpoint {

    private UserRepository userRepository;
    private ConfirmationTokenRepository tokenRepository;


    public EmailEndpoint(UserRepository userRepository, ConfirmationTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }


    @RequestMapping(value="/confirm-reset", method= { RequestMethod.GET, RequestMethod.POST})
    public void validateResetToken(@RequestParam("token")String confirmationToken) {
        ConfirmationToken token = tokenRepository.findByConfirmationToken(confirmationToken);
        if (token != null) {
            PdqUser user = userRepository.findByEmailIgnoreCase(token.getUser().getEmail());
            QueryParameters params = QueryParameters.simple(Map.of("username", user.getUsername()));
            UI.getCurrent().navigate("reset", params);
        }
    }
}
