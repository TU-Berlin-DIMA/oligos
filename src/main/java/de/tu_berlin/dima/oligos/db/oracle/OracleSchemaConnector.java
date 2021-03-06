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
package de.tu_berlin.dima.oligos.db.oracle;

import de.tu_berlin.dima.oligos.db.JdbcConnector;
import de.tu_berlin.dima.oligos.db.SchemaConnector;
import org.apache.log4j.Logger;
import org.javatuples.Quartet;

import java.sql.SQLException;
import java.util.Set;

public class OracleSchemaConnector implements SchemaConnector {

  private final JdbcConnector connector;

  private static final Logger LOGGER = Logger.getLogger(OracleSchemaConnector.class);

  public OracleSchemaConnector(final JdbcConnector jdbcConnector) {
    this.connector = jdbcConnector;
  }

  @Override
  public Set<Quartet<String, String, String, String>> getReferences(String schema) throws SQLException {
    return this.connector.getReferences(schema);
  }


}
