package com.processdataquality.praeclarus.ui.parameter.editor;

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.plugin.PluginParameter;
import com.vaadin.flow.component.checkbox.Checkbox;

/**
 * @author Michael Adams
 * @date 5/5/21
 */

public class BooleanEditor extends AbstractEditor {

    public BooleanEditor(PDQPlugin plugin, PluginParameter param) {
        super(plugin, param);
    }


    protected Checkbox createField(PluginParameter param) {
        Checkbox cb = new Checkbox();
        cb.setValue((boolean) param.getValue());
        cb.addValueChangeListener(e -> {
            param.setValue(e.getValue());
           updateProperties(param);
        });
        return cb;
    }

}
