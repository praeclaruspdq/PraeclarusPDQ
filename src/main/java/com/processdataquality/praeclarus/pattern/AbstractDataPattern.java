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

package com.processdataquality.praeclarus.pattern;

import com.processdataquality.praeclarus.plugin.AbstractPlugin;
import com.processdataquality.praeclarus.plugin.uitemplate.PluginUI;

/**
 * @author Michael Adams
 * @date 18/5/2022
 */
public abstract class AbstractDataPattern extends AbstractPlugin implements DataPattern {

    // The UI template used for the front-end interactions
    protected PluginUI _ui;

    
    protected AbstractDataPattern() {
        super();
    }


    @Override
    public PluginUI getUI() {
        return _ui;
    }

    @Override
    public void setUI(PluginUI ui) {
        _ui = ui;
    }

    /**
     * By default subclasses can detect, but they can override this as required
     * @return true (this plugin can detect an imperfection pattern)
     */
    @Override
    public boolean canDetect() {
        return true;
    }


    /**
     * By default subclasses can repair, but they can override this as required
     * @return true (this plugin can repair a log)
     */
    @Override
    public boolean canRepair() {
        return true;
    }

}
