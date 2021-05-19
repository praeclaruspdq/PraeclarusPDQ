package com.processdataquality.praeclarus.annotations;

import com.processdataquality.praeclarus.pattern.PatternGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Michael Adams
 * @date 30/3/21
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginMetaData {
    String name();
    String author();
    String synopsis();
    String description() default "";
    String version() default "0.1";
    PatternGroup group() default PatternGroup.NONE;
}
