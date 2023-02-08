/*
 * Copyright (c) 2022 Queensland University of Technology
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

package com.processdataquality.praeclarus.ui.parameter.editor;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.processdataquality.praeclarus.option.ColumnNameListAndStringOption;
import com.processdataquality.praeclarus.option.ColumnNameListOption;
import com.processdataquality.praeclarus.option.HasOptions;
import com.processdataquality.praeclarus.option.Option;
import com.processdataquality.praeclarus.support.math.Pair;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;

/**
 * @author Sareh Sadeghianasl
 * @date 8/2/23
 */

public class ListAndStringEditor extends AbstractEditor {

	public ListAndStringEditor(HasOptions container, Option option) {
		super(container, option);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Component createField() {
		Pair<List<String>, String> value = (Pair<List<String>, String>) getOption().value();
		List<String> items = (List<String>) value.getKey();
		HorizontalLayout component = new HorizontalLayout();

		Select<String> field1 = new Select<>();
		H6 field2 = new H6(" as ");
		TextField field3 = new TextField();
		field1.setWidth("55%");
		field2.setWidth("10%");
		field3.setWidth("35%");

		field1.setItems(items);
		if (!items.isEmpty()) {
			String selection = String.valueOf(((ColumnNameListAndStringOption) getOption()).getSelected().getKey());
			String name = String.valueOf(((ColumnNameListAndStringOption) getOption()).getSelected().getValue());
			boolean selectCol = !StringUtils.isEmpty(selection) && items.contains(selection);
			boolean selectName = name != null && !StringUtils.isEmpty(name);
			if (selectCol) {
				field1.setValue(selection);
				if (selectName) {
					field3.setValue(name);
					((ColumnNameListAndStringOption) getOption())
							.setSelected(new Pair<String, String>(selection, name));
				}
			} else {
				field1.setValue(items.get(0));
				if (selectName) {
					((ColumnNameListAndStringOption) getOption())
							.setSelected(new Pair<String, String>(items.get(0), name));
					field3.setValue(name);
				} else {
					((ColumnNameListAndStringOption) getOption())
							.setSelected(new Pair<String, String>(items.get(0), ""));
				}
			}

		}

		// user has chosen a value
		field1.addValueChangeListener(
				e -> ((ColumnNameListAndStringOption) getOption()).setSelected(new Pair<String, String>(e.getValue(),
						((ColumnNameListAndStringOption) getOption()).getSelected().getValue())));
		field3.addValueChangeListener(
				e -> ((ColumnNameListAndStringOption) getOption()).setSelected(new Pair<String, String>(
						((ColumnNameListAndStringOption) getOption()).getSelected().getKey(), e.getValue())));

		component.add(field1, field2, field3);
		component.setWidth("75%");

		return component;
	}

}
