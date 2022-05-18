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
import com.processdataquality.praeclarus.exception.InvalidOptionValueException;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An enclosed map of Options (i.e. a set of parameters) to configure a plugin.
 *
 * @author Michael Adams
 * @date 12/5/21
 */
public class Options extends HashMap<String, Option> {         // key is Option's name

    // track any changes made to option values for this plugin
    private final Map<String, Option> changes = new HashMap<>();

    private OptionValueChangeListener listener;                 // will only be one


    public Options() {
        super();
    }

    public Options(Map<String, Object> defaults) {
        this();
        addDefaults(defaults);
    }


    public void setValueChangeListener(OptionValueChangeListener listener) {
        this.listener = listener;
    }


    public Option add(String key, Object o) {
        Option option = new Option(key, o);
        super.put(key, option);
        return option;
    }


    public Option update(Option option) {
        saveChanges(option);
        return super.put(option.key(), option);           // returns previous value
    }

    
    public void addAll(Map<String, Object> map) {
        for (String key : map.keySet()) {
            add(key, map.get(key));
        }
    }


    public Option addDefault(String key, Object o) {
        Option option = new Option(key, o);
        addDefault(option);
        return option;
    }


    public Option addDefault(Option option) {
        return super.put(option.key(), option);              // returns previous value
    }


    public Option getNotNull(String key) throws InvalidOptionException {
        Option option = super.get(key);
        if (option != null) return option;
        throw new InvalidOptionException("Missing parameter: " + key);
    }


    public void addDefaults(Map<String, Object> map) {
         for (String key : map.keySet()) {
             addDefault(key, map.get(key));
         }
     }
     

    public Map<String, Option> getChanges() {
        return changes;
    }


    public JSONObject getChangesAsJson() throws JSONException {
        JSONObject json = new JSONObject();
        for (String key : changes.keySet()) {
             json.put(key, changes.get(key).value());
        }
        return json;
    }


    public List<Option> sort() {
        return values().stream().sorted().collect(Collectors.toList());
    }
    

    private void saveChanges(Option option) {
         Option existing = get(option.key());
         if (! (existing == null || existing.equals(option.value()))) {
             changes.put(option.key(), option);
             if (listener != null) listener.optionValueChanged(option);
         }
     }


     // just a little test 
    public static void main(String[] args) throws InvalidOptionValueException {
        Options o = new Options();
        o.add("one", "string");
        o.add("two", 3);
        o.add("three", 4.5);

        String a = o.get("one").asString();
        int b = o.get("two").asInt();
        double c = o.get("three").asDouble();
        assert a.equals("string");
        assert b == 3;
        assert c == 4.5;
    }
}
