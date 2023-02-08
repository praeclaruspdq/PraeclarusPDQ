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

import com.processdataquality.praeclarus.option.ColumnNameListOption;
import com.processdataquality.praeclarus.option.HasOptions;
import com.processdataquality.praeclarus.option.ListOption;
import com.processdataquality.praeclarus.option.Option;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

/**
 * An abstract class defining a single entry field for a property. Subclasses
 * define what type of field to create.
 *
 * @author Michael Adams
 * @date 5/5/21
 */
public abstract class AbstractEditor extends HorizontalLayout {

	private final HasOptions _container; // the plugin this field is an option for
	private final Option _option; // this field's underlying option

	// adds the label and field as a single component
	public AbstractEditor(HasOptions container, Option option) {
		super();
		_container = container;
		_option = option;
		add(createLabel(), createField());
		setWidth("100%");
		setMargin(false);
		getElement().getStyle().set("margin-top", "5px");
	}

	// to be implemented by sub-classes
	protected abstract Component createField();

	protected Label createLabel() {
		Label l = new Label(_option.key());
		l.setWidth("25%");
		l.getElement().getStyle().set("font-size", "14px");
		return l;
	}

	protected TextField initTextField() {
		TextField field = new TextField();

		String value = _option.asString();
		if (value.equals("null"))
			value = "";
		field.setValue(value);

		field.addValueChangeListener(e -> updateOption(e.getValue()));
		return field;
	}

	protected void updateOption(Object value) {
		_option.setValue(value);
		_container.getOptions().update(_option);
	}

	protected void updateListOption(String value) {
		if (_option instanceof ListOption) {
			((ListOption) _option).setSelected(value);
		}
		else if (_option instanceof ColumnNameListOption) {
			((ColumnNameListOption) _option).setSelected(value);
		}
		_container.getOptions().update(_option);
	}

	protected HasOptions getContainer() {
		return _container;
	}

	protected Option getOption() {
		return _option;
	}

}
