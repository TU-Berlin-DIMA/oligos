/*******************************************************************************
 * Copyright 2013 DIMA Research Group, TU Berlin (http://www.dima.tu-berlin.de)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tu_berlin.dima.oligos.db;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.apache.derby.tools.ij;
import org.junit.rules.ExternalResource;
import com.google.common.base.Charsets;
import com.google.common.io.NullOutputStream;

import de.tu_berlin.dima.oligos.db.reference.SchemaRef;

public abstract class AbstractDbDerby extends ExternalResource {

  private final static String JDBC_DRIVER = "jdbc:derby:memory:";
  private final static String ENCODING = Charsets.UTF_8.name();
  private final static String USER_NAME = "TESTUSER";
  
  private final PrintStream out;
  private final Properties properties;

  private Connection connection;

  public AbstractDbDerby() {
    this(false);
  }

  public AbstractDbDerby(boolean verbose) {
    this.out = (verbose) ? System.out : new PrintStream(new NullOutputStream());
    // set database and connection properties
    this.properties = new Properties();
    this.properties.setProperty("user", USER_NAME);
  }

  @Override
  protected void before() throws SQLException, UnsupportedEncodingException {
    // open connection to in-memory database
    connection = DriverManager.getConnection(
        getConnectionString("create=true"), properties);

    // create tables
    out.print("Creating tables ... ");
    ij.runScript(
        connection,
        getSchemaScript(),
        ENCODING,
        out,
        ENCODING);
    out.println("done.");
    // create constraints
    out.print("Creating constraints ... ");
    ij.runScript(
        connection,
        getConstraintScript(),
        ENCODING,
        out,
        ENCODING);
    out.println("done.");

  }

  @Override
  protected void after() {
    try {
      DriverManager.getConnection(getConnectionString("drop=true"), properties);
    } catch (SQLException e) {
      // do nothing
      // derby raises an exception even when dropping the database succeeded
      out.println(e.getMessage());
    } finally {
      connection = null;
    }
  }

  public Connection getConnection() {
    return connection;
  }

  public String getUserName() {
    return USER_NAME;
  }

  public Collection<SchemaRef> getSchemas() {
    return Arrays.asList(new SchemaRef(USER_NAME));
  }

  public Collection<String> getSchemaNames() {
    return Arrays.asList(USER_NAME);
  }

  public abstract String getDataBaseName();

  public abstract InputStream getSchemaScript();

  public abstract InputStream getConstraintScript();

  private String getConnectionString(String cmd) {
    return JDBC_DRIVER + getDataBaseName() + ";" + cmd;
  }

}
