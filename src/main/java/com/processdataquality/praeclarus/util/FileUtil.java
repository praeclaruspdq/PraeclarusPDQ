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

package com.processdataquality.praeclarus.util;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Michael Adams
 * @date 2/6/21
 */
public class FileUtil {

    public static File stringToTempFile(String contents) {
        try {
            return stringToFile(
                    File.createTempFile(
                            RandomStringUtils.randomAlphanumeric(12), null), contents);
        } catch (IOException e) {
            return null;
        }
    }


    public static File stringToFile(File f, String contents) {
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(f));
            buf.write(contents, 0, contents.length());
            buf.close();
        } catch (IOException ioe) {
            f = null;
        }
        return f;
    }

}
