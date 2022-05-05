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

package com.processdataquality.praeclarus.logging.entity;

import com.processdataquality.praeclarus.logging.LogConstant;
import com.processdataquality.praeclarus.logging.EventLogger;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author Michael Adams
 * @date 30/11/21
 */
@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractLogEvent {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private LocalDateTime timestamp;
    private String user;
    private String label;

    public AbstractLogEvent() { }

    public AbstractLogEvent(String user, LogConstant label) {
        setTimestamp(LocalDateTime.now());
        setUser(user);
        setLabel(label.asString());
    }


    public Long getId() { return id; }


    public LocalDateTime getTimestamp() { return timestamp; }

    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }


    public String getUser() { return user; }

    public void setUser(String user) { this.user = user; }


    public String getLabel() {return label; }

    public void setLabel(String label) { this.label = label; }


    @Override
    public String toString() {
        return getTimestamp().format(EventLogger.dtFormatter) + "; User: " + getUser() +
                "; Event: " + getLabel();
    }

}
