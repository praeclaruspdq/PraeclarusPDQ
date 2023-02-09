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
public class UIContainer implements UIComponent {

    public enum Orientation {VERTICAL, HORIZONTAL}

    private final Orientation _orientation;
    private final List<UIComponent> _components = new ArrayList<>();

    public UIContainer() {
        this(Orientation.VERTICAL);
    }

    public UIContainer(Orientation orientation) {
        _orientation = orientation;
    }

    public void add(UIComponent c) {
        _components.add(c);
    }

    public Orientation getOrientation() { return _orientation; }

    public List<UIComponent> getComponents() { return _components; }
}
