package com.processdataquality.praeclarus.ui.parameter.editor;

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.plugin.PluginParameter;
import com.vaadin.flow.component.textfield.IntegerField;

/**
 * @author Michael Adams
 * @date 5/5/21
 */

public class IntEditor extends AbstractEditor {

    public IntEditor(PDQPlugin plugin, PluginParameter param) {
        super(plugin, param);
    }


    protected IntegerField createField(PluginParameter param) {
        IntegerField field = new IntegerField();
        field.setHasControls(true);
        field.setWidth("75%");
        field.setValue((int) param.getValue());
        field.addValueChangeListener(e -> {
            param.setValue(e.getValue());
           updateProperties(param);
        });
        return field;
    }


}
