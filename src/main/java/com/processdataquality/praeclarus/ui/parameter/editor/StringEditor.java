package com.processdataquality.praeclarus.ui.parameter.editor;

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.plugin.PluginParameter;
import com.vaadin.flow.component.textfield.TextField;

/**
 * @author Michael Adams
 * @date 5/5/21
 */

public class StringEditor extends AbstractEditor {

    public StringEditor(PDQPlugin plugin, PluginParameter param) {
        super(plugin, param);
    }


    protected TextField createField(PluginParameter param) {
        TextField tf = new TextField();
        tf.setWidth("75%");
        tf.setValue(param.getStringValue());
        tf.addValueChangeListener(e -> {
            param.setStringValue(e.getValue());
           updateProperties(param);
        });
        return tf;
    }

}
