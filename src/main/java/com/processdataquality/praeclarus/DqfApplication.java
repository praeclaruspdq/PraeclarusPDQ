package com.processdataquality.praeclarus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@ConfigurationPropertiesScan("com.processdataquality.praeclarus.config")
public class DqfApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(DqfApplication.class, args);
    }

}
