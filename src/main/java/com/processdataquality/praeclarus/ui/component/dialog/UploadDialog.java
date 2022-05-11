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


import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

/**
 * @author Michael Adams
 * @date 27/5/21
 */
public class UploadDialog extends AbstractDialog {

    public UploadDialog(UploadDialogListener listener, String[] mimeDescriptors) {
        super("Select File");
        MemoryBuffer buffer = new MemoryBuffer();

        Button ok = new Button("OK", event -> {
            listener.dialogClosed(new UploadDialogCloseEvent(true,
                    buffer.getInputStream(), buffer.getFileName()));
            close();
        });
        ok.setEnabled(false);
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Upload upload = new Upload(buffer);
        Div outputMsg = new Div();
        
        upload.setAcceptedFileTypes(mimeDescriptors);
        upload.addSucceededListener(event -> ok.setEnabled(true));
        
        upload.getElement().addEventListener("file-remove",
                event -> outputMsg.removeAll());

        upload.addFileRejectedListener(event -> {
            outputMsg.removeAll();
            HtmlComponent p = new HtmlComponent(Tag.P);
            p.getElement().setText(event.getErrorMessage());
            outputMsg.add(p);
            ok.setEnabled(false);
        });

        Button cancel = new Button("Cancel", event -> {
            listener.dialogClosed(new UploadDialogCloseEvent(false,null, null));
            close();
        });

        addComponent(upload, outputMsg);
        getButtonBar().add(cancel, ok);
    }

}
