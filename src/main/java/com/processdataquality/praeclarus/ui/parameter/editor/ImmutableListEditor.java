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
import com.vaadin.flow.component.select.Select;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author Michael Adams
 * @date 27/10/21
 */
public class ImmutableListEditor extends AbstractEditor {

	public ImmutableListEditor(HasOptions container, Option option) {
		super(container, option);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Component createField() {
		List<String> items = (List<String>) getOption().value();

		Select<String> field = new Select<>();
		field.setItems(items);
		if (getOption() instanceof ColumnNameListOption) {
			if (!items.isEmpty()) {
				String selection = ((ColumnNameListOption) getOption()).getSelected();
				if (!StringUtils.isEmpty(selection) && items.contains(selection)) {
					field.setValue(selection);
				} else {
					field.setValue(items.get(0));
					((ColumnNameListOption) getOption()).setSelected(items.get(0));
				}
			}
		} else if (getOption() instanceof ListOption){
			if (!items.isEmpty()) {
				String selection = ((ListOption) getOption()).getSelected();
				if (!StringUtils.isEmpty(selection) && items.contains(selection)) {
					field.setValue(selection);
				} else {
					field.setValue(items.get(0));
					((ListOption) getOption()).setSelected(items.get(0));
				}
			}
		}
		field.setWidth("60%");
		// user has chosen a value
		field.addValueChangeListener(e -> updateListOption(e.getValue()));
		return field;
	}
	
	protected Label createLabel() {
        Label l = new Label(getOption().key());
        l.setWidth("40%");
        l.getElement().getStyle().set("font-size", "14px");
        return l;
    }

}
