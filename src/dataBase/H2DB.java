/**
 * Copyright (C) 2014 Martín Pacheco.
 *
 * This file is part of my Thesis aplication called "Extraction and Analysis
 * System of Topics for Software History Reports". Faculty of Exact Sciences of
 * the UNICEN University. Tandil, Argentine. http://www.exa.unicen.edu.ar/
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Email: mpacheco@alumnos.exa.unicen.edu.ar
 *
 * @author Martín Pacheco
 */
package dataBase;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Observable;
import org.h2.jdbcx.JdbcDataSource;

public class H2DB extends Observable {

    JdbcDataSource db = null;
    Connection connection = null;
    Statement stat = null;
    ResultSet rs = null;

    public H2DB(String path, String user, String password) throws SQLException {
        if (db == null) {
            db = new JdbcDataSource();
            db.setURL(path);
            db.setUser(user);
            db.setPassword(password);
            db.setDescription("Base de datos de bugs a procesar por LDA.");
            connection = db.getConnection();
        }
    }

    /**
     *
     * @throws SQLException
     */
    public void destroy() throws SQLException {
        closeConnection();
        db = null;
        connection = null;
        stat = null;
        rs = null;
    }

    /**
     * Creates a connection to data base.
     *
     * @throws SQLException
     */
    public void createConnection() throws SQLException {
        if (connection != null && connection.isClosed()) {
            connection = db.getConnection();
        }
    }

    /**
     * Close the actual connection with the data base.
     *
     * @throws SQLException
     */
    public void closeConnection() throws SQLException {
        connection.close();
    }

    /**
     * Execute a query (select statement) and returns the result set. If another
     * result set exists for this statement, this will be closed (even if this
     * statement fails).
     *
     * @param statement Is the statement to execute-
     * @return The results set.
     */
    public ResultSet executeQuery(String statement) throws SQLException {
        if (statement.length() > 0) {
            stat = connection.createStatement();
            rs = stat.executeQuery(statement);
            return rs;
        }
        return null;
    }

    /**
     * Executes an arbitrary statement.
     *
     * @param statement The sql statement to execute.
     * @return returns true if a result set is available, false if not.
     */
    public boolean executeStatement(String statement) throws SQLException {
        if (statement.length() > 0) {
            stat = connection.createStatement();
            return stat.execute(statement);
        }
        return false;
    }

    /**
     * Closes this statement. All result sets that where created by this
     * statement become invalid after calling this method.
     *
     * @return Returns true if the statement successfully closed. False if not.
     */
    public boolean closeStatement() throws SQLException {
        if (stat != null) {
            stat.close();
            return true;
        }
        return false;
    }

    /**
     * Closes this result set.
     *
     * @return Returns true if the statement successfully closed. False if not.
     * @throws SQLException
     */
    public boolean closeResultSet() throws SQLException {
        if (rs != null) {
            rs.close();
            return true;
        }
        return false;
    }
}
