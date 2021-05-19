package com.processdataquality.praeclarus.ui.parameter.editor;

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.plugin.PluginParameter;
import com.vaadin.flow.component.textfield.NumberField;

/**
 * @author Michael Adams
 * @date 5/5/21
 */

public class NumberEditor extends AbstractEditor {

    public NumberEditor(PDQPlugin plugin, PluginParameter param) {
        super(plugin, param);
    }


    protected NumberField createField(PluginParameter param) {
        NumberField field = new NumberField();
        field.setWidth("75%");
        field.setHasControls(true);
        field.setValue((double) param.getValue());
        field.addValueChangeListener(e -> {
            param.setValue(e.getValue());
           updateProperties(param);
        });
        return field;
    }


}
