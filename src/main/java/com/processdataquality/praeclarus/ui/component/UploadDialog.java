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
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

/**
 * @author Michael Adams
 * @date 27/5/21
 */
public class UploadDialog extends Dialog {


    public UploadDialog(UploadDialogListener listener, String[] mimeDescriptors) {
        MemoryBuffer _buffer = new MemoryBuffer();
        Upload _upload = new Upload(_buffer);
        Div _outputMsg = new Div();
        Button _ok = new Button("OK");
        H4 title = new H4("Select File");

        setCloseOnOutsideClick(false);
        setModal(true);

        _upload.setAcceptedFileTypes(mimeDescriptors);
        _upload.addSucceededListener(event -> _ok.setEnabled(true));
        
        _upload.getElement().addEventListener("file-remove",
                event -> _outputMsg.removeAll());

        _upload.addFileRejectedListener(event -> {
            _outputMsg.removeAll();
            HtmlComponent p = new HtmlComponent(Tag.P);
            p.getElement().setText(event.getErrorMessage());
            _outputMsg.add(p);
            _ok.setEnabled(false);
        });

        _ok.setEnabled(false);
        _ok.addClickListener(event -> {
            listener.dialogClosed(new UploadDialogCloseEvent(true,
                    _buffer.getInputStream(), _buffer.getFileName()));
            close();
        });

        Button cancel = new Button("Cancel", event -> {
            listener.dialogClosed(new UploadDialogCloseEvent(false,null, null));
            close();
        });

        add(title, _upload, _outputMsg, new HorizontalLayout(_ok, cancel));
    }

}
