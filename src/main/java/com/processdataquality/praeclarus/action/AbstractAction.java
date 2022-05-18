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

package com.processdataquality.praeclarus.action;

import com.processdataquality.praeclarus.exception.InvalidOptionValueException;
import com.processdataquality.praeclarus.plugin.AbstractPlugin;
import tech.tablesaw.api.Table;

import java.util.List;

/**
 * @author Michael Adams
 * @date 21/5/21
 */
public abstract class AbstractAction extends AbstractPlugin {

    protected AbstractAction() {
        super();
    }

    public abstract Table run(List<Table> inputSet) throws InvalidOptionValueException;
}
