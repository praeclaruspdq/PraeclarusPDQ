/*
 * Copyright (c) 2021 Queensland University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.processdataquality.praeclarus.ui.component;

import com.processdataquality.praeclarus.plugin.Options;
import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.plugin.PluginParameter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import java.util.*;

/**
 * @author Michael Adams
 * @date 16/4/21
 */

public class PropertiesPanel extends VerticalLayout {

//    private final Grid<PluginParameter> _grid;
    private PDQPlugin _plugin;
//    private FormLayout _form = null;
    private VerticalLayout _form = null;

    public PropertiesPanel() {
        add(new H3("Parameters"));
 //       _grid = createGrid();
 //       add(_grid);
        setSizeFull();
    }


    public void setPlugin(PDQPlugin plugin) {
        if (_form != null) {
            remove(_form);
        }
        _plugin = plugin;
        _form = createForm();
        add(_form);
        _form.setSizeFull();
 //       populateGrid();
    }


//    private FormLayout createForm() {
//        FormLayout form = new FormLayout();
//        form.setResponsiveSteps(
//                new FormLayout.ResponsiveStep("1px", 1));
//        Map<String, Object> map = _plugin.getParameters();
//        List<PluginParameter> paramList = new ArrayList<>();
//        for (String key : map.keySet()) {
//            PluginParameter param = new PluginParameter(key, map.get(key), null);
//            paramList.add(new PluginParameter(key, map.get(key), null));
//            TextField tf = new TextField();
//            tf.setValue(String.valueOf(map.get(key)));
//            tf.addValueChangeListener(e -> {
//                param.setStringValue(e.getValue());
//               updateProperties(param);
//            });
//            form.addFormItem(tf, key);
//        }
//        return form;
//    }

    private VerticalLayout createForm() {
        VerticalLayout form = new VerticalLayout();

        Options options = _plugin.getOptions();
        List<PluginParameter> paramList = new ArrayList<>();
        for (String key : options.keySet()) {
            PluginParameter param = new PluginParameter(key, options.get(key), null);
            form.add(param.editor(_plugin));
//            paramList.add(new PluginParameter(key, options.get(key), null));
//            TextField tf = new TextField();
//            tf.setWidth("100%");
//            tf.setValue(String.valueOf(options.get(key)));
//            tf.addValueChangeListener(e -> {
//                param.setStringValue(e.getValue());
//               updateProperties(param);
//            });
//
//            Label l = new Label(key);
//            l.setWidth("25%");
//            HorizontalLayout hl = new HorizontalLayout(l, tf);
//            hl.setWidth("100%");
//            hl.setMargin(false);
//
//            form.add(hl);
        }
        return form;
    }



    private Grid<PluginParameter> createGrid() {
        Grid<PluginParameter> grid = new Grid<>();
        grid.addColumn(PluginParameter::getName).setHeader("Parameter");
        Grid.Column<PluginParameter> valColumn = grid
                .addColumn(PluginParameter::getValue).setHeader("Value");

        configEditor(grid, valColumn);
        addEditButtonColumn(grid);
        return grid;
    }


    private void configEditor(Grid<PluginParameter> grid,
                              Grid.Column<PluginParameter> valColumn) {
        Binder<PluginParameter> binder = new Binder<>(PluginParameter.class);
        Editor<PluginParameter> editor = grid.getEditor();
        editor.setBinder(binder);
        editor.setBuffered(true);

        TextField valField = new TextField();
        binder.forField(valField).bind("stringValue");
//                .withConverter(
//                        new StringToIntegerConverter("Age must be a number."))
//                .withStatusLabel(validationStatus).bind("age");
        valColumn.setEditorComponent(valField);
    }


    private void addEditButtonColumn(Grid<PluginParameter> grid) {
        Editor<PluginParameter> editor = grid.getEditor();
        Collection<Button> editButtons = Collections.newSetFromMap(new WeakHashMap<>());

        Grid.Column<PluginParameter> editorColumn = grid.addComponentColumn(item -> {
            Button edit = new Button(styleIcon(VaadinIcon.PENCIL.create(), null));
            edit.addClassName("edit");
            edit.addClickListener(e -> {
                editor.editItem(item);
                ((TextField) editor.getGrid().getColumns().get(1).getEditorComponent()).focus();
            });
            edit.setEnabled(!editor.isOpen());
            editButtons.add(edit);
            return edit;
        });

        editor.addOpenListener(e -> editButtons.stream()
                .forEach(button -> button.setEnabled(!editor.isOpen())));
        editor.addCloseListener(e -> editButtons.stream()
                .forEach(button -> button.setEnabled(!editor.isOpen())));

        Icon saveIcon = styleIcon(VaadinIcon.CHECK.create(), "green");
        Icon cancelIcon = styleIcon(VaadinIcon.CLOSE.create(), "red");

        Button save = new Button(saveIcon, e -> {
            updateProperties(editor.getItem());
            editor.save();
        });
        save.addClassName("save");

        Button cancel = new Button(cancelIcon, e -> editor.cancel());
        cancel.addClassName("cancel");

        Div buttons = new Div(save, cancel);
        editorColumn.setEditorComponent(buttons);
    }

//    private void populateGrid() {
//        Map<String, Object> map = _plugin.getParameters();
//        List<PluginParameter> paramList = new ArrayList<>();
//        for (String key : map.keySet()) {
//            paramList.add(new PluginParameter(key, map.get(key), null));
//        }
//        _grid.setItems(paramList);
//        _grid.getDataProvider().refreshAll();
//    }

    private void updateProperties(PluginParameter parameter) {
        Options options = _plugin.getOptions();
        options.put(parameter.getName(), parameter.getValue());
        _plugin.setOptions(options);
    }

    private Icon styleIcon(Icon icon, String colour) {
        icon.setSize("24px");
        if (colour != null) icon.setColor(colour);
        return icon;
    }


}
