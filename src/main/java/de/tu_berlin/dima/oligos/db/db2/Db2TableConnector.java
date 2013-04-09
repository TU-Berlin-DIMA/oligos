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

import java.sql.SQLException;

import de.tu_berlin.dima.oligos.db.JdbcConnector;
import de.tu_berlin.dima.oligos.db.TableConnector;

public class Db2TableConnector implements TableConnector {
  
  private final static String QUERY =
      "SELECT card " +
      "FROM   SYSSTAT.TABLES " +
      "WHERE  tabschema = ? AND tabname = ?";

  private final JdbcConnector connector;

  public Db2TableConnector(final JdbcConnector jdbcConnector) {
    this.connector = jdbcConnector;
  }

  @Override
  public long getCardinality(final String schema, final String table) throws SQLException {
    return connector.<Long>scalarQuery(QUERY, "card", schema, table);
  }

}
