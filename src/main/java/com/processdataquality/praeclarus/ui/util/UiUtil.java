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

package com.processdataquality.praeclarus.ui.util;

import com.processdataquality.praeclarus.repo.LogEntry;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.ListDataProvider;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 2/11/21
 */
public class UiUtil {

 //   public static final String VAADIN_BLUE = "2C68EC";
    public static final String DISABLED_COLOUR = "#BDC3CC";
    public static final String DEFAULT_BLUE = "#4A9AE9";


    public static Image getLargeLogo() {
        Image image = new Image("icons/laurelWreath.png", "Praeclarus");
        image.setHeight("128px");
        return image;
    }


    public static void removeTopMargin(Component c) {
        setStyle(c,"margin-top", "0");
    }

    public static void removeTopPadding(Component c) {
        setStyle(c,"padding-top", "0");
    }

    public static void removeBottomPadding(Component c) {
        setStyle(c,"padding-bottom", "0");
    }


    public static void setStyle(Component c, String key, String value) {
        c.getElement().getStyle().set(key, value);
    }


    public static void setTooltip(Component c, String tip) {
        setAttribute(c, "title", tip);
    }


    public static void setAttribute(Component c, String key, String value) {
        c.getElement().setAttribute(key, value);
    }


    public static Icon createIcon(VaadinIcon vIcon) {
        return createIcon(vIcon, DEFAULT_BLUE);
    }

    public static Icon createIcon(VaadinIcon vIcon, String colour) {
        Icon icon = vIcon.create();
        icon.setSize("24px");
        icon.setColor(colour);
        return icon;
    }


    public static Button createToolButton(VaadinIcon vIcon, String tip, boolean enabled,
                                          ComponentEventListener<ClickEvent<Button>> clickEvent) {
        Icon icon = createIcon(vIcon);
        Button button = new ToolButton(icon, clickEvent);
        button.setEnabled(enabled);
        setTooltip(button, tip);
        return button;
    }


    public static Grid<Row> tableToGrid(Table table) {
//        if (table.rowCount() == 0) {
//            return emptyTableToGrid(table);
//        }
        Grid<Row> grid = new Grid<>();
        grid.setColumnReorderingAllowed(true);
        for (String name : table.columnNames()) {
            ColumnType colType = table.column(name).type();
            Grid.Column<Row> column;
            if (colType == ColumnType.STRING) {
                column = grid.addColumn(row -> row.getString(name));
            }
            else if (colType == ColumnType.BOOLEAN) {
                column = grid.addColumn(row -> row.getBoolean(name));
            }
            else if (colType == ColumnType.INTEGER) {
                column = grid.addColumn(row -> row.getInt(name));
            }
            else if (colType == ColumnType.LONG) {
                column = grid.addColumn(row -> row.getLong(name));
            }
            else if (colType == ColumnType.FLOAT) {
                column = grid.addColumn(row -> row.getFloat(name));
            }
            else if (colType == ColumnType.DOUBLE) {
                column = grid.addColumn(row -> row.getDouble(name));
            }
            else if (colType == ColumnType.LOCAL_DATE) {
                column = grid.addColumn(row -> row.getDate(name));
            }
            else if (colType == ColumnType.LOCAL_TIME || colType == ColumnType.LOCAL_DATE_TIME) {
                column = grid.addColumn(row -> row.getDateTime(name));
            }
            else if (colType == ColumnType.INSTANT) {
                column = grid.addColumn(row -> row.getInstant(name));
            }
            else {
                column = grid.addColumn(row -> row.getObject(name));
            }

            column.setHeader(new Html("<b>" + name + "</b>"))
                    .setAutoWidth(true).setSortable(true);
        }

        List<Row> rows = new ArrayList<>();
        for (int i=0; i < table.rowCount(); i++) {
            rows.add(table.row(i));
        }
        grid.setItems(rows);
        return grid;
    }

    
    private static Grid<Row> emptyTableToGrid(Table table) {
        Grid<Row> grid = new Grid<>();
        for (String name : table.columnNames()) {
            grid.addColumns(name);
        }
        return grid;
    }


    public static Table gridSelectedToTable(Grid<Row> grid, Table table) {
        Table copy = table.copy();
        copy.clear();
        for (Row row : grid.asMultiSelect().getSelectedItems()) {
            copy.addRow(row.getRowNumber(), table);
        }
        return copy;
    }

    
    public static Table gridToTable(Grid<Row> grid, Table table) {
        Table copy = table.copy();
        copy.clear();

        @SuppressWarnings("unchecked")
        ListDataProvider<Row> provider = (ListDataProvider<Row>) grid.getDataProvider();
        for (Row row : provider.getItems()) {
            copy.addRow(row.getRowNumber(), table);
        }
        return copy;
    }


    public static Grid<LogEntry> logEntriesToGrid(List<LogEntry> entries) {
        Grid<LogEntry> grid = new Grid<>();
        grid.addColumn(LogEntry::getTimeString).setHeader("Time").setAutoWidth(true);
        grid.addColumn(LogEntry::getCommitter).setHeader("Committer").setAutoWidth(true);
        grid.addColumn(LogEntry::getMessage).setHeader("Message").setAutoWidth(true);
        grid.setItems(entries);
        return grid;
    }


    static class ToolButton extends Button {

        public ToolButton(Component icon, ComponentEventListener<ClickEvent<Button>> clickListener) {
            super(icon, clickListener);
        }

        @Override
        public void setEnabled(boolean enabled) {
            ((Icon) getIcon()).setColor(enabled ? DEFAULT_BLUE : DISABLED_COLOUR);
            super.setEnabled(enabled);
        }
    }
    
}