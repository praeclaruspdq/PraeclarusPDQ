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

package com.processdataquality.praeclarus.plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 12/5/21
 */
public class Options extends HashMap<String, Object> {

    private final Map<String, Object> changes = new HashMap<>();

    @Override
    public Object put(String key, Object o) {
        saveChanges(key, o);
        return super.put(key, o);
    }


    @Override
    public void putAll(Map<? extends String, ?> m) {
        for (String key : m.keySet()) {
            saveChanges(key, m.get(key));
        }
        super.putAll(m);
    }


    public Map<String, Object> getChanges() {
        return changes;
    }


    public String getStringValue(String key) {
        Object o = get(key);
        if (! (o instanceof String)) {
            throw new IllegalArgumentException("Value is not a String");
        }
        return String.valueOf(get(key));
    }


    public int getIntValue(String key) {
        Object o = get(key);
        if (! (o instanceof Integer)) {
            throw new IllegalArgumentException("Value is not an Integer");
        }
        return (int) get(key);
    }


    public double getDoubleValue(String key) {
        Object o = get(key);
        if (! (o instanceof Double)) {
            throw new IllegalArgumentException("Value is not a Double");
        }
        return (double) get(key);
    }


    public boolean getBooleanValue(String key) {
        Object o = get(key);
        if (! (o instanceof Boolean)) {
            throw new IllegalArgumentException("Value is not a Boolean");
        }
        return (boolean) get(key);
    }

    @SuppressWarnings("unchecked")
    private <T> T getValue(String key, Class<T> clazz) {
        try {
            return (T) get(key);
        }
        catch (ClassCastException cce) {
            throw new IllegalArgumentException(cce.getMessage());
        }
    }


    private void saveChanges(String key, Object o) {
         Object existing = get(key);
         if (existing != null && existing.equals(o)) {
             changes.put(key, o);
         }
     }


    public static void main(String[] args) {
        Options o = new Options();
        o.put("one", "string");
        o.put("two", 3);
        o.put("three", 4.5);

        String a = o.getStringValue("one");
        int b = o.getIntValue("two");
        double c = o.getDoubleValue("three");
        assert a.equals("string");
        assert b == 3;
        assert c == 4.5;
    }
}
