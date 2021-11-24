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

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * @author Michael Adams
 * @date 23/11/21
 */
public class SaveExistingDialog extends Dialog {

    public enum CLICKED { SAVE, DISCARD, CANCEL }


    public SaveExistingDialog(SaveExistingListener listener) {
        H4 title = new H4("Save changes to existing workflow?");
        HtmlComponent p = new HtmlComponent(Tag.P);
        p.getElement().setText("Click 'Save' to save changes, " +
                "'Discard' to discard changes, 'Cancel' to keep working.");

        Button ok = new Button("Save", e -> close(listener, CLICKED.SAVE));
        Button discard = new Button("Discard", e -> close(listener, CLICKED.DISCARD));
        Button cancel = new Button("Cancel", e -> close(listener, CLICKED.CANCEL));
        
        add(new Div(title), new Div(p), new HorizontalLayout(ok, discard, cancel));
    }
    

    private void close(SaveExistingListener listener, CLICKED clicked) {
        listener.saveExistingDialogEvent(clicked);
        close();
    }
}
