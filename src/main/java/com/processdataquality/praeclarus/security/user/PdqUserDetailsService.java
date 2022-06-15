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

package com.processdataquality.praeclarus.security.user;

import com.processdataquality.praeclarus.logging.EventLogger;
import com.processdataquality.praeclarus.repo.user.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author Michael Adams
 * @date 27/5/2022
 */
@Service
public class PdqUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    public PdqUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        PdqUser user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return new PdqUserPrincipal(user);
    }


    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        Object principal = success.getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            userRepository.updateLastLogin(username, LocalDateTime.now());
            EventLogger.logonSuccessEvent(username);
        }
    }


    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failures) {
        String username = failures.getAuthentication().getName();
        if (username != null) {
            EventLogger.logonFailEvent(username, failures.getException().getMessage());
        }
    }

}
