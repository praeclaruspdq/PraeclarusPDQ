/*
 * Copyright (c) 2021-2022 Queensland University of Technology
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

package com.processdataquality.praeclarus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {
        "com.processdataquality.praeclarus.logging.repository",
        "com.processdataquality.praeclarus.repo.graph",
        "com.processdataquality.praeclarus.repo.user",
        "com.processdataquality.praeclarus.ui.repo"
})
public class DqfApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
// These lines are required to enable ssl (and so https)
//        SpringApplication application = new SpringApplication(DqfApplication.class);
//        application.setAdditionalProfiles("ssl");
//        application.run(args);

        SpringApplication.run(DqfApplication.class, args);
    }

}
