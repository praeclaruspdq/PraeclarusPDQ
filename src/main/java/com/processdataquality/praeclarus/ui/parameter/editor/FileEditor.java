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

package com.processdataquality.praeclarus.ui.parameter.editor;

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.plugin.PluginParameter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

/**
 * @author Michael Adams
 * @date 5/5/21
 */
@CssImport("./styles/pdq-styles.css")
public class FileEditor extends AbstractEditor {

    private TextField tf;
    private FileBuffer buffer;


    public FileEditor(PDQPlugin plugin, PluginParameter param) {
        super(plugin, param);
    }


    protected HorizontalLayout createField(PluginParameter param) {

//        Upload u = new Upload();
//        u.setAutoUpload(false);
//        u.setDropAllowed(false);
//        u.addSucceededListener(e -> {
//            System.out.println(e.getFileName());
//        });
//
//        Button b = new Button(VaadinIcon.FOLDER_OPEN_O.create());
//        b.addClickListener(e -> {
//            this.add(new Html("<input type='file' style='position: fixed; top: -100em'>"));
////            UI.getCurrent().getPage().executeJs(
////                    "var input = document.createElement('input');" +
////                            " input.setAttribute('type', 'file');" +
////                            " input.click;" +
////                            " input.onchange = function () {" +
////                            "  alert('Selected file: ' + this.value);" +
////                            "};");
//        });

        tf = new TextField();
    //    tf.setWidth("75%");
        tf.setValue(param.getStringValue());
        tf.setReadOnly(true);

        return new HorizontalLayout(tf, createUpload());

    }


    private Upload createUpload() {
        buffer = new FileBuffer();
        Upload upload = new Upload(buffer);
//        upload.setAutoUpload(false);
        upload.setDropAllowed(false);
        upload.setDropLabelIcon(null);
        upload.setDropLabel(null);
        upload.setUploadButton(new Button(VaadinIcon.FOLDER_OPEN_O.create()));
        upload.addClassName("hide-file-upload-bar");
//        upload.getStyle().set("display", "none");
     //   upload.getElement().callJsFunction("uploadFiles");
        upload.addFinishedListener(e -> {
 //           InputStream inputStream = buffer.getInputStream();
            tf.setValue(buffer.getFileName());
            // read the contents of the buffered memory
            // from inputStream
        });
        return upload;
    }


    private void upload(PDQPlugin plugin) {
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setMaxFiles(1);
        upload.setDropAllowed(false);
        upload.setAcceptedFileTypes("text/csv");
//        upload.setReceiver( new Upload() {
//   			@Override
//   			public OutputStream receiveUpload(String filename, String mimeType) {
//                               //your code here
//   			}
//   		} );
//        upload.setReceiver();
        ((Button) upload.getUploadButton()).click();

//        upload.addSucceededListener(event -> {
//            String fileName = buffer.getFileName();  buffer.g
//        });
//
//        Div output = new Div();
//        upload.addFileRejectedListener(event -> {
//            Paragraph component = new Paragraph();
//            showOutput(event.getErrorMessage(), component, output);
//        });
//        upload.getElement().addEventListener("file-remove", event -> {
//            output.removeAll();
//        });
//
//        add(upload, output);
    }


    //            e.getSource().getElement().executeJs(
    //                    "document.getElementById('file-input').click();");

    //                    "const fileInput = document.getElementById('file-input');" +
    //                        "fileInput('file-input').click();");
    //            e.getSource().getElement().executeJs("return inputselect()")
    //  //                           "fileInput.click();" +
    ////                            "document.getElementById('file-input').onchange = function(event) {" +
    ////                            "return document.getElementById('file-input').files[0];" +
    // //                           "    return selectedFile;" +
    ////
    //                    .then(Object.class, result -> {
    //                        if (result != null) {
    //                            System.out.println("type = " + result.getClass());
    //                            System.out.println("result is -> " + result.toString());
    //                        }});

    //                    "document.getElementById('file-input').click();" +
    //                            "document.getElementById('file-input').onchange = function(event) {" +
    //                            "   var fileList = document.getElementById('file-input').files;}");

}
