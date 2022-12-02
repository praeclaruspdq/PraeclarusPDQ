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

import com.processdataquality.praeclarus.exception.InvalidOptionValueException;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A store for a single configuration option (i.e. a parameter) for a plugin
 * 
 * @author Michael Adams
 * @date 25/5/21
 */
public class Option implements Comparable<Option> {

    private final String _key;            // the option's name
    private Object _value;                // the option's value
    private Object _prevValue;                // the option's former value
    private boolean _mandatory;


    // Option can only be created through Options object
    protected Option(String key, Object value) {
        _key = key;
        _value = value;
    }


    public boolean isMandatory() { return _mandatory; }

    public void setMandatory(boolean b) { _mandatory = b; }


    public String key() { return _key; }

    public Object value() { return _value; }


    public void setValue(Object v) {
        _prevValue = _value;
        _value = v;
    }


    public Object getPreviousValue() { return _prevValue; }


    // the remaining methods return the value cast to its actual type

    public String asString() throws InvalidOptionValueException {
//        if (_value == null) {
//            throw new InvalidOptionValueException("Value is missing");
//        }
        return String.valueOf(_value);
    }


    public int asInt() {
        if (! (_value instanceof Integer)) {
            throwOne("an Integer");
        }
        return (int) _value;
    }


    public double asDouble() {
        if (! (_value instanceof Double)) {
            throwOne("a Double");
        }
        return (double) _value;
    }


    public boolean asBoolean() {
        if (! (_value instanceof Boolean)) {
            throwOne("a Boolean");
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
            throwOne( "a char type");
        }
        return (char) _value;
    }


    private void throwOne(String type) {
        throw new InvalidOptionValueException("The value for Option '" + key()  +
                " is missing or not " + type);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Option option = (Option) o;
        return  _key.equals(option._key) && _value.equals(option._value) ;
    }


    @Override
    public int hashCode() {
        return Objects.hash(_key);                // key is unique within Options map
    }


    @Override
    public int compareTo(@NotNull Option o) {
        return _key.compareTo(o.key());
    }

}
