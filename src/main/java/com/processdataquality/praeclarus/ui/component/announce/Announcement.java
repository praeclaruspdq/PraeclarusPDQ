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

package com.processdataquality.praeclarus.ui.component.announce;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/**
 * @author Michael Adams
 * @date 10/5/2022
 */
public class Announcement {

    private static final Notification.Position DEFAULT_POSITION = Notification.Position.TOP_END;
    private static final int DEFAULT_TIME = 5000;


    public static Notification show(String msg) {
        return Notification.show(msg, DEFAULT_TIME, DEFAULT_POSITION);
    }


    public static Notification show(String msg, NotificationVariant variant) {
        Notification notification = Notification.show(msg, DEFAULT_TIME, DEFAULT_POSITION);
        notification.addThemeVariants(variant);
        return notification;
    }


    public static Notification success(String msg) {
        return show(msg, NotificationVariant.LUMO_SUCCESS);
    }


    public static Notification highlight(String msg) {
        return show(msg, NotificationVariant.LUMO_PRIMARY);
    }


    public static void error(String msg) {
        new ErrorMsg(msg, DEFAULT_POSITION).open();
    }
    
}
