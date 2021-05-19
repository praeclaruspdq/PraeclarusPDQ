package com.processdataquality.praeclarus.plugin;

/**
 * @author Michael Adams
 * @date 6/4/21
 */
public interface PDQPlugin {
    
    /**
     * @return A map of configuration parameters for the plugin.
     */
    Options getOptions();

    /**
     * Sets the configuration parameters for the plugin
     * @param options a map of configuration keys and values
     */
    void setOptions(Options options);

}
