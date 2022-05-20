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
import com.processdataquality.praeclarus.logging.entity.*;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextArea;

import static com.processdataquality.praeclarus.logging.EventType.CONNECTOR_ADDED;

/**
 * @author Michael Adams
 * @date 13/5/2022
 */
@CssImport("./styles/pdq-styles.css")
public class EventsPanel extends Div implements LogEventListener {
    
    private static final TextArea ta = new TextArea();

    public EventsPanel() {
        super();
        EventLogger.addEventListener(this);

        add(ta);
        ta.setWidthFull();
        ta.setClearButtonVisible(true);
        ta.setReadOnly(true);
        ta.addClassName("readonly-text");               // remove dotted border
    }


    @Override
    public void eventLogged(AbstractLogEvent event) {
        ta.setValue(getEventLine(event) + "\n" + ta.getValue());
    }


    private String getEventLine(AbstractLogEvent event) {
        String line = event.getTimestampAsString() + " :- " + event.getLabel() + ", ";
        if (event instanceof ConnectorEvent) {
            line += formatConnectorEvent((ConnectorEvent) event);
        }
        else if (event instanceof NodeExecutionEvent) {
            line += formatNodeExecutionEvent((NodeExecutionEvent) event);
        }
        else if (event instanceof NodeEvent) {
            line += formatNodeEvent((NodeEvent) event);
        }
        else if (event instanceof AbstractGraphEvent) {
            line += formatGraphEvent((AbstractGraphEvent) event);
        }
        else if (event instanceof OptionChangeEvent) {
            line += formatOptionChangeEvent((OptionChangeEvent) event);
        }

        return line;
    }


    private String formatConnectorEvent(ConnectorEvent event) {
        String joiner = event.getCategory() == CONNECTOR_ADDED ? "-->" :  "-X-";
        return String.format("%s %s %s %s",  event.getSourceLabel(), joiner,
                event.getTargetLabel(), formatGraphEvent(event));
    }


    private String formatOptionChangeEvent(OptionChangeEvent event) {
        return String.format("%s [%s] option '%s' value '%s' --> '%s'",
                event.getComponentLabel(), event.getComponentId(), event.getOptionName(),
                event.getOldValue(), event.getNewValue());
    }


    private String formatNodeExecutionEvent(NodeExecutionEvent event) {
        return formatNodeEvent(event) + ", " + event.getNote();
    }


    private String formatNodeEvent(NodeEvent event) {
        return String.format("%s [%s], %s",
                event.getNodeName(), event.getNodeId(), formatGraphEvent(event));
    }


    private String formatGraphEvent(AbstractGraphEvent event) {
        return String.format(" [%s, %s]", event.getGraphName(), event.getGraphID());
    }
    
}
