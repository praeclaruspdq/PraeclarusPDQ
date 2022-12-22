/*
 * Copyright (c) 2021-2022 Queensland University of Technology
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

package com.processdataquality.praeclarus.plugin.uitemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 1/11/21
 */
public class PluginUI {

    private final String _title;
    private final List<UIContainer> _containers = new ArrayList<>();

    public PluginUI(String title) {
        _title = title;
    }

    public void add(UIContainer l) {
        _containers.add(l);
    }

    public List<UIContainer> getContainers() {
        return _containers;
    }

    public String getTitle() { return _title; }


    public List<UITable> extractTables() {
        List<UITable> tables = new ArrayList<>();
        for (UIContainer container : _containers) {
            for (UIComponent component : container.getComponents()) {
                if (component instanceof UITable) {
                    tables.add((UITable) component);
                }
            }
        }
        return tables;
    }

}
