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
package de.tu_berlin.dima.oligos.db.db2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.javatuples.Quartet;

import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.db.JdbcConnector;
import de.tu_berlin.dima.oligos.db.SchemaConnector;

public class Db2SchemaConnector implements SchemaConnector {

  public final static String REFERENCES_QUERY =
      "SELECT reftbname as parent_table " +
      "     , tbname as child_table " +
      "     , pkcolnames " +
      "     , fkcolnames " +
      "FROM SYSIBM.SYSRELS " +
      "WHERE creator = ?";

  private final JdbcConnector connector;

  public Db2SchemaConnector(final JdbcConnector jdbcConnector) {
    this.connector = jdbcConnector;
  }

  public Set<Quartet<String, String, String, String>> getReferences(final String schema)
      throws SQLException {
    Set<Quartet<String, String, String, String>> references = connector.getReferences(schema);
    return references;
  }

}
