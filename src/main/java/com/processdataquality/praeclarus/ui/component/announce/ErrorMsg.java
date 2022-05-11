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

package com.processdataquality.praeclarus.ui.component.announce;


import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * @author Michael Adams
 * @date 16/3/2022
 */
public class ErrorMsg extends Notification {
    
    public ErrorMsg(String msg) {
        super();
        addThemeVariants(NotificationVariant.LUMO_ERROR);
        setPosition(Position.MIDDLE);

        Button closeButton = new Button(new Icon("lumo", "cross"));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        closeButton.getElement().setAttribute("aria-label", "Close");
        closeButton.addClickListener(event -> close());

        Div text = new Div(new Text(msg));
        Icon icon = VaadinIcon.WARNING.create();

        HorizontalLayout layout = new HorizontalLayout(icon, text, closeButton);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        add(layout);
    }


    public ErrorMsg(String msg, Position position) {
        this(msg);
        setPosition(position);
    }

}
