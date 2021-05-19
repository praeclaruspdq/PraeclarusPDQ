package com.processdataquality.praeclarus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Michael Adams
 * @date 14/4/21
 */

@Configuration
@ConfigurationProperties(prefix = "plugin")
public class PluginConfig {

      private List<String> pathList;

    public List<String> getPathList() {
        return pathList;
    }

    public void setPathList(List<String> pathList) {
        this.pathList = pathList;
    }
}
