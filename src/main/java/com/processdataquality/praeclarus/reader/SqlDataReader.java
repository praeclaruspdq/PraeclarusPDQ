package com.processdataquality.praeclarus.reader;

import com.processdataquality.praeclarus.annotations.PluginMetaData;
import com.processdataquality.praeclarus.plugin.Options;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.jdbc.SqlResultSetReader;

import java.io.IOException;
import java.sql.*;

/**
 * @author Michael Adams
 * @date 30/3/21
 */
@PluginMetaData(
        name = "Database Table Reader",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Loads a log stored as a table in a database."
)
public class SqlDataReader implements DataReader {

    private Options _options = initOptions();

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
    public Options getOptions() {
        return _options;
    }

    @Override
    public void setOptions(Options options) {
        _options = options;
    }


    private Options initOptions() {
        Options options = new Options();
        options.put("DB Type", "MySQL");
        options.put("DB URL", "jdbc:mysql://localhost/DB");
        options.put("User Name", "");
        options.put("Password", "");
        options.put("Table Name", "tablename");
        return options;
    }


    private ResultSet getResultSet() throws IOException {
        Connection connection = null;
        Statement statement = null;
        try {
            Class.forName(getDriver(_options.get("DB Type")));
            String url = (String) _options.get("DB URL");
            String user = (String) _options.get("User Name");
            String pass = (String) _options.get("Password");
            String tableName = (String) _options.get("Table Name");
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
