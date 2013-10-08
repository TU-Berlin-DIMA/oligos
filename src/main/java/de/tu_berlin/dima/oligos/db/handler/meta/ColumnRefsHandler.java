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
package de.tu_berlin.dima.oligos.db.handler.meta;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.handlers.AbstractListHandler;

import de.tu_berlin.dima.oligos.db.reference.ColumnRef;
import static de.tu_berlin.dima.oligos.db.JdbcConstants.Identifiers.*;

/**
 * Handler implementation to obtain all columns from a JDBC ResultSet.
 * <br />
 * A call to
 * {@link java.sql.DatabaseMetaData#getColumns(String, String, String, String)} 
 * returns a ResultSet containing the names of all columns and their respective 
 * schemas and tables.
 * 
 * @author Christoph Brücke (christoph.bruecke@campus.tu-berlin.de)
 *
 */
public class ColumnRefsHandler extends AbstractListHandler<ColumnRef> {
  
  @Override
  protected ColumnRef handleRow(ResultSet rs) throws SQLException {
    String schema = rs.getString(SCHEMA_NAME);
    String table = rs.getString(TABLE_NAME);
    String column = rs.getString(COLUMN_NAME);
    return new ColumnRef(schema, table, column);
  }

}
