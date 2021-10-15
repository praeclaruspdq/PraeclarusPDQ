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

/**
 * A store for a single configuration option (i.e. a parameter) for a plugin
 * 
 * @author Michael Adams
 * @date 25/5/21
 */
public class Option {

    private final String _key;              // the option's name
    private final Object _value;            // the option's value
    private boolean _mandatory;             // true if this option must have a value
    private String _mandatoryMessage;       // message that explains why 


    public Option(String key, Object value) {
        _key = key;
        _value = value;
    }

    public boolean isMandatory() { return _mandatory; }

    public void setMandatory(boolean b) { _mandatory = b; }

    public String getMandatoryErrorMessage() { return _mandatoryMessage; }

    public void setMandatoryErrorMessage(String msg) { _mandatoryMessage = msg; }

    public String key() { return _key; }

    public Object get() { return _value; }


    // the remaining methods return the value cast to its actual type

    public String asString() {
        if (! (_value instanceof String)) {
            throw new IllegalArgumentException("Value is missing or not a String");
        }
        return String.valueOf(_value);
    }


    public int asInt() {
        if (! (_value instanceof Integer)) {
            throw new IllegalArgumentException("Value is missing or not an Integer");
        }
        return (int) _value;
    }


    public double asDouble() {
        if (! (_value instanceof Double)) {
            throw new IllegalArgumentException("Value is missing or not a Double");
        }
        return (double) _value;
    }


    public boolean asBoolean() {
        if (! (_value instanceof Boolean)) {
            throw new IllegalArgumentException("Value is missing or not a Boolean");
        }
        return (boolean) _value;
    }

    public char asChar() {
        if (! (_value instanceof Character)) {

            // if it's a string of length 1, let's treat it as a char
            if (_value instanceof String) {
                String value = (String) _value;
                if (value.length() == 1) {
                    return value.charAt(0);
                }
            }
            throw new IllegalArgumentException("Value is missing or not a char type");
        }
        return (char) _value;
    }

    
    @SuppressWarnings("unchecked")
    private <T> T getValueAs(Class<T> clazz) {
        try {
            return (T) _value;
        }
        catch (ClassCastException cce) {
            throw new IllegalArgumentException(cce.getMessage());
        }
    }


}
