package com.processdataquality.praeclarus.plugin;

import com.processdataquality.praeclarus.ui.parameter.editor.BooleanEditor;
import com.processdataquality.praeclarus.ui.parameter.editor.IntEditor;
import com.processdataquality.praeclarus.ui.parameter.editor.NumberEditor;
import com.processdataquality.praeclarus.ui.parameter.editor.StringEditor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * @author Michael Adams
 * @date 29/4/21
 */
public class PluginParameter {

    String _name;
    Object _value;
    String _synopsis;
    boolean _updated = false;

    public PluginParameter(String name, Object value, String synopsis) {
        _name = name;
        _value = value;
        _synopsis = synopsis;
    }

    public String getName() { return _name; }

    public Object getValue() { return _value; }

    public String getStringValue() { return String.valueOf(_value); }

    public String getSynopsis() { return _synopsis; }

    public boolean isUpdated() { return _updated; }


    public void setValue(Object value) {
        _value = value;
        _updated = true;
    }

    public void setStringValue(String value) {
        convertAndSet(value);
        _updated = true;
    }

    public HorizontalLayout editor(PDQPlugin plugin) {
        if (_value instanceof Boolean) {
            return new BooleanEditor(plugin, this);
        }
//        if (_name.equals("Source")) {
//            return new FileEditor(plugin, this);
//        }
//        else if (_value instanceof Character) {
//            _value = value.charAt(0);
//        }
        else if (_value instanceof Integer) {
            return new IntEditor(plugin, this);
        }
        else if (_value instanceof Double) {
            return new NumberEditor(plugin, this);
        }

        return new StringEditor(plugin, this);

    }

    private void convertAndSet(String value) {
        if (_value instanceof Boolean) {
            _value = Boolean.valueOf(value);
        }
        else if (_value instanceof Character) {
            _value = value.charAt(0);
        }
        else if (_value instanceof Integer) {
            _value = Integer.valueOf(value);
        }
        else if (_value instanceof Long) {
            _value = Long.valueOf(value);
        }
        else {
            _value = value;
        }
    }

}
