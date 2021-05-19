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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.Div;

/**
 * @author Michael Adams
 * @date 23/4/21
 */
@JavaScript("https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js")
public class MermaidCanvas extends Div {

    public MermaidCanvas() {
 //       this.setId("canvas");
        setClassName("mermaid");
        UI.getCurrent().getPage().executeJs("mermaid.initialize({startOnLoad:true});");

        getElement().setText(" graph LR\n" +
                "%%{config: { 'fontFamily': 'Menlo', 'fontSize': 48, 'fontWeight': 400} }%%\n" +
                "    A[Square Rect] -- Link text --> B((Circle))\n" +
                "    A --> C(Round Rect)\n" +
                "    B --> D{Rhombus}\n" +
                "    C --> D");
    }
}
