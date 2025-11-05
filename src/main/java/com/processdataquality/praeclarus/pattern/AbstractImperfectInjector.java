/*
 * Copyright (c) 2021-2022 Queensland University of Technology
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

import java.util.logging.Logger;

import com.processdataquality.praeclarus.exception.InvalidOptionException;
import com.processdataquality.praeclarus.exception.OptionException;
import com.processdataquality.praeclarus.plugin.uitemplate.PluginUI;

import tech.tablesaw.api.Table;

public abstract class AbstractImperfectInjector extends AbstractDataPattern {
	
	protected Table _detected;
	protected Logger _log;

    protected AbstractImperfectInjector() {
        super();
        _detected = null;
        _log = Logger.getLogger(this.getName());
       
    }
    

    @Override
	public Table detect(Table table) throws OptionException {
		// TODO Auto-generated method stub
		return _detected;
	}


    @Override
    public Table repair(Table master) throws InvalidOptionException {
        return _detected;
    }
    
	@Override
	public boolean canDetect() {
		return false;
	}
    
    @Override
	public boolean canRepair() {
		return true;
	}
    
    @Override
    public PluginUI getUI() {
    	_ui = null;
        return _ui;
   }
}
