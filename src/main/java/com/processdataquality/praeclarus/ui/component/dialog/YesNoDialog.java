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

package com.processdataquality.praeclarus.ui.component.dialog;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;

/**
 * @author Michael Adams
 * @date 27/5/21
 */
public class YesNoDialog extends AbstractDialog {

    private final Button yesBtn = createYesButton();
    private final Button noBtn = new Button("No", event -> close());

    public YesNoDialog(String title, String text) {
        super(title, text);
        getButtonBar().add(noBtn, yesBtn);
    }


    // these are called to add listeners
    public Button getYesButton() { return yesBtn; }

    public Button getNoButton() { return noBtn; }


    private Button createYesButton() {
        Button yes = new Button("Yes", event -> close());
        yes.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return yes;
    }

}
