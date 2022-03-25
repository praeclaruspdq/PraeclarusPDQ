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

package com.processdataquality.praeclarus.option;

import com.processdataquality.praeclarus.exception.InvalidOptionException;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Michael Adams
 * @date 25/3/2022
 */
public class OptionsUtils {

    public static String getSelectedListValue(Options options, String key)
            throws InvalidOptionException {
        Option option = options.getNotNull(key);
        String value = (option instanceof ListOption<?>) ?
                (String) ((ListOption<?>) option).getSelected() :
                option.asString();
        if (StringUtils.isEmpty(value)) {
            throw new InvalidOptionException("No value for " + key + " specified");
        }
        return value;
    }

}
