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

package com.processdataquality.praeclarus.reader;

import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.exception.InvalidOptionValueException;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.ReadOptions;
import tech.tablesaw.io.Source;
import tech.tablesaw.io.jdbc.SqlResultSetReader;

import java.io.IOException;
import java.sql.*;

/**
 * @author Michael Adams
 * @date 30/3/21
 */
@Plugin(
        name = "Database Table Reader",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Loads a log stored as a table in a database."
)
public class SqlDataReader extends AbstractDataReader {

    public SqlDataReader() {
        super();
        addDefaultOptions();
    }

    @Override
    protected ReadOptions getReadOptions() throws InvalidOptionValueException {
        return null;
    }

    @Override
    public Table read() throws IOException {
        try {
            return SqlResultSetReader.read(getResultSet());
        }
        catch (SQLException sqle) {
            throw new IOException("Failed to read", sqle);
        }
    }


    @Override
    public void setSource(Source source) { }      // unused

    @Override
    public Source getSource() {       // unused
        return null;
    }

    protected void addDefaultOptions() {
        getOptions().addDefault("DB Type", "MySQL");
        getOptions().addDefault("DB URL", "jdbc:mysql://localhost/DB");
        getOptions().addDefault("User Name", "");
        getOptions().addDefault("Password", "");
        getOptions().addDefault("Table Name", "tablename");
    }


    private ResultSet getResultSet() throws IOException, InvalidOptionValueException {
        Connection connection = null;
        Statement statement = null;
        try {
            Class.forName(getDriver(getOptions().get("DB Type")));
            String url = getOptions().get("DB URL").asString();
            String user = getOptions().get("User Name").asString();
            String pass = getOptions().get("Password").asString();
            String tableName = getOptions().get("Table Name").asString();
            connection = DriverManager.getConnection(url, user, pass);
            statement = connection.createStatement();
            return statement.executeQuery("SELECT * FROM " + tableName);
        }
        catch (ClassNotFoundException cnfe) {
            throw new IOException("Failed to find driver", cnfe);
        }
        catch (SQLException sqle) {
            throw new IOException("Failed to read from database", sqle);
        }
        finally {
            try {
                if (statement != null) statement.close();
            }
            catch (SQLException sqle) {
                // nothing to do;
            }
            try {
                if (connection != null) connection.close();
            }
            catch (SQLException sqle) {
                // nothing to do;
            }
        }

    }


    private String getDriver(Object name) throws IOException {
        if ("MySQL".equals(name)) {
            return "com.mysql.jdbc.Driver";
        }
        throw new IOException("Invalid or unknown DB Type");
    }
}
