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

package com.processdataquality.praeclarus.ui.component.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * @author Michael Adams
 * @date 26/5/21
 */
public class VerticalScrollLayout extends VerticalLayout {
    private VerticalLayout content;

    public VerticalScrollLayout(){
        preparePanel();
    }

    public VerticalScrollLayout(Component... children){
        preparePanel();
        this.add(children);
    }

    private void preparePanel() {
        setWidth("100%");
        setHeight("100%");
        getStyle().set("overflow", "auto");

        content = new VerticalLayout();
        content.getStyle().set("display", "block");
        content.setWidth("100%");
        content.setPadding(false);
        super.add(content);
    }

    public VerticalLayout getContent(){
        return content;
    }

    @Override
    public void add(Component... components){
        content.add(components);
    }

    @Override
    public void remove(Component... components){
        content.remove(components);
    }

    @Override
    public void removeAll(){
        content.removeAll();
    }

    @Override
    public void addComponentAsFirst(Component component) {
        content.addComponentAtIndex(0, component);
    }
}
