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

import java.io.InputStream;

/**
 * @author Michael Adams
 * @date 15/10/21
 */
public class UploadDialogCloseEvent {

    public boolean successful;
    public InputStream inputStream;
    public String fileName;

    public UploadDialogCloseEvent(boolean b, InputStream is, String fn) {
        successful = b;
        inputStream = is;
        fileName = fn;
    }
}
