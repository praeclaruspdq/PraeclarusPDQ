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

package com.processdataquality.praeclarus.config;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 16/6/2022
 */

//@Configuration
public class TomcatConfig {

    private static final Map<String, Context> loaded = new HashMap<>();

//    @Bean
//    @ConditionalOnProperty(name = "external.war.file")
    public TomcatServletWebServerFactory servletContainerFactory(
            @Value("${external.war.file}") String path,
            @Value("${external.war.context}") String contextPath) {

        return new TomcatServletWebServerFactory() {

            @Override
            protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {
                File webApps = new File(tomcat.getServer().getCatalinaBase(), "webapps");
                webApps.mkdirs();
                Context context = tomcat.addWebapp(contextPath, path);
                context.setParentClassLoader(getClass().getClassLoader());
//                try {
//                    File sourcePath = new File(path);
//                    FileUtils.copyFileToDirectory(sourcePath, webApps);
//
//                    Context context = tomcat.addWebapp(contextPath, new File(webApps,
//                            sourcePath.getName()).getAbsolutePath());
//                    context.setParentClassLoader(getClass().getClassLoader());
//                    loaded.put(contextPath, context);
//                }
//                catch (IOException ioe) {
//                    ioe.printStackTrace();
//                }

                return super.getTomcatWebServer(tomcat);
            }

        };
    }


    public void startWebapp(String contextPath) throws LifecycleException {
        Context context = loaded.get(contextPath);
        if (context != null) {
            context.start();
        }
    }


    public void stopWebapp(String contextPath) throws LifecycleException {
        Context context = loaded.get(contextPath);
        if (context != null) {
            context.stop();
        }
    }

}
