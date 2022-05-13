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

package com.processdataquality.praeclarus.ui.component;

import com.processdataquality.praeclarus.logging.EventLogger;
import com.processdataquality.praeclarus.logging.LogEventListener;
import com.processdataquality.praeclarus.logging.entity.AbstractLogEvent;
import com.processdataquality.praeclarus.logging.entity.ConnectorEvent;
import com.processdataquality.praeclarus.logging.entity.OptionChangeEvent;
import com.processdataquality.praeclarus.logging.entity.NodeEvent;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 13/5/2022
 */
public class EventsPanel extends VerticalLayout implements LogEventListener {

    private static final List<String> items = new ArrayList<>();
    private static final ListBox<String> list = new ListBox<>();

    public EventsPanel() {
        super();
        EventLogger.addEventListener(this);
        setPadding(false);
//        H4 title = new H4("Events");
//        UiUtil.removeTopMargin(title);
//        add(title);
        
        list.setItems(items);
        add(list);
        list.setSizeFull();
    }


    @Override
    public void eventLogged(AbstractLogEvent event) {
        list.getListDataView().addItem(getEventLine(event));
    }


    private String getEventLine(AbstractLogEvent event) {
        String line = event.getTimestampAsString() + " :- " + event.getLabel() + ", ";
        if (event instanceof ConnectorEvent) {
            line += getConnectorInfo((ConnectorEvent) event);
        }
        else if (event instanceof OptionChangeEvent) {
            line += getNodeChangeInfo((OptionChangeEvent) event);
        }
        else if (event instanceof NodeEvent) {
            line += ((NodeEvent) event).getNodeName();
        }

        return line;
    }


    private String getConnectorInfo(ConnectorEvent event) {
        switch(event.getCategory()) {
            case CONNECTOR_ADDED:  return event.getSource() + " --> " + event.getTarget();
            case CONNECTOR_REMOVED: return event.getSource() + " -X- " + event.getTarget();
            default: return "";
        }
    }


    private String getNodeChangeInfo(OptionChangeEvent event) {
        return String.format("%s changed option value '%s' from [%s] to [%s]",
                event.getNodeName(), event.getOption(),
                event.getOldValue(), event.getNewValue());
    }

    
}
