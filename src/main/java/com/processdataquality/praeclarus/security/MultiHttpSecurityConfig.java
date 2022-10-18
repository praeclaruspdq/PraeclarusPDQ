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

package com.processdataquality.praeclarus.security;

import com.processdataquality.praeclarus.ui.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

/**
 * @author Michael Adams
 * @date 18/10/2022
 */
@EnableWebSecurity
@Configuration
public class MultiHttpSecurityConfig {


    // via REST API
    @Configuration
    @Order(1)
    public static class RestSecurityAdapter {

        @Bean
        public SecurityFilterChain filterChainApp1(HttpSecurity http) throws Exception {
            http.antMatcher("/api/**")
                    .authorizeRequests().anyRequest().authenticated().and().httpBasic();
            return http.build();
        }

        @Bean
        public AuthenticationEntryPoint authenticationEntryPoint(){
            BasicAuthenticationEntryPoint entryPoint =
                    new BasicAuthenticationEntryPoint();
            entryPoint.setRealmName("User");
            return entryPoint;
        }
    }



    // via browser login
    @Configuration
    @Order(2)
    public static class WebSecurityAdapter extends VaadinWebSecurity {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            setLoginView(http, LoginView.class);
        }


        /**
         * Allows access to static resources, bypassing Spring security.
         */
        @Override
        public void configure(WebSecurity web) throws Exception {
            web.ignoring().antMatchers("/icons/**");
            super.configure(web);
        }


        @Bean
        public PasswordEncoder passwordEncoder() {         //TODO
            return new BCryptPasswordEncoder();
        }


        @Bean
        public AuthenticationEventPublisher authenticationEventPublisher
                (ApplicationEventPublisher applicationEventPublisher) {
            return new DefaultAuthenticationEventPublisher(applicationEventPublisher);
        }

    }
}
