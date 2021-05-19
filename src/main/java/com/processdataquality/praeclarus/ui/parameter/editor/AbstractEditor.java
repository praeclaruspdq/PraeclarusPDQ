package com.processdataquality.praeclarus.ui.parameter.editor;

import com.processdataquality.praeclarus.plugin.Options;
import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.plugin.PluginParameter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * @author Michael Adams
 * @date 5/5/21
 */
@CssImport("./styles/pdq-styles.css")
public abstract class AbstractEditor extends HorizontalLayout {

    private final PDQPlugin _plugin;


    public AbstractEditor(PDQPlugin plugin, PluginParameter param) {
        super();
        _plugin = plugin;
        add(createLabel(param), createField(param));
        setWidth("100%");
        setMargin(false);
    }


    protected abstract Component createField(PluginParameter param);


    private Label createLabel(PluginParameter param) {
        Label l = new Label(param.getName());
        l.setWidth("25%");
        return l;
    }

    
    protected void updateProperties(PluginParameter parameter) {
        Options options = _plugin.getOptions();
        options.put(parameter.getName(), parameter.getValue());
        _plugin.setOptions(options);
    }

}
